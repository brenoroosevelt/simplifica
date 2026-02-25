-- Add content column to training_videos table
-- This column stores optional additional content/description for each video

ALTER TABLE training_videos ADD COLUMN content TEXT;
