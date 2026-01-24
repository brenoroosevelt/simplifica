-- ====================================
-- Multi-Tenant Schema Migration
-- ====================================
-- This migration creates the multi-tenant infrastructure:
-- - institutions: Stores tenant/institution information
-- - user_institutions: Links users to institutions with roles
-- - user_institution_roles: Stores user roles per institution
-- - security_audit_log: Audit trail for security events
-- ====================================

-- Create institutions table
CREATE TABLE institutions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    acronym VARCHAR(50) NOT NULL UNIQUE,
    logo_url VARCHAR(1024),
    logo_thumbnail_url VARCHAR(1024),
    logo_uploaded_at TIMESTAMP,
    type VARCHAR(50) NOT NULL,
    domain VARCHAR(255) UNIQUE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_institution_type CHECK (type IN ('FEDERAL', 'ESTADUAL', 'MUNICIPAL', 'PRIVADA'))
);

-- Create indexes for institutions table
CREATE INDEX idx_institutions_acronym ON institutions(acronym);
CREATE INDEX idx_institutions_domain ON institutions(domain);
CREATE INDEX idx_institutions_active ON institutions(active);
CREATE INDEX idx_institutions_type ON institutions(type);
CREATE INDEX idx_institutions_created_at ON institutions(created_at DESC);

-- Create user_institutions table
CREATE TABLE user_institutions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    institution_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    linked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    linked_by UUID,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_institutions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_institutions_institution FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_institutions_linked_by FOREIGN KEY (linked_by) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT uk_user_institution UNIQUE (user_id, institution_id)
);

-- Create indexes for user_institutions table
CREATE INDEX idx_user_institutions_user_id ON user_institutions(user_id);
CREATE INDEX idx_user_institutions_institution_id ON user_institutions(institution_id);
CREATE INDEX idx_user_institutions_linked_at ON user_institutions(linked_at DESC);
CREATE INDEX idx_user_institutions_active ON user_institutions(active);

-- Create user_institution_roles table
CREATE TABLE user_institution_roles (
    user_institution_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_user_institution_roles FOREIGN KEY (user_institution_id) REFERENCES user_institutions(id) ON DELETE CASCADE,
    CONSTRAINT chk_institution_role CHECK (role IN ('ADMIN', 'MANAGER', 'VIEWER')),
    CONSTRAINT pk_user_institution_roles PRIMARY KEY (user_institution_id, role)
);

-- Create index for user_institution_roles table
CREATE INDEX idx_user_institution_roles_ui_id ON user_institution_roles(user_institution_id);

-- Create security_audit_log table
CREATE TABLE security_audit_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    institution_id UUID,
    action VARCHAR(100) NOT NULL,
    resource VARCHAR(100) NOT NULL,
    resource_id UUID,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    result VARCHAR(50) NOT NULL,
    CONSTRAINT chk_audit_result CHECK (result IN ('SUCCESS', 'FAILURE', 'DENIED'))
);

-- Create indexes for security_audit_log table
CREATE INDEX idx_audit_log_user_id ON security_audit_log(user_id);
CREATE INDEX idx_audit_log_institution_id ON security_audit_log(institution_id);
CREATE INDEX idx_audit_log_timestamp ON security_audit_log(timestamp DESC);
CREATE INDEX idx_audit_log_action ON security_audit_log(action);
CREATE INDEX idx_audit_log_result ON security_audit_log(result);

-- Create function to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create triggers for updated_at columns
CREATE TRIGGER trg_institutions_updated_at
    BEFORE UPDATE ON institutions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_user_institutions_updated_at
    BEFORE UPDATE ON user_institutions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON TABLE institutions IS 'Stores tenant/institution information for multi-tenant architecture';
COMMENT ON COLUMN institutions.acronym IS 'Unique acronym/abbreviation for the institution (e.g., UFMS)';
COMMENT ON COLUMN institutions.type IS 'Institution type: FEDERAL, ESTADUAL, MUNICIPAL, PRIVADA';
COMMENT ON COLUMN institutions.domain IS 'Email domain for automatic user linking (e.g., ufms.br)';
COMMENT ON COLUMN institutions.active IS 'Whether the institution is active (soft delete)';

COMMENT ON TABLE user_institutions IS 'Links users to institutions with access control';
COMMENT ON COLUMN user_institutions.linked_by IS 'User who created the link (admin)';
COMMENT ON COLUMN user_institutions.active IS 'Whether the link is active (soft delete)';

COMMENT ON TABLE user_institution_roles IS 'Stores user roles per institution (ADMIN, MANAGER, VIEWER)';
COMMENT ON COLUMN user_institution_roles.role IS 'Role within institution: ADMIN (manage), MANAGER (moderate), VIEWER (read-only)';

COMMENT ON TABLE security_audit_log IS 'Audit trail for security-related events and access control';
COMMENT ON COLUMN security_audit_log.action IS 'Action performed (e.g., ACCESS_RESOURCE, CREATE_INSTITUTION)';
COMMENT ON COLUMN security_audit_log.result IS 'Result of action: SUCCESS, FAILURE, DENIED';
