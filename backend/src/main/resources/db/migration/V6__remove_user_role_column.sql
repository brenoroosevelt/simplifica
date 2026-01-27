-- Remove coluna role da tabela users
-- Role agora existe apenas no contexto de instituições através de user_institution_roles

ALTER TABLE users DROP COLUMN IF EXISTS role;

-- Adicionar comentário à tabela para documentar a mudança
COMMENT ON TABLE users IS 'Users table - roles are now managed per institution in user_institution_roles';
