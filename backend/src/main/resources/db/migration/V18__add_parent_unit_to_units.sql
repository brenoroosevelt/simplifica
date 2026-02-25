-- ============================================================
-- V18: Add parent_unit column to units table
-- ============================================================
-- Adds an optional free-text field to record the name of
-- the superior organizational unit. This is informational only
-- (no foreign key / no self-reference constraint).
-- ============================================================

ALTER TABLE units
    ADD COLUMN parent_unit VARCHAR(255) NULL;

COMMENT ON COLUMN units.parent_unit IS 'Name of the superior organizational unit (free text, optional)';
