package com.simplifica.storage.domain;

/**
 * Categories of files stored in the system.
 * Used to organize files and apply category-specific rules.
 */
public enum FileCategory {
    /**
     * Institution logos
     */
    INSTITUTION_LOGO,

    /**
     * Value chain images
     */
    VALUE_CHAIN_IMAGE,

    /**
     * Process mapping files (Bizagi exports)
     */
    PROCESS_MAPPING,

    /**
     * Process documents (PDFs, etc)
     */
    PROCESS_DOCUMENT,

    /**
     * Training cover images
     */
    TRAINING_COVER,

    /**
     * Training videos
     */
    TRAINING_VIDEO,

    /**
     * Training attachments/documents
     */
    TRAINING_ATTACHMENT,

    /**
     * Generic/other files
     */
    OTHER
}
