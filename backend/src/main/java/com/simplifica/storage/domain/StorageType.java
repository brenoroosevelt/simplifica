package com.simplifica.storage.domain;

/**
 * Storage backend types supported by the system.
 */
public enum StorageType {
    /**
     * Local filesystem storage
     */
    FILESYSTEM,

    /**
     * Dropbox storage
     */
    DROPBOX,

    /**
     * Amazon S3 or S3-compatible storage (MinIO)
     */
    S3,

    /**
     * Microsoft Azure Blob Storage
     */
    AZURE,

    /**
     * Google Cloud Storage / Google Drive
     */
    GCS
}
