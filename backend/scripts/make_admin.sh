#!/bin/bash

# ====================================
# Script: Make User Admin
# ====================================
# Este script torna um usuário admin do sistema
# e vincula à instituição Administração Simplifica
# ====================================

set -e

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configurações do banco (ajustar conforme necessário)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-simplifica_db}"
DB_USER="${DB_USER:-simplifica}"
DB_PASSWORD="${DB_PASSWORD:-simplifica}"

# Email do usuário
USER_EMAIL="breno.roosevelt@gmail.com"

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}Make User Admin Script${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""
echo "Configurações:"
echo "  Host: $DB_HOST:$DB_PORT"
echo "  Database: $DB_NAME"
echo "  User: $DB_USER"
echo "  Email: $USER_EMAIL"
echo ""

# Verificar se psql está instalado
if ! command -v psql &> /dev/null; then
    echo -e "${RED}ERRO: psql não encontrado!${NC}"
    echo "Instale o PostgreSQL client:"
    echo "  Ubuntu/Debian: sudo apt-get install postgresql-client"
    echo "  Mac: brew install postgresql"
    exit 1
fi

# Confirmar execução
read -p "Deseja continuar? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "Cancelado."
    exit 0
fi

# Executar script SQL
echo -e "${YELLOW}Executando script SQL...${NC}"
echo ""

PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME <<EOF
DO \$\$
DECLARE
    v_user_id UUID;
    v_institution_id UUID;
    v_user_institution_id UUID;
BEGIN
    -- Buscar ID do usuário pelo email
    SELECT id INTO v_user_id
    FROM users
    WHERE email = '$USER_EMAIL';

    -- Verificar se usuário existe
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'ERRO: Usuário $USER_EMAIL não encontrado! O usuário precisa fazer login primeiro via OAuth.';
    END IF;

    RAISE NOTICE 'Usuário encontrado: %', v_user_id;

    -- Buscar ID da instituição 1 (Administração Simplifica)
    SELECT id INTO v_institution_id
    FROM institutions
    WHERE acronym = 'SIMP-ADMIN'
    LIMIT 1;

    IF v_institution_id IS NULL THEN
        RAISE EXCEPTION 'ERRO: Instituição SIMP-ADMIN não encontrada! Execute a migration V4 primeiro.';
    END IF;

    RAISE NOTICE 'Instituição encontrada: %', v_institution_id;

    -- 1. Atualizar role global do usuário para ADMIN
    UPDATE users
    SET
        role = 'ADMIN',
        status = 'ACTIVE',
        updated_at = CURRENT_TIMESTAMP
    WHERE id = v_user_id;

    RAISE NOTICE 'Usuário atualizado: role=ADMIN, status=ACTIVE';

    -- 2. Verificar se vínculo com instituição já existe
    SELECT id INTO v_user_institution_id
    FROM user_institutions
    WHERE user_id = v_user_id
      AND institution_id = v_institution_id;

    IF v_user_institution_id IS NULL THEN
        -- Criar vínculo com instituição
        INSERT INTO user_institutions (
            id,
            user_id,
            institution_id,
            active,
            linked_at,
            linked_by,
            updated_at
        ) VALUES (
            uuid_generate_v4(),
            v_user_id,
            v_institution_id,
            true,
            CURRENT_TIMESTAMP,
            v_user_id,
            CURRENT_TIMESTAMP
        )
        RETURNING id INTO v_user_institution_id;

        RAISE NOTICE 'Vínculo criado com instituição: %', v_user_institution_id;
    ELSE
        -- Reativar vínculo se existir
        UPDATE user_institutions
        SET
            active = true,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = v_user_institution_id;

        RAISE NOTICE 'Vínculo reativado: %', v_user_institution_id;
    END IF;

    -- 3. Adicionar papel ADMIN na instituição (se não existir)
    INSERT INTO user_institution_roles (user_institution_id, role)
    VALUES (v_user_institution_id, 'ADMIN')
    ON CONFLICT (user_institution_id, role) DO NOTHING;

    RAISE NOTICE 'Papel ADMIN adicionado à instituição';

    -- Resultado final
    RAISE NOTICE '';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'SUCESSO! Usuário $USER_EMAIL agora é:';
    RAISE NOTICE '- Role Global: ADMIN';
    RAISE NOTICE '- Status: ACTIVE';
    RAISE NOTICE '- Instituição: Administração Simplifica';
    RAISE NOTICE '- Papel na Instituição: ADMIN';
    RAISE NOTICE '========================================';

END \$\$;

-- Verificação
SELECT
    u.email,
    u.role as global_role,
    u.status,
    i.name as institution,
    array_agg(uir.role) as institution_roles
FROM users u
LEFT JOIN user_institutions ui ON ui.user_id = u.id AND ui.active = true
LEFT JOIN institutions i ON i.id = ui.institution_id
LEFT JOIN user_institution_roles uir ON uir.user_institution_id = ui.id
WHERE u.email = '$USER_EMAIL'
GROUP BY u.email, u.role, u.status, i.name;
EOF

EXIT_CODE=$?

echo ""
if [ $EXIT_CODE -eq 0 ]; then
    echo -e "${GREEN}✓ Script executado com sucesso!${NC}"
    echo ""
    echo "Próximos passos:"
    echo "1. Fazer logout e login novamente no sistema"
    echo "2. Verificar que o menu admin está visível"
    echo "3. Acessar /admin/users para gerenciar usuários"
else
    echo -e "${RED}✗ Erro ao executar script!${NC}"
    echo ""
    echo "Possíveis causas:"
    echo "1. Usuário $USER_EMAIL ainda não fez login via OAuth"
    echo "   → Acesse o sistema e faça login primeiro"
    echo "2. Migration V4 não foi executada"
    echo "   → Execute: mvn flyway:migrate"
    echo "3. Credenciais do banco incorretas"
    echo "   → Verifique DB_HOST, DB_USER, DB_PASSWORD"
    exit 1
fi

exit 0
