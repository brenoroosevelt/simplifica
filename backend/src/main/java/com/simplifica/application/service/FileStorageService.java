package com.simplifica.application.service;

import com.simplifica.config.FileStorageProperties;
import com.simplifica.presentation.exception.BadRequestException;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
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
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.Set;
import java.util.UUID;

/**
 * Service for file storage operations.
 *
 * Handles image and HTML file uploads with automatic validation and storage.
 * For images: generates thumbnails (150px max dimension, maintains aspect ratio).
 * For HTML: validates content without thumbnail generation.
 *
 * Features:
 * - Validates file size and MIME type
 * - Generates thumbnails for images only (150px max dimension, maintains aspect ratio)
 * - Returns public URLs for files (and thumbnails when applicable)
 * - Automatic directory creation
 * - File deletion with cleanup
 * - Support for process mapping HTML files (Bizagi exports)
 */
@Service
@Slf4j
public class FileStorageService {

    private static final int THUMBNAIL_SIZE = 150;
    private static final String THUMBNAIL_FOLDER = "thumbnails";
    private static final long MAX_HTML_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    private static final long MAX_ZIP_FILE_SIZE_BYTES = 50 * 1024 * 1024; // 50MB
    private static final long MAX_EXTRACTED_SIZE_BYTES = 100 * 1024 * 1024; // 100MB
    private static final int HTML_VALIDATION_BUFFER_SIZE = 512; // Bytes to read from start of file
    private static final Set<String> HTML_INDICATORS = Set.of(
            "<!DOCTYPE", "<!doctype",
            "<html", "<HTML",
            "<head", "<HEAD",
            "<body", "<BODY"
    );

    /**
     * Allowed file extensions within ZIP files for security.
     */
    private static final Set<String> ALLOWED_ZIP_EXTENSIONS = Set.of(
            "html", "htm", "css", "js", "json",
            "jpg", "jpeg", "png", "gif", "svg", "webp",
            "txt", "xml"
    );

