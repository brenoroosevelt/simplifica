-- ====================================
-- Processes Enums Migration
-- ====================================
-- Creates enum types for process management system
-- These enums support the Portfolio of Processes feature
-- ====================================

-- Documentation status enum
CREATE TYPE documentation_status AS ENUM (
    'DOCUMENTED',
    'NOT_DOCUMENTED',
    'DOCUMENTED_WITH_PENDING'
);

-- External user guidance status enum
CREATE TYPE external_guidance_status AS ENUM (
    'AVAILABLE',
    'NOT_AVAILABLE',
    'AVAILABLE_WITH_PENDING',
    'NOT_NECESSARY'
);

-- Risk management status enum
CREATE TYPE risk_management_status AS ENUM (
    'PREPARED',
    'PREPARED_WITH_PENDING',
    'NOT_PREPARED'
);

-- Process mapping status enum
CREATE TYPE process_mapping_status AS ENUM (
    'MAPPED',
    'NOT_MAPPED',
    'MAPPED_WITH_PENDING'
);

-- Add comments for documentation
COMMENT ON TYPE documentation_status IS 'Process documentation status';
COMMENT ON TYPE external_guidance_status IS 'External user guidance availability status';
COMMENT ON TYPE risk_management_status IS 'Risk management preparation status';
COMMENT ON TYPE process_mapping_status IS 'Process mapping completion status';
