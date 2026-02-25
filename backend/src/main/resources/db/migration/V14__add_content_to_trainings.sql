-- Add content column to trainings table
-- This column stores optional additional content/description for the training

ALTER TABLE trainings ADD COLUMN content TEXT;