    /**
     * Whitelist of allowed folders for security.
     * Prevents path traversal attacks through folder parameter.
     */
    private static final Set<String> ALLOWED_FOLDERS = Set.of(
            "institutions",
            "value-chains",
            "users",
            "processes",
            "trainings"
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
     * Stores an HTML file without generating thumbnails.
     *
     * Validates HTML files exported from process mapping tools (e.g., Bizagi).
     * Unlike image uploads, HTML files do not generate thumbnails.
     *
     * @param file the HTML file to store
     * @param folder the folder name within the storage path (e.g., "processes")
     * @return FileUploadResult containing URL for the HTML file (no thumbnail)
     * @throws BadRequestException if validation fails
     */
    public FileUploadResult storeHtmlFile(MultipartFile file, String folder) {
        log.info("Starting HTML file upload process for folder: {}", folder);

        // Security: Validate folder before any file operations
        validateFolder(folder);

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required and cannot be empty");
        }

        validateHtmlFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String uniqueFilename = generateUniqueFilename(extension);

        log.debug("Generated unique filename: {} for original filename: {}", uniqueFilename, originalFilename);

        try {
            Path folderPath = createDirectories(folder);
            Path targetPath = folderPath.resolve(uniqueFilename);

            saveOriginalFile(file, targetPath);

            String fileUrl = buildPublicUrl(folder, uniqueFilename);

            log.info("HTML file uploaded successfully: {}", fileUrl);

            return FileUploadResult.builder()
                    .fileUrl(fileUrl)
                    .thumbnailUrl(null)  // No thumbnail for HTML files
                    .filename(uniqueFilename)
                    .build();

        } catch (IOException e) {
            log.error("Failed to store HTML file: {}", originalFilename, e);
            throw new BadRequestException("Failed to store HTML file: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts and stores a ZIP file containing Bizagi exports.
     *
     * Validates the ZIP file, extracts it to a unique directory, and validates
     * all extracted files for security. Returns the URL to the index.html file
     * or the first HTML file found.
     *
     * @param file the ZIP file to extract
     * @param folder the folder name within the storage path (e.g., "processes")
     * @param processId the UUID of the process (used for directory naming)
     * @return FileUploadResult containing URL to the extracted HTML content
     * @throws BadRequestException if validation fails
     */
    public FileUploadResult storeZipFile(MultipartFile file, String folder, UUID processId) {
        log.info("Starting ZIP file extraction for folder: {}, processId: {}", folder, processId);

        // Security: Validate folder before any file operations
        validateFolder(folder);

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required and cannot be empty");
        }

        validateZipFile(file);

        String extractionDirName = processId.toString();

        try {
            // Create extraction directory: basePath/processes/{processId}/
            Path folderPath = createDirectories(folder);
            Path extractionPath = folderPath.resolve(extractionDirName);

            // Delete existing content if any
            if (Files.exists(extractionPath)) {
                deleteDirectoryRecursively(extractionPath);
            }

            Files.createDirectories(extractionPath);

            // Save ZIP temporarily
            Path tempZipPath = Files.createTempFile("upload", ".zip");
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, tempZipPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // Extract ZIP
            String indexHtmlPath = extractZipFile(tempZipPath.toFile(), extractionPath);

            // Delete temporary ZIP
            Files.deleteIfExists(tempZipPath);

            // Build URL to index.html or first HTML file
            String fileUrl = buildPublicUrl(folder, extractionDirName + "/" + indexHtmlPath);

            log.info("ZIP file extracted successfully to: {}", extractionPath);
            log.info("Index HTML URL: {}", fileUrl);

            return FileUploadResult.builder()
                    .fileUrl(fileUrl)
                    .thumbnailUrl(null)
                    .filename(indexHtmlPath)
                    .build();

        } catch (IOException e) {
            log.error("Failed to extract ZIP file", e);
            throw new BadRequestException("Failed to extract ZIP file: " + e.getMessage(), e);
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
     * Validates HTML file size, MIME type, extension, and content.
     *
     * @param file the file to validate
     * @throws BadRequestException if validation fails
     */
    private void validateHtmlFile(MultipartFile file) {
        log.debug("Validating HTML file: size={} bytes, contentType={}", file.getSize(), file.getContentType());

        // Validate file size (10MB max)
        if (file.getSize() > MAX_HTML_FILE_SIZE_BYTES) {
            throw new BadRequestException(
                    String.format("HTML file size exceeds maximum allowed size of 10 MB")
            );
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("text/html")) {
            throw new BadRequestException("File must be an HTML file (MIME type: text/html)");
        }

        // Validate extension
        String extension = getFileExtension(file.getOriginalFilename());
        if (!extension.equals("html")) {
            throw new BadRequestException("File must have .html extension");
        }

        // Validate content (check if file actually contains HTML)
        validateHtmlContent(file);

        log.debug("HTML file validation passed");
    }

    /**
     * Validates that the file content is actually HTML by checking for common HTML tags.
     * Reads the first 512 bytes to check for HTML indicators without loading the entire file.
     *
     * @param file the file to validate
     * @throws BadRequestException if content validation fails
     */
    private void validateHtmlContent(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] buffer = new byte[HTML_VALIDATION_BUFFER_SIZE];
            int bytesRead = inputStream.read(buffer);

            if (bytesRead <= 0) {
                throw new BadRequestException("HTML file appears to be empty");
            }

            // Convert to string and check for HTML indicators (case-insensitive)
            String content = new String(buffer, 0, bytesRead).toLowerCase();

            boolean isValidHtml = HTML_INDICATORS.stream()
                    .anyMatch(indicator -> content.contains(indicator.toLowerCase()));

            if (!isValidHtml) {
                log.warn("File does not appear to contain valid HTML content");
                throw new BadRequestException(
                        "File does not appear to be a valid HTML file. Expected HTML tags not found."
                );
            }

            log.debug("HTML content validation passed");

        } catch (IOException e) {
            log.error("Failed to validate HTML content", e);
            throw new BadRequestException("Failed to validate HTML file content: " + e.getMessage(), e);
        }
    }

    /**
     * Validates ZIP file size, MIME type, and extension.
     *
     * @param file the file to validate
     * @throws BadRequestException if validation fails
     */
    private void validateZipFile(MultipartFile file) {
        log.debug("Validating ZIP file: size={} bytes, contentType={}", file.getSize(), file.getContentType());

        // Validate file size (50MB max)
        if (file.getSize() > MAX_ZIP_FILE_SIZE_BYTES) {
            throw new BadRequestException(
                    "ZIP file size exceeds maximum allowed size of 50 MB"
            );
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/zip")
                && !contentType.equals("application/x-zip-compressed"))) {
            throw new BadRequestException("File must be a ZIP file");
        }

        // Validate extension
        String extension = getFileExtension(file.getOriginalFilename());
        if (!extension.equals("zip")) {
            throw new BadRequestException("File must have .zip extension");
        }

        log.debug("ZIP file validation passed");
    }

    /**
     * Extracts a ZIP file to a target directory with security validations.
     *
     * @param zipFile the ZIP file to extract
     * @param targetDir the target directory
     * @return the relative path to index.html or first HTML file found
     * @throws IOException if an I/O error occurs
     * @throws BadRequestException if security validation fails
     */
    private String extractZipFile(File zipFile, Path targetDir) throws IOException {
        log.info("Extracting ZIP file to: {}", targetDir);

        String indexHtmlPath = null;
        long totalExtractedSize = 0;

        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // Security: Prevent path traversal
                if (entryName.contains("..")) {
                    log.warn("Path traversal attempt detected in ZIP: {}", entryName);
                    throw new BadRequestException("Invalid file path in ZIP: " + entryName);
                }

                // Skip directories and hidden files
                if (entry.isDirectory() || entryName.startsWith(".") || entryName.contains("/.")) {
                    continue;
                }

                // Security: Check file extension
                String extension = getFileExtensionFromPath(entryName);
                if (extension != null && !ALLOWED_ZIP_EXTENSIONS.contains(extension)) {
                    log.warn("Skipping file with disallowed extension: {}", entryName);
                    continue;
                }

                // Security: Check total extracted size
                totalExtractedSize += entry.getSize();
                if (totalExtractedSize > MAX_EXTRACTED_SIZE_BYTES) {
                    throw new BadRequestException("Extracted content exceeds maximum size of 100 MB");
                }

                // Extract file
                Path targetFile = targetDir.resolve(entryName).normalize();

                // Security: Ensure target file is within target directory
                if (!targetFile.startsWith(targetDir)) {
                    log.warn("Path traversal attempt detected: {}", entryName);
                    throw new BadRequestException("Invalid file path in ZIP");
                }

                // Create parent directories if needed
                Files.createDirectories(targetFile.getParent());

                // Write file
                try (InputStream inputStream = zip.getInputStream(entry);
                     OutputStream outputStream = Files.newOutputStream(targetFile)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, len);
                    }
                }

