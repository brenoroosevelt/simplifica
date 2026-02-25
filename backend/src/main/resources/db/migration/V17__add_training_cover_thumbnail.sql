-- Add thumbnail URL column to trainings table
ALTER TABLE trainings ADD COLUMN IF NOT EXISTS cover_image_thumbnail_url VARCHAR(1024);

COMMENT ON COLUMN trainings.cover_image_thumbnail_url IS 'URL of the cover image thumbnail (400x300px)';
