package com.simplifica.storage.service;

import com.simplifica.presentation.exception.ResourceNotFoundException;
import com.simplifica.storage.adapter.StorageAdapter;
import com.simplifica.storage.adapter.StorageAdapterFactory;
import com.simplifica.storage.adapter.StorageException;
import com.simplifica.storage.config.StorageProperties;
import com.simplifica.storage.domain.FileCategory;
import com.simplifica.storage.domain.StorageType;
import com.simplifica.storage.domain.StoredFile;
import com.simplifica.storage.repository.StoredFileRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of StorageService using pluggable storage adapters (Flysystem-style).
 *
 * This implementation delegates storage operations to a StorageAdapter,
 * which can be filesystem, Dropbox, Google Drive, etc. The adapter is
 * chosen based on configuration (STORAGE_PROVIDER environment variable).
 *
 * The service manages file metadata in the database while delegating
 * actual storage to the adapter.
 */
@Service
@Transactional(readOnly = true)
public class FileStorageServiceV2 implements StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageServiceV2.class);

    private static final int THUMBNAIL_WIDTH = 400;
    private static final int THUMBNAIL_HEIGHT = 300;

    private final StorageAdapter storageAdapter;

    @Autowired
    private StoredFileRepository storedFileRepository;

    @Autowired
    private FileValidationService fileValidationService;

    @Autowired
    private StorageProperties storageProperties;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Constructor that creates the appropriate storage adapter based on configuration.
     */
    public FileStorageServiceV2(StorageAdapterFactory factory, StorageProperties properties,
                                @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.storageAdapter = factory.createAdapter(properties, baseUrl);
        LOGGER.info("StorageService initialized with adapter: {}", storageAdapter.getAdapterType());
    }

    @Override
    @Transactional
    public FileUploadResult storeFile(
            MultipartFile file,
            String entityType,
            UUID entityId,
            FileCategory category,
            boolean generateThumbnail) {

        LOGGER.info("Storing file for entity {}:{} category:{}", entityType, entityId, category);

        // Validate file
        fileValidationService.validateFile(file, category);

        try {
            // Detect MIME type
            String contentType = fileValidationService.detectMimeType(
                    file.getInputStream(), file.getOriginalFilename());

            // Generate storage path
            String storageKey = generateStorageKey(entityType, entityId, category, file.getOriginalFilename());

            // Store file using adapter (Flysystem-style)
            String adapterKey = storageAdapter.store(storageKey, file.getInputStream(), contentType);

            // Calculate ETag (MD5 hash)
            String etag = calculateETag(file.getInputStream());

            // Determine storage type from adapter
            StorageType storageType = getStorageTypeFromAdapter(storageAdapter.getAdapterType());

            // Create metadata
            StoredFile storedFile = StoredFile.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .category(category)
                    .filename(file.getOriginalFilename())
                    .contentType(contentType)
                    .size(file.getSize())
                    .storageKey(adapterKey)
                    .etag(etag)
                    .storageType(storageType)
                    .build();

            storedFile = storedFileRepository.save(storedFile);

            String fileUrl = buildFileUrl(storedFile.getId());
            String thumbnailUrl = null;

            // Generate thumbnail if requested and file is an image
            if (generateThumbnail && fileValidationService.isImage(contentType)) {
                thumbnailUrl = generateAndStoreThumbnail(file, entityType, entityId, category, storedFile.getId());
            }

            LOGGER.info("File stored successfully: {} using adapter: {}", storedFile.getId(), storageAdapter.getAdapterType());
            return new FileUploadResult(storedFile, fileUrl, thumbnailUrl);

        } catch (IOException | StorageException e) {
            LOGGER.error("Failed to store file for {}:{}", entityType, entityId, e);
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource loadAsResource(UUID fileId) {
        StoredFile storedFile = getMetadata(fileId);

        try {
            // Use adapter to retrieve file
            InputStream inputStream = storageAdapter.retrieve(storedFile.getStorageKey());
            return new InputStreamResource(inputStream);
        } catch (StorageException e) {
            throw new ResourceNotFoundException("File not found: " + fileId);
        }
    }

    @Override
    public InputStream loadAsStream(UUID fileId) {
        StoredFile storedFile = getMetadata(fileId);

        try {
            // Use adapter to retrieve file
            return storageAdapter.retrieve(storedFile.getStorageKey());
        } catch (StorageException e) {
            throw new ResourceNotFoundException("File not found: " + fileId);
        }
    }

    @Override
    public StoredFile getMetadata(UUID fileId) {
        return storedFileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("StoredFile", fileId.toString()));
    }

    @Override
    public List<StoredFile> findByEntity(String entityType, UUID entityId) {
        return storedFileRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    @Override
    public List<StoredFile> findByEntityAndCategory(String entityType, UUID entityId, FileCategory category) {
        return storedFileRepository.findByEntityTypeAndEntityIdAndCategory(entityType, entityId, category);
    }

    @Override
    @Transactional
    public void delete(UUID fileId) {
        StoredFile storedFile = getMetadata(fileId);

        try {
            // Use adapter to delete file
            storageAdapter.delete(storedFile.getStorageKey());
            storedFileRepository.delete(storedFile);
            LOGGER.info("File deleted: {}", fileId);
        } catch (StorageException e) {
            LOGGER.error("Failed to delete file: {}", fileId, e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteByEntity(String entityType, UUID entityId) {
        List<StoredFile> files = findByEntity(entityType, entityId);
        files.forEach(file -> delete(file.getId()));
    }

    @Override
    @Transactional
    public void deleteByEntityAndCategory(String entityType, UUID entityId, FileCategory category) {
        List<StoredFile> files = findByEntityAndCategory(entityType, entityId, category);
        files.forEach(file -> delete(file.getId()));
    }

    @Override
    public String getFileUrl(UUID fileId) {
        return buildFileUrl(fileId);
    }

    /**
     * Generates storage key (path) for a file.
     */
    private String generateStorageKey(String entityType, UUID entityId, FileCategory category, String filename) {
        String extension = getFileExtension(filename);
        String folder = entityType.toLowerCase().replace("_", "-") + "s";
        String subfolder = category.name().toLowerCase().replace("_", "-");

        return String.format("%s/%s/%s/original%s", folder, entityId, subfolder, extension);
    }

    /**
     * Generates and stores a thumbnail for an image.
     */
    private String generateAndStoreThumbnail(
            MultipartFile file,
            String entityType,
            UUID entityId,
            FileCategory category,
            UUID originalFileId) throws IOException {

        LOGGER.debug("Generating thumbnail for file: {}", originalFileId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
                .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                .outputFormat("jpg")
                .outputQuality(0.85)
                .toOutputStream(outputStream);

        byte[] thumbnailBytes = outputStream.toByteArray();
        String extension = ".jpg";
        String folder = entityType.toLowerCase().replace("_", "-") + "s";
        String subfolder = category.name().toLowerCase().replace("_", "-");
        String thumbnailKey = String.format("%s/%s/%s/thumbnail%s", folder, entityId, subfolder, extension);

        // Use adapter to store thumbnail
        try (ByteArrayInputStream thumbnailStream = new ByteArrayInputStream(thumbnailBytes)) {
            storageAdapter.store(thumbnailKey, thumbnailStream, "image/jpeg");
        } catch (StorageException e) {
            throw new IOException("Failed to store thumbnail", e);
        }

        // Determine storage type from adapter
        StorageType storageType = getStorageTypeFromAdapter(storageAdapter.getAdapterType());

        // Create metadata for thumbnail
        StoredFile thumbnailFile = StoredFile.builder()
                .entityType(entityType)
                .entityId(entityId)
                .category(category)
                .filename("thumbnail" + extension)
                .contentType("image/jpeg")
                .size((long) thumbnailBytes.length)
                .storageKey(thumbnailKey)
                .etag(calculateETag(new ByteArrayInputStream(thumbnailBytes)))
                .storageType(storageType)
                .build();

        thumbnailFile = storedFileRepository.save(thumbnailFile);

        LOGGER.info("Thumbnail generated: {}", thumbnailFile.getId());
        return buildFileUrl(thumbnailFile.getId());
    }

    /**
     * Calculates MD5 hash (ETag) for a file.
     */
    private String calculateETag(InputStream inputStream) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }

            return HexFormat.of().formatHex(md.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            LOGGER.warn("Failed to calculate ETag", e);
            return UUID.randomUUID().toString();
        }
    }

    /**
     * Builds public URL for a file.
     */
    private String buildFileUrl(UUID fileId) {
        return baseUrl + "/files/" + fileId;
    }

    /**
     * Converts adapter type string to StorageType enum.
     */
    private StorageType getStorageTypeFromAdapter(String adapterType) {
        return switch (adapterType.toLowerCase()) {
            case "filesystem", "local" -> StorageType.FILESYSTEM;
            case "dropbox" -> StorageType.DROPBOX;
            case "googledrive" -> StorageType.GCS; // Google Cloud Storage
            case "s3" -> StorageType.S3;
            case "azure" -> StorageType.AZURE;
            default -> StorageType.FILESYSTEM;
        };
    }

    /**
     * Extracts file extension from filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        int lastDot = filename.lastIndexOf(".");
        return filename.substring(lastDot);
    }
}
