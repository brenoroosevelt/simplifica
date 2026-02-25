-- ============================================================
-- V19: Create normatives table
-- ============================================================
CREATE TABLE normatives (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    institution_id UUID NOT NULL REFERENCES institutions(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    file_url TEXT,
    file_original_name VARCHAR(512),
    external_link TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_normatives_institution_id ON normatives(institution_id);
CREATE INDEX idx_normatives_created_at ON normatives(created_at);

CREATE OR REPLACE FUNCTION update_normatives_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_normatives_updated_at
    BEFORE UPDATE ON normatives
    FOR EACH ROW EXECUTE FUNCTION update_normatives_updated_at();

COMMENT ON TABLE normatives IS 'Institutional normatives with file or link attachments';
COMMENT ON COLUMN normatives.file_url IS 'URL path to uploaded file via StorageService';
COMMENT ON COLUMN normatives.file_original_name IS 'Original filename as uploaded by user';
COMMENT ON COLUMN normatives.external_link IS 'External URL provided by user (alternative to file upload)';