                log.debug("Extracted: {}", entryName);

                // Track index.html or first HTML file
                if (entryName.equalsIgnoreCase("index.html") || entryName.equalsIgnoreCase("index.htm")) {
                    indexHtmlPath = entryName;
                } else if (indexHtmlPath == null && extension != null
                        && (extension.equals("html") || extension.equals("htm"))) {
                    indexHtmlPath = entryName;
                }
            }
        }

        if (indexHtmlPath == null) {
            throw new BadRequestException("No HTML file found in ZIP");
        }

        log.info("ZIP extraction completed. Total files extracted, total size: {} bytes", totalExtractedSize);
        return indexHtmlPath;
    }

    /**
     * Recursively deletes a directory and all its contents.
     *
     * @param path the directory to delete
     * @throws IOException if an I/O error occurs
     */
    private void deleteDirectoryRecursively(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((a, b) -> b.compareTo(a)) // Reverse order to delete files before directories
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            log.warn("Failed to delete: {}", p, e);
                        }
                    });
        }
    }

    /**
     * Gets file extension from a file path.
     *
     * @param path the file path
     * @return the extension in lowercase, or null if no extension
     */
    private String getFileExtensionFromPath(String path) {
        if (path == null || !path.contains(".")) {
            return null;
        }
        int lastDot = path.lastIndexOf(".");
        int lastSlash = Math.max(path.lastIndexOf("/"), path.lastIndexOf("\\"));
        if (lastDot > lastSlash) {
            return path.substring(lastDot + 1).toLowerCase();
        }
        return null;
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
