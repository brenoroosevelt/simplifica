package com.simplifica.application.service;

import com.simplifica.config.FileStorageProperties;
import com.simplifica.presentation.exception.BadRequestException;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * Service for file storage operations.
 *
 * Handles image uploads with automatic thumbnail generation, file validation,
 * and deletion. Supports local filesystem storage with configurable paths and URLs.
 *
 * Features:
 * - Validates file size and MIME type
 * - Generates thumbnails (150px max dimension, maintains aspect ratio)
 * - Returns public URLs for both original and thumbnail
 * - Automatic directory creation
 * - File deletion with cleanup
 */
@Service
@Slf4j
public class FileStorageService {

    private static final int THUMBNAIL_SIZE = 150;
    private static final String THUMBNAIL_FOLDER = "thumbnails";

    /**
     * Whitelist of allowed folders for security.
     * Prevents path traversal attacks through folder parameter.
     */
    private static final Set<String> ALLOWED_FOLDERS = Set.of(
            "institutions",
            "value-chains",
            "users"
    );

    @Autowired
    private FileStorageProperties storageProperties;

    /**
     * Stores an image file and generates a thumbnail.
     *
     * @param file the multipart file to store
     * @param folder the folder name within the storage path (e.g., "institutions")
     * @return FileUploadResult containing URLs for the original file and thumbnail
     * @throws BadRequestException if validation fails
     */
    public FileUploadResult storeImage(MultipartFile file, String folder) {
        log.info("Starting file upload process for folder: {}", folder);

        // Security: Validate folder before any file operations
        validateFolder(folder);

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required and cannot be empty");
        }

        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(extension);

        log.debug("Generated unique filename: {} for original filename: {}", uniqueFilename, originalFilename);

        try {
            Path folderPath = createDirectories(folder);
            Path thumbnailsPath = folderPath.resolve(THUMBNAIL_FOLDER);
            Files.createDirectories(thumbnailsPath);

            Path targetPath = folderPath.resolve(uniqueFilename);
            Path thumbnailPath = thumbnailsPath.resolve(uniqueFilename);

            saveOriginalFile(file, targetPath);
            generateAndSaveThumbnail(file, thumbnailPath);

            String fileUrl = buildPublicUrl(folder, uniqueFilename);
            String thumbnailUrl = buildPublicUrl(folder, THUMBNAIL_FOLDER + "/" + uniqueFilename);

            log.info("File uploaded successfully: {}", fileUrl);

            return FileUploadResult.builder()
                    .fileUrl(fileUrl)
                    .thumbnailUrl(thumbnailUrl)
                    .filename(uniqueFilename)
                    .build();

        } catch (IOException e) {
            log.error("Failed to store file: {}", originalFilename, e);
            throw new BadRequestException("Failed to store file: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a file and its thumbnail based on the file URL.
     *
     * @param fileUrl the public URL of the file to delete
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.debug("Delete requested with null or empty URL, skipping");
            return;
        }

        log.info("Deleting file: {}", fileUrl);

        try {
            Path filePath = extractPathFromUrl(fileUrl);
            Files.deleteIfExists(filePath);
            log.debug("Deleted original file: {}", filePath);

            Path thumbnailPath = getThumbnailPath(filePath);
            Files.deleteIfExists(thumbnailPath);
            log.debug("Deleted thumbnail file: {}", thumbnailPath);

            log.info("File and thumbnail deleted successfully: {}", fileUrl);

        } catch (IOException e) {
            log.warn("Failed to delete file: {}", fileUrl, e);
        }
    }

