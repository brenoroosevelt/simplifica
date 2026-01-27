-- ====================================
-- Value Chains Table Migration
-- ====================================
-- This migration creates the value_chains table for managing
-- value chains (cadeias de valor) within institutions.
-- Each value chain belongs to a specific institution (multi-tenant).
-- ====================================

-- Create value_chains table
CREATE TABLE value_chains (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    institution_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    image_url VARCHAR(1024),
    image_thumbnail_url VARCHAR(1024),
    image_uploaded_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_value_chains_institution FOREIGN KEY (institution_id)
        REFERENCES institutions(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_value_chains_institution_id ON value_chains(institution_id);
CREATE INDEX idx_value_chains_active ON value_chains(active);
CREATE INDEX idx_value_chains_institution_active ON value_chains(institution_id, active);
CREATE INDEX idx_value_chains_name ON value_chains(name);

-- Create trigger for updated_at column
CREATE TRIGGER trg_value_chains_updated_at
    BEFORE UPDATE ON value_chains
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON TABLE value_chains IS 'Stores value chains (cadeias de valor) for institutions';
COMMENT ON COLUMN value_chains.institution_id IS 'Institution that owns this value chain (multi-tenant isolation)';
COMMENT ON COLUMN value_chains.name IS 'Value chain name (e.g., "Agricultura Familiar", "Turismo Rural")';
COMMENT ON COLUMN value_chains.description IS 'Detailed description of the value chain';
COMMENT ON COLUMN value_chains.image_url IS 'URL of the uploaded image';
COMMENT ON COLUMN value_chains.image_thumbnail_url IS 'URL of the thumbnail version of the image';
COMMENT ON COLUMN value_chains.image_uploaded_at IS 'Timestamp when the image was uploaded';
COMMENT ON COLUMN value_chains.active IS 'Whether the value chain is active (soft delete)';
