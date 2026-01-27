-- ====================================
-- Audit Logs Table Migration
-- ====================================
-- This migration creates the audit_logs table for tracking
-- administrative operations and user management actions.
-- Stores information about who performed what action, when,
-- and on what resources for compliance and accountability.
-- ====================================

-- Create audit_logs table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- User who performed the action (ADMIN or MANAGER)
    performed_by_user_id UUID NOT NULL,
    performed_by_email VARCHAR(255) NOT NULL,

    -- Type of action performed
    action VARCHAR(50) NOT NULL,

    -- Target user (if applicable)
    target_user_id UUID,
    target_user_email VARCHAR(255),

    -- Target institution (if applicable)
    target_institution_id UUID,

    -- JSON representation of changes made
    changes_json TEXT,

    -- Additional context
    description VARCHAR(500),
    http_status INTEGER,
    request_path VARCHAR(500),

    -- Audit timestamp
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_audit_logs_performed_by FOREIGN KEY (performed_by_user_id)
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_logs_target_user FOREIGN KEY (target_user_id)
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_audit_logs_target_institution FOREIGN KEY (target_institution_id)
        REFERENCES institutions(id) ON DELETE SET NULL,

    -- Check constraint for valid action types
    CONSTRAINT chk_audit_action_type CHECK (
        action IN (
            'USER_UPDATED',
            'USER_ROLES_UPDATED',
            'USER_LINKED_TO_INSTITUTION',
            'USER_UNLINKED_FROM_INSTITUTION',
            'USER_ACTIVATED',
            'USER_DEACTIVATED',
            'INSTITUTION_CREATED',
            'INSTITUTION_UPDATED',
            'INSTITUTION_DELETED',
            'ACCESS_DENIED',
            'AUTH_FAILED'
        )
    )
);

-- Create indexes for performance optimization
-- Index for queries filtering by who performed the action
CREATE INDEX idx_audit_logs_performed_by ON audit_logs(performed_by_user_id);

-- Index for queries filtering by target user
CREATE INDEX idx_audit_logs_target_user ON audit_logs(target_user_id);

-- Index for queries filtering by target institution
CREATE INDEX idx_audit_logs_target_institution ON audit_logs(target_institution_id);

-- Index for queries filtering by action type
CREATE INDEX idx_audit_logs_action ON audit_logs(action);

-- Index for temporal queries (most recent first)
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);

-- Composite index for common queries (user actions in institution)
CREATE INDEX idx_audit_logs_user_institution ON audit_logs(performed_by_user_id, target_institution_id, created_at DESC);

-- Add comments for documentation
COMMENT ON TABLE audit_logs IS 'Audit trail for administrative operations and user management actions. Used for compliance, investigation, and accountability.';

COMMENT ON COLUMN audit_logs.performed_by_user_id IS 'UUID of the user who performed the action (ADMIN or MANAGER)';
COMMENT ON COLUMN audit_logs.performed_by_email IS 'Email of the user who performed the action for readability';
COMMENT ON COLUMN audit_logs.action IS 'Type of action performed (USER_UPDATED, USER_LINKED_TO_INSTITUTION, etc.)';
COMMENT ON COLUMN audit_logs.target_user_id IS 'UUID of the user being affected by the action (nullable)';
COMMENT ON COLUMN audit_logs.target_user_email IS 'Email of the target user for audit trail readability';
COMMENT ON COLUMN audit_logs.target_institution_id IS 'UUID of the institution being affected by the action (nullable)';
COMMENT ON COLUMN audit_logs.changes_json IS 'JSON representation of changes made (e.g., {"oldStatus": "PENDING", "newStatus": "ACTIVE"})';
COMMENT ON COLUMN audit_logs.description IS 'Human-readable description of the action';
COMMENT ON COLUMN audit_logs.http_status IS 'HTTP status code returned (for API calls)';
COMMENT ON COLUMN audit_logs.request_path IS 'Request path for context';
COMMENT ON COLUMN audit_logs.created_at IS 'Timestamp when the action was performed';
