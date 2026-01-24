-- ====================================
-- Fix Institution Column Types
-- ====================================
-- This migration ensures that name and acronym columns
-- are VARCHAR instead of bytea, which can occur due to
-- schema generation issues.
-- ====================================

-- Check if the institutions table exists and fix column types
-- Using ALTER TABLE to ensure columns are VARCHAR types

-- Fix name column if it's not VARCHAR
DO $$
BEGIN
    -- Check if name column exists and alter it to VARCHAR if needed
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'institutions' AND column_name = 'name'
    ) THEN
        ALTER TABLE institutions ALTER COLUMN name TYPE VARCHAR(255) USING name::text;
    END IF;
END $$;

-- Fix acronym column if it's not VARCHAR
DO $$
BEGIN
    -- Check if acronym column exists and alter it to VARCHAR if needed
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'institutions' AND column_name = 'acronym'
    ) THEN
        ALTER TABLE institutions ALTER COLUMN acronym TYPE VARCHAR(50) USING acronym::text;
    END IF;
END $$;

-- Verify constraints are in place
DO $$
BEGIN
    -- Ensure NOT NULL constraint on name
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'institutions'
          AND column_name = 'name'
          AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE institutions ALTER COLUMN name SET NOT NULL;
    END IF;

    -- Ensure NOT NULL constraint on acronym
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'institutions'
          AND column_name = 'acronym'
          AND is_nullable = 'NO'
    ) THEN
        ALTER TABLE institutions ALTER COLUMN acronym SET NOT NULL;
    END IF;

    -- Ensure UNIQUE constraint on acronym
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE table_name = 'institutions'
          AND constraint_type = 'UNIQUE'
          AND constraint_name LIKE '%acronym%'
    ) THEN
        ALTER TABLE institutions ADD CONSTRAINT uk_institutions_acronym UNIQUE (acronym);
    END IF;
END $$;
