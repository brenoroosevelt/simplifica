-- ====================================
-- Trainings Table Migration
-- ====================================
-- Creates the trainings table for institutional training management
-- Multi-tenant with institution isolation
-- ====================================

CREATE TABLE trainings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    institution_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    cover_image_url VARCHAR(1024),

    -- Audit fields
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_trainings_institution
        FOREIGN KEY (institution_id)
        REFERENCES institutions(id)
        ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_trainings_institution_id ON trainings(institution_id);
CREATE INDEX idx_trainings_active ON trainings(active);
CREATE INDEX idx_trainings_institution_active ON trainings(institution_id, active);
CREATE INDEX idx_trainings_title ON trainings(title);
CREATE INDEX idx_trainings_created_at ON trainings(created_at DESC);

-- Create trigger for updated_at column
CREATE TRIGGER trg_trainings_updated_at
    BEFORE UPDATE ON trainings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON TABLE trainings IS 'Stores institutional trainings (multi-tenant)';
COMMENT ON COLUMN trainings.institution_id IS 'Institution owner (tenant isolation)';
COMMENT ON COLUMN trainings.title IS 'Training title (required, max 255 chars)';
COMMENT ON COLUMN trainings.description IS 'Detailed description of the training (optional)';
COMMENT ON COLUMN trainings.cover_image_url IS 'URL to cover image (optional, max 1024 chars)';
COMMENT ON COLUMN trainings.active IS 'Whether the training is active (soft delete flag)';
