-- ====================================
-- Processes Table Migration
-- ====================================
-- Creates the processes table for portfolio management
-- Multi-tenant with institution isolation
-- ====================================

CREATE TABLE processes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    institution_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    value_chain_id UUID,
    responsible_unit_id UUID,
    direct_unit_id UUID,
    description TEXT,
    is_critical BOOLEAN NOT NULL DEFAULT false,

    -- Documentation
    documentation_status documentation_status,
    documentation_url VARCHAR(1024),

    -- External User Guidance
    external_guidance_status external_guidance_status,
    external_guidance_url VARCHAR(1024),

    -- Risk Management
    risk_management_status risk_management_status,
    risk_management_url VARCHAR(1024),

    -- Process Mapping
    mapping_status process_mapping_status,

    -- Audit fields
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    CONSTRAINT fk_processes_institution
        FOREIGN KEY (institution_id)
        REFERENCES institutions(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_processes_value_chain
        FOREIGN KEY (value_chain_id)
        REFERENCES value_chains(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_processes_responsible_unit
        FOREIGN KEY (responsible_unit_id)
        REFERENCES units(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_processes_direct_unit
        FOREIGN KEY (direct_unit_id)
        REFERENCES units(id)
        ON DELETE SET NULL
);

-- Create indexes for performance
CREATE INDEX idx_processes_institution_id ON processes(institution_id);
CREATE INDEX idx_processes_active ON processes(active);
CREATE INDEX idx_processes_institution_active ON processes(institution_id, active);
CREATE INDEX idx_processes_name ON processes(name);
CREATE INDEX idx_processes_value_chain ON processes(value_chain_id);
CREATE INDEX idx_processes_responsible_unit ON processes(responsible_unit_id);
CREATE INDEX idx_processes_is_critical ON processes(is_critical);
CREATE INDEX idx_processes_mapping_status ON processes(mapping_status);
CREATE INDEX idx_processes_created_at ON processes(created_at DESC);

-- Create trigger for updated_at column
CREATE TRIGGER trg_processes_updated_at
    BEFORE UPDATE ON processes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON TABLE processes IS 'Stores process portfolio for institutions (multi-tenant)';
COMMENT ON COLUMN processes.institution_id IS 'Institution owner (tenant isolation)';
COMMENT ON COLUMN processes.name IS 'Process name (required, max 255 chars)';
COMMENT ON COLUMN processes.value_chain_id IS 'Value chain this process belongs to (optional)';
COMMENT ON COLUMN processes.responsible_unit_id IS 'Unit responsible for the process (optional)';
COMMENT ON COLUMN processes.direct_unit_id IS 'Direct unit for the process (optional)';
COMMENT ON COLUMN processes.description IS 'Detailed description of the process (optional, max 5000 chars)';
COMMENT ON COLUMN processes.is_critical IS 'Whether this is a critical process (default false)';
COMMENT ON COLUMN processes.documentation_status IS 'Status of process documentation';
COMMENT ON COLUMN processes.documentation_url IS 'URL to documentation (optional, max 1024 chars)';
COMMENT ON COLUMN processes.external_guidance_status IS 'Status of external user guidance';
COMMENT ON COLUMN processes.external_guidance_url IS 'URL to external guidance (optional, max 1024 chars)';
COMMENT ON COLUMN processes.risk_management_status IS 'Status of risk management preparation';
COMMENT ON COLUMN processes.risk_management_url IS 'URL to risk management docs (optional, max 1024 chars)';
COMMENT ON COLUMN processes.mapping_status IS 'Status of process mapping completion';
COMMENT ON COLUMN processes.active IS 'Whether the process is active (soft delete flag)';
