-- ====================================
-- Units Table Migration
-- ====================================
-- Creates the units table for storing organizational units
-- per institution (multi-tenant).
-- ====================================

CREATE TABLE units (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    institution_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    acronym VARCHAR(50) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_units_institution
        FOREIGN KEY (institution_id)
        REFERENCES institutions(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_units_institution_acronym
        UNIQUE (institution_id, acronym)
);

-- Create indexes for performance
CREATE INDEX idx_units_institution_id ON units(institution_id);
CREATE INDEX idx_units_active ON units(active);
CREATE INDEX idx_units_acronym ON units(acronym);
CREATE INDEX idx_units_institution_active ON units(institution_id, active);
CREATE INDEX idx_units_created_at ON units(created_at DESC);

-- Create trigger for automatic updated_at
CREATE TRIGGER trg_units_updated_at
    BEFORE UPDATE ON units
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON TABLE units IS 'Stores organizational units per institution (multi-tenant)';
COMMENT ON COLUMN units.institution_id IS 'Foreign key to institutions table (tenant isolation)';
COMMENT ON COLUMN units.name IS 'Name of the unit (required, max 255 chars)';
COMMENT ON COLUMN units.acronym IS 'Acronym of the unit (required, unique per institution, max 50 chars)';
COMMENT ON COLUMN units.description IS 'Detailed description of the unit (optional)';
COMMENT ON COLUMN units.active IS 'Whether the unit is active (soft delete flag)';
