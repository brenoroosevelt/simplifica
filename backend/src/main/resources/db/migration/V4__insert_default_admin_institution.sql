-- ====================================
-- Insert Default Admin Institution
-- ====================================
-- This migration inserts the "Administração Simplifica" institution
-- that serves as the system administration tenant. Users from this
-- institution are eligible for global ADMIN roles.
-- ====================================

-- Insert default admin institution (idempotent)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM institutions WHERE acronym = 'SIMP-ADMIN') THEN
        INSERT INTO institutions (
            id,
            name,
            acronym,
            type,
            domain,
            active,
            created_at,
            updated_at
        ) VALUES (
            uuid_generate_v4(),
            'Administração Simplifica',
            'SIMP-ADMIN',
            'PRIVADA',
            'simplifica.ai',
            true,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
    END IF;
END $$;
