-- Add training type and external link to trainings table
ALTER TABLE trainings ADD COLUMN IF NOT EXISTS training_type VARCHAR(20) NOT NULL DEFAULT 'VIDEO_SEQUENCE';
ALTER TABLE trainings ADD COLUMN IF NOT EXISTS external_link VARCHAR(1024);

COMMENT ON COLUMN trainings.training_type IS 'Type of training: VIDEO_SEQUENCE or LINK';
COMMENT ON COLUMN trainings.external_link IS 'External URL for LINK type trainings';
