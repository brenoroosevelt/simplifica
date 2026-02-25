-- Create stored_files table for file metadata
-- This table stores metadata about files managed by Spring Content
CREATE TABLE stored_files (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,
    category VARCHAR(50) NOT NULL DEFAULT 'OTHER',
    filename VARCHAR(512) NOT NULL,
    content_type VARCHAR(127) NOT NULL,
    size BIGINT NOT NULL,
    storage_key VARCHAR(1024) NOT NULL,
    etag VARCHAR(64),
    storage_type VARCHAR(20) NOT NULL DEFAULT 'FILESYSTEM',
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    uploaded_by UUID,
    CONSTRAINT chk_size_positive CHECK (size > 0),
    CONSTRAINT chk_category CHECK (category IN (
        'INSTITUTION_LOGO',
        'VALUE_CHAIN_IMAGE',
        'PROCESS_MAPPING',
        'PROCESS_DOCUMENT',
        'TRAINING_COVER',
        'TRAINING_VIDEO',
        'TRAINING_ATTACHMENT',
        'OTHER'
    )),
    CONSTRAINT chk_storage_type CHECK (storage_type IN (
        'FILESYSTEM',
        'S3',
        'AZURE',
        'GCS'
    ))
);

-- Create indexes for common queries
CREATE INDEX idx_stored_files_entity ON stored_files (entity_type, entity_id);
CREATE INDEX idx_stored_files_category ON stored_files (category);
CREATE INDEX idx_stored_files_uploaded_at ON stored_files (uploaded_at);

-- Add comments
COMMENT ON TABLE stored_files IS 'Metadata for files managed by Spring Content';
COMMENT ON COLUMN stored_files.entity_type IS 'Type of entity owning this file (e.g., ValueChain, Institution)';
COMMENT ON COLUMN stored_files.entity_id IS 'UUID of the entity owning this file';
COMMENT ON COLUMN stored_files.category IS 'Category of file for organization and rules';
COMMENT ON COLUMN stored_files.storage_key IS 'Path or key where file is stored (filesystem path or S3 key)';
COMMENT ON COLUMN stored_files.etag IS 'MD5 hash for caching and integrity';
COMMENT ON COLUMN stored_files.storage_type IS 'Storage backend used for this file';
