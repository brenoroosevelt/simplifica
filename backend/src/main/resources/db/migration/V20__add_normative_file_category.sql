-- Add NORMATIVE_FILE to the chk_category constraint on stored_files
-- PostgreSQL requires dropping and recreating CHECK constraints
ALTER TABLE stored_files DROP CONSTRAINT chk_category;

ALTER TABLE stored_files ADD CONSTRAINT chk_category CHECK (category IN (
    'INSTITUTION_LOGO',
    'VALUE_CHAIN_IMAGE',
    'PROCESS_MAPPING',
    'PROCESS_DOCUMENT',
    'TRAINING_COVER',
    'TRAINING_VIDEO',
    'TRAINING_ATTACHMENT',
    'NORMATIVE_FILE',
    'OTHER'
));
