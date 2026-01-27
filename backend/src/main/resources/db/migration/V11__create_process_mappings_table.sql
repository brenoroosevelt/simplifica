-- ====================================
-- Process Mappings Table Migration
-- ====================================
-- Stores multiple HTML mapping files per process (Bizagi exports)
-- ====================================

CREATE TABLE process_mappings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    process_id UUID NOT NULL,
    file_url VARCHAR(1024) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    file_size BIGINT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_process_mappings_process
        FOREIGN KEY (process_id)
        REFERENCES processes(id)
        ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_process_mappings_process_id ON process_mappings(process_id);
CREATE INDEX idx_process_mappings_uploaded_at ON process_mappings(uploaded_at DESC);

-- Add comments for documentation
COMMENT ON TABLE process_mappings IS 'Stores HTML mapping files for processes (Bizagi exports)';
COMMENT ON COLUMN process_mappings.process_id IS 'Process that owns this mapping file';
COMMENT ON COLUMN process_mappings.file_url IS 'URL to access the HTML file (max 1024 chars)';
COMMENT ON COLUMN process_mappings.filename IS 'Original filename (max 255 chars)';
COMMENT ON COLUMN process_mappings.file_size IS 'File size in bytes';
COMMENT ON COLUMN process_mappings.uploaded_at IS 'Timestamp when the file was uploaded';
