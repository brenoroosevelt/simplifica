-- Configurações de inicialização do PostgreSQL para Claude Agents

-- Configurar encoding UTF8 (já é padrão, mas deixando explícito)
SET client_encoding = 'UTF8';

-- Criar extensão uuid-ossp para geração de UUIDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Mensagem de confirmação
DO $$
BEGIN
  RAISE NOTICE 'Simplifica Database initialized successfully';
  RAISE NOTICE 'Extensions created: uuid-ossp';
END $$;