    /**
     * Validates folder name to prevent path traversal attacks.
     *
     * @param folder the folder name to validate
     * @throws BadRequestException if validation fails
     */
    private void validateFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            throw new BadRequestException("Folder cannot be empty");
        }

        // Check for path traversal patterns
        if (folder.contains("..") || folder.contains("/") || folder.contains("\\")) {
            log.warn("Path traversal attempt detected in folder: {}", folder);
            throw new BadRequestException("Invalid folder name");
        }

        // Check against whitelist
        if (!ALLOWED_FOLDERS.contains(folder)) {
            log.warn("Attempt to use non-whitelisted folder: {}", folder);
            throw new BadRequestException("Folder not allowed: " + folder);
        }

        log.debug("Folder validation passed: {}", folder);
    }

    /**
     * Validates file size and MIME type.
     *
     * @param file the file to validate
     * @throws BadRequestException if validation fails
     */
    private void validateFile(MultipartFile file) {
        log.debug("Validating file: size={} bytes, contentType={}", file.getSize(), file.getContentType());

        if (file.getSize() > storageProperties.getMaxFileSizeBytes()) {
            long maxSizeMb = storageProperties.getMaxFileSizeMb();
            throw new BadRequestException(
                    String.format("File size exceeds maximum allowed size of %d MB", maxSizeMb)
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("File must be an image");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!storageProperties.getAllowedExtensions().contains(extension.toLowerCase())) {
            throw new BadRequestException(
                    String.format("File type not allowed. Allowed types: %s",
                            String.join(", ", storageProperties.getAllowedExtensions()))
            );
        }

        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage image = ImageIO.read(inputStream);
            if (image == null) {
                throw new BadRequestException("File is not a valid image");
            }
        } catch (IOException e) {
            throw new BadRequestException("Failed to validate image file: " + e.getMessage(), e);
        }

        log.debug("File validation passed");
    }

    /**
     * Saves the original file to the target path.
     *
     * @param file the file to save
     * @param targetPath the destination path
     * @throws IOException if an I/O error occurs
     */
    private void saveOriginalFile(MultipartFile file, Path targetPath) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Original file saved to: {}", targetPath);
        }
    }

    /**
     * Generates and saves a thumbnail for the image.
     *
     * @param file the original image file
     * @param thumbnailPath the destination path for the thumbnail
     * @throws IOException if an I/O error occurs
     */
    private void generateAndSaveThumbnail(MultipartFile file, Path thumbnailPath) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            BufferedImage originalImage = ImageIO.read(inputStream);
            if (originalImage == null) {
                throw new IOException("Failed to read image for thumbnail generation");
            }

            BufferedImage thumbnail = resizeImage(originalImage, THUMBNAIL_SIZE);

            String extension = getFileExtension(file.getOriginalFilename());
            String formatName = getImageFormatName(extension);

            File thumbnailFile = thumbnailPath.toFile();
            ImageIO.write(thumbnail, formatName, thumbnailFile);

            log.debug("Thumbnail saved to: {}", thumbnailPath);
        }
    }

    /**
     * Resizes an image maintaining aspect ratio.
     *
     * @param original the original image
     * @param maxSize the maximum dimension (width or height)
     * @return the resized image
     */
    private BufferedImage resizeImage(BufferedImage original, int maxSize) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        double ratio = (double) originalWidth / originalHeight;
        int newWidth;
        int newHeight;

        if (originalWidth > originalHeight) {
            newWidth = Math.min(originalWidth, maxSize);
            newHeight = (int) (newWidth / ratio);
        } else {
            newHeight = Math.min(originalHeight, maxSize);
            newWidth = (int) (newHeight * ratio);
        }

        Image scaledImage = original.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = resizedImage.createGraphics();
        graphics.drawImage(scaledImage, 0, 0, null);
        graphics.dispose();

        log.debug("Image resized from {}x{} to {}x{}", originalWidth, originalHeight, newWidth, newHeight);

        return resizedImage;
    }

    /**
     * Creates the necessary directories for file storage.
     *
     * @param folder the folder name
     * @return the created path
     * @throws IOException if an I/O error occurs
     */
    private Path createDirectories(String folder) throws IOException {
        Path folderPath = Paths.get(storageProperties.getLocal().getBasePath(), folder);
        Files.createDirectories(folderPath);
        log.debug("Ensured directory exists: {}", folderPath);
        return folderPath;
    }

    /**
     * Builds a public URL for a file.
     *
     * @param folder the folder name
     * @param filename the file name
     * @return the public URL
     */
    private String buildPublicUrl(String folder, String filename) {
        String publicUrl = storageProperties.getLocal().getPublicUrl();
        if (publicUrl.endsWith("/")) {
            publicUrl = publicUrl.substring(0, publicUrl.length() - 1);
        }
        return String.format("%s/%s/%s", publicUrl, folder, filename);
    }

    /**
     * Extracts the file path from a public URL.
     * Validates that the resolved path is within the base directory to prevent path traversal attacks.
     *
     * @param fileUrl the public URL
     * @return the file path
     * @throws BadRequestException if path traversal is detected
     */
    private Path extractPathFromUrl(String fileUrl) {
        String publicUrl = storageProperties.getLocal().getPublicUrl();
        String relativePath = fileUrl.replace(publicUrl, "");
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        // Security: Validate that resolved path is within base directory
        Path filePath = Paths.get(storageProperties.getLocal().getBasePath(), relativePath)
                             .normalize().toAbsolutePath();

        Path baseDir = Paths.get(storageProperties.getLocal().getBasePath())
                             .normalize().toAbsolutePath();

        if (!filePath.startsWith(baseDir)) {
            log.warn("Path traversal attempt detected in deleteFile: {}", fileUrl);
            throw new BadRequestException("Invalid file path");
        }

        return filePath;
    }

    /**
     * Gets the thumbnail path for a given file path.
     *
     * @param filePath the original file path
     * @return the thumbnail path
     */
    private Path getThumbnailPath(Path filePath) {
        Path parent = filePath.getParent();
        String filename = filePath.getFileName().toString();
        return parent.resolve(THUMBNAIL_FOLDER).resolve(filename);
    }

    /**
     * Generates a unique filename with UUID.
     *
     * @param extension the file extension
     * @return the unique filename
     */
    private String generateUniqueFilename(String extension) {
        return UUID.randomUUID() + "." + extension;
    }

    /**
     * Extracts the file extension from a filename.
     *
     * @param filename the filename
     * @return the extension in lowercase
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new BadRequestException("File must have an extension");
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Gets the ImageIO format name for a file extension.
     *
     * @param extension the file extension
     * @return the format name for ImageIO
     */
    private String getImageFormatName(String extension) {
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> "jpg";
            case "png" -> "png";
            case "gif" -> "gif";
            case "webp" -> "webp";
            default -> "jpg";
        };
    }

    /**
     * Result of a file upload operation.
     */
    @Getter
    @Builder
    public static class FileUploadResult {
        private final String fileUrl;
        private final String thumbnailUrl;
        private final String filename;
    }
}
