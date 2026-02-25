package com.simplifica.storage.adapter;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Google Drive storage adapter.
 *
 * Stores files in Google Drive using the Google Drive API.
 *
 * Configuration needed:
 * - Service Account credentials JSON file
 */
public class GoogleDriveStorageAdapter implements StorageAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleDriveStorageAdapter.class);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Simplifica Backend";

    private final Drive driveService;
    private final String baseFolderId;
    private final Map<String, String> folderCache = new HashMap<>();

    /**
     * Creates a Google Drive storage adapter.
     *
     * @param credentialsPath path to service account JSON file
     * @param baseFolderName name of base folder in Drive (will be created if doesn't exist)
     */
    public GoogleDriveStorageAdapter(String credentialsPath, String baseFolderName) {
        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new FileInputStream(credentialsPath))
                    .createScoped(Collections.singleton("https://www.googleapis.com/auth/drive.file"));

            this.driveService = new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // Get or create base folder
            this.baseFolderId = getOrCreateFolder(null, baseFolderName);

            LOGGER.info("Google Drive adapter initialized with folder: {}", baseFolderName);

        } catch (GeneralSecurityException | IOException e) {
            throw new StorageException("Failed to initialize Google Drive adapter", e);
        }
    }

    @Override
    public String store(String path, InputStream inputStream, String contentType) throws StorageException {
        try {
            // Parse path to get folder structure and filename
            String[] parts = path.split("/");
            String filename = parts[parts.length - 1];

            // Navigate/create folder structure
            String parentFolderId = baseFolderId;
            for (int i = 0; i < parts.length - 1; i++) {
                parentFolderId = getOrCreateFolder(parentFolderId, parts[i]);
            }

            // Check if file already exists
            String existingFileId = findFileInFolder(parentFolderId, filename);

            File fileMetadata = new File();
            fileMetadata.setName(filename);

            if (existingFileId == null) {
                // Create new file
                fileMetadata.setParents(Collections.singletonList(parentFolderId));

                InputStreamContent mediaContent = new InputStreamContent(contentType, inputStream);
                File file = driveService.files()
                        .create(fileMetadata, mediaContent)
                        .setFields("id, name, webViewLink, webContentLink")
                        .execute();

                LOGGER.debug("File created in Google Drive: {} (ID: {})", filename, file.getId());
                return file.getId();

            } else {
                // Update existing file
                InputStreamContent mediaContent = new InputStreamContent(contentType, inputStream);
                File file = driveService.files()
                        .update(existingFileId, fileMetadata, mediaContent)
                        .setFields("id, name, webViewLink, webContentLink")
                        .execute();

                LOGGER.debug("File updated in Google Drive: {} (ID: {})", filename, file.getId());
                return file.getId();
            }

        } catch (IOException e) {
            throw new StorageException("Failed to store file in Google Drive: " + path, e);
        }
    }

    @Override
    public InputStream retrieve(String path) throws StorageException {
        try {
            String fileId = findFileByPath(path);
            if (fileId == null) {
                throw new StorageException("File not found in Google Drive: " + path);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);

            return new ByteArrayInputStream(outputStream.toByteArray());

        } catch (IOException e) {
            throw new StorageException("Failed to retrieve file from Google Drive: " + path, e);
        }
    }

    @Override
    public boolean delete(String path) throws StorageException {
        try {
            String fileId = findFileByPath(path);
            if (fileId == null) {
                return false;
            }

            driveService.files().delete(fileId).execute();
            LOGGER.debug("File deleted from Google Drive: {}", path);
            return true;

        } catch (IOException e) {
            throw new StorageException("Failed to delete file from Google Drive: " + path, e);
        }
    }

    @Override
    public boolean exists(String path) {
        return findFileByPath(path) != null;
    }

    @Override
    public String getPublicUrl(String path) {
        try {
            String fileId = findFileByPath(path);
            if (fileId == null) {
                return null;
            }

            // Make file publicly accessible
            Permission permission = new Permission()
                    .setType("anyone")
                    .setRole("reader");

            driveService.permissions().create(fileId, permission).execute();

            // Get file to retrieve webContentLink
            File file = driveService.files().get(fileId)
                    .setFields("webContentLink, webViewLink")
                    .execute();

            return file.getWebContentLink();

        } catch (IOException e) {
            LOGGER.warn("Failed to get public URL for: {}", path, e);
            return null;
        }
    }

    @Override
    public String getAdapterType() {
        return "googledrive";
    }

    private String getOrCreateFolder(String parentId, String folderName) throws IOException {
        // Check cache
        String cacheKey = (parentId != null ? parentId : "root") + "/" + folderName;
        if (folderCache.containsKey(cacheKey)) {
            return folderCache.get(cacheKey);
        }

        // Search for existing folder
        String query = String.format("name='%s' and mimeType='application/vnd.google-apps.folder'",
                folderName.replace("'", "\\'"));

        if (parentId != null) {
            query += String.format(" and '%s' in parents", parentId);
        }

        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();

        List<File> files = result.getFiles();

        if (files != null && !files.isEmpty()) {
            String folderId = files.get(0).getId();
            folderCache.put(cacheKey, folderId);
            return folderId;
        }

        // Create folder
        File fileMetadata = new File();
        fileMetadata.setName(folderName);
        fileMetadata.setMimeType("application/vnd.google-apps.folder");

        if (parentId != null) {
            fileMetadata.setParents(Collections.singletonList(parentId));
        }

        File folder = driveService.files()
                .create(fileMetadata)
                .setFields("id")
                .execute();

        folderCache.put(cacheKey, folder.getId());
        return folder.getId();
    }

    private String findFileInFolder(String folderId, String filename) throws IOException {
        String query = String.format("name='%s' and '%s' in parents and trashed=false",
                filename.replace("'", "\\'"), folderId);

        FileList result = driveService.files().list()
                .setQ(query)
                .setSpaces("drive")
                .setFields("files(id)")
                .execute();

        List<File> files = result.getFiles();
        return (files != null && !files.isEmpty()) ? files.get(0).getId() : null;
    }

    private String findFileByPath(String path) {
        try {
            String[] parts = path.split("/");
            String filename = parts[parts.length - 1];

            // Navigate to folder
            String parentFolderId = baseFolderId;
            for (int i = 0; i < parts.length - 1; i++) {
                parentFolderId = getOrCreateFolder(parentFolderId, parts[i]);
            }

            return findFileInFolder(parentFolderId, filename);

        } catch (IOException e) {
            LOGGER.warn("Failed to find file by path: {}", path, e);
            return null;
        }
    }
}
