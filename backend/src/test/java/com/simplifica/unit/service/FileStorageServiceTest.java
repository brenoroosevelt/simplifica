package com.simplifica.unit.service;

import com.simplifica.application.service.FileStorageService;
import com.simplifica.config.FileStorageProperties;
import com.simplifica.presentation.exception.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for FileStorageService.
 */
class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private FileStorageProperties storageProperties;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        storageProperties = new FileStorageProperties();
        storageProperties.setProvider("local");
        storageProperties.setMaxFileSizeMb(5);
        storageProperties.setAllowedExtensions(java.util.List.of("jpg", "jpeg", "png", "gif", "webp"));

        FileStorageProperties.Local localConfig = new FileStorageProperties.Local();
        localConfig.setBasePath(tempDir.toString());
        localConfig.setPublicUrl("http://localhost:8080/api/public/uploads");
        storageProperties.setLocal(localConfig);

        fileStorageService = new FileStorageService();
        ReflectionTestUtils.setField(fileStorageService, "storageProperties", storageProperties);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up temp directory
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // Ignore
                        }
                    });
        }
    }

    @Test
    void testStoreImage_Success() throws IOException {
        MultipartFile file = createValidImageFile("test.png", 1024);

        FileStorageService.FileUploadResult result = fileStorageService.storeImage(file, "institutions");

        assertNotNull(result);
        assertNotNull(result.getFileUrl());
        assertNotNull(result.getThumbnailUrl());
        assertNotNull(result.getFilename());
        assertTrue(result.getFileUrl().contains("institutions"));
        assertTrue(result.getThumbnailUrl().contains("thumbnails"));
    }

    @Test
    void testStoreImage_FileSizeExceeded() throws IOException {
        // Create a 6MB file (exceeds 5MB limit)
        MultipartFile file = createValidImageFile("large.png", 6 * 1024 * 1024);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> fileStorageService.storeImage(file, "institutions")
        );

        assertTrue(exception.getMessage().contains("exceeds maximum"));
    }

    @Test
    void testStoreImage_InvalidFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.txt",
                "text/plain",
                "Some text content".getBytes()
        );

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> fileStorageService.storeImage(file, "institutions")
        );

        assertTrue(exception.getMessage().contains("must be an image"));
    }

    @Test
    void testStoreImage_DisallowedExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "image.bmp",
                "image/bmp",
                new byte[100]
        );

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> fileStorageService.storeImage(file, "institutions")
        );

        assertTrue(exception.getMessage().contains("type not allowed"));
    }

    @Test
    void testStoreImage_EmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> fileStorageService.storeImage(file, "institutions")
        );

        assertTrue(exception.getMessage().contains("cannot be empty"));
    }

    @Test
    void testStoreImage_ThumbnailGeneration() throws IOException {
        MultipartFile file = createValidImageFile("test.png", 1024);

        FileStorageService.FileUploadResult result = fileStorageService.storeImage(file, "institutions");

        // Check that both files exist
        Path originalPath = extractPathFromUrl(result.getFileUrl());
        Path thumbnailPath = extractPathFromUrl(result.getThumbnailUrl());

        assertTrue(Files.exists(originalPath), "Original file should exist");
        assertTrue(Files.exists(thumbnailPath), "Thumbnail should exist");

        // Verify thumbnail is smaller
        long originalSize = Files.size(originalPath);
        long thumbnailSize = Files.size(thumbnailPath);
        assertTrue(thumbnailSize <= originalSize, "Thumbnail should be smaller or equal to original");
    }

    @Test
    void testDeleteFile_Success() throws IOException {
        // First upload a file
        MultipartFile file = createValidImageFile("test.png", 1024);
        FileStorageService.FileUploadResult result = fileStorageService.storeImage(file, "institutions");

        Path originalPath = extractPathFromUrl(result.getFileUrl());
        Path thumbnailPath = extractPathFromUrl(result.getThumbnailUrl());

        assertTrue(Files.exists(originalPath), "File should exist before deletion");
        assertTrue(Files.exists(thumbnailPath), "Thumbnail should exist before deletion");

        // Delete the file
        fileStorageService.deleteFile(result.getFileUrl());

        // Verify both are deleted
        assertTrue(!Files.exists(originalPath), "File should be deleted");
        assertTrue(!Files.exists(thumbnailPath), "Thumbnail should be deleted");
    }

    @Test
    void testDeleteFile_NullUrl() {
        assertDoesNotThrow(() -> fileStorageService.deleteFile(null));
    }

    @Test
    void testDeleteFile_EmptyUrl() {
        assertDoesNotThrow(() -> fileStorageService.deleteFile(""));
    }

    @Test
    void testDeleteFile_NonExistentFile() {
        assertDoesNotThrow(() ->
                fileStorageService.deleteFile("http://localhost:8080/api/public/uploads/test/nonexistent.png")
        );
    }

    // Security Tests

    @Test
    void testStoreImage_PathTraversalInFolder() throws IOException {
        MultipartFile file = createValidImageFile("test.png", 1024);

        // Test path traversal with ../
        BadRequestException exception1 = assertThrows(
                BadRequestException.class,
                () -> fileStorageService.storeImage(file, "../../../etc")
        );
        assertTrue(exception1.getMessage().contains("Invalid folder name"));

        // Test path traversal with combination
        BadRequestException exception2 = assertThrows(
                BadRequestException.class,
                () -> fileStorageService.storeImage(file, "institutions/../malicious")
        );
        assertTrue(exception2.getMessage().contains("Invalid folder name"));

        // Test path traversal with backslash
        BadRequestException exception3 = assertThrows(
                BadRequestException.class,
                () -> fileStorageService.storeImage(file, "..\\..\\etc")
        );
        assertTrue(exception3.getMessage().contains("Invalid folder name"));

        // Test path traversal with forward slash
        BadRequestException exception4 = assertThrows(
                BadRequestException.class,
                () -> fileStorageService.storeImage(file, "institutions/subfolder")
        );
        assertTrue(exception4.getMessage().contains("Invalid folder name"));
    }

    @Test
    void testStoreImage_InvalidFolder() throws IOException {
        MultipartFile file = createValidImageFile("test.png", 1024);

        // Test non-whitelisted folder
        BadRequestException exception1 = assertThrows(
                BadRequestException.class,
                () -> fileStorageService.storeImage(file, "not-allowed-folder")
        );
        assertTrue(exception1.getMessage().contains("Folder not allowed"));

        // Test empty folder
        BadRequestException exception2 = assertThrows(
                BadRequestException.class,
                () -> fileStorageService.storeImage(file, "")
        );
        assertTrue(exception2.getMessage().contains("Folder cannot be empty"));

        // Test null folder
        BadRequestException exception3 = assertThrows(
                BadRequestException.class,
                () -> fileStorageService.storeImage(file, null)
        );
        assertTrue(exception3.getMessage().contains("Folder cannot be empty"));

        // Test blank folder
        BadRequestException exception4 = assertThrows(
                BadRequestException.class,
                () -> fileStorageService.storeImage(file, "   ")
        );
        assertTrue(exception4.getMessage().contains("Folder cannot be empty"));
    }

    @Test
    void testStoreImage_ValidWhitelistedFolders() throws IOException {
        MultipartFile file = createValidImageFile("test.png", 1024);

        // Test all whitelisted folders - should NOT throw exception
        assertDoesNotThrow(() -> fileStorageService.storeImage(file, "institutions"));
        assertDoesNotThrow(() -> fileStorageService.storeImage(file, "value-chains"));
        assertDoesNotThrow(() -> fileStorageService.storeImage(file, "users"));
    }

    // Helper methods

    private MultipartFile createValidImageFile(String filename, int sizeInBytes) throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);

        byte[] imageBytes = baos.toByteArray();

        // If the generated image is smaller than requested size, pad it
        byte[] finalBytes;
        if (imageBytes.length < sizeInBytes) {
            finalBytes = new byte[sizeInBytes];
            System.arraycopy(imageBytes, 0, finalBytes, 0, imageBytes.length);
        } else {
            finalBytes = imageBytes;
        }

        return new MockMultipartFile(
                "file",
                filename,
                "image/png",
                finalBytes
        );
    }

    private Path extractPathFromUrl(String fileUrl) {
        String publicUrl = storageProperties.getLocal().getPublicUrl();
        String relativePath = fileUrl.replace(publicUrl, "");
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return Paths.get(storageProperties.getLocal().getBasePath(), relativePath);
    }
}
