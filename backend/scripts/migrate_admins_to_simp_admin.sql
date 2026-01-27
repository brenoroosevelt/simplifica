-- ====================================
-- Script: Migrar Admins para SIMP-ADMIN
-- ====================================
-- Este script deve ser executado ANTES da migration V6
-- que remove a coluna role da tabela users.
--
-- O que faz:
-- - Busca todos os usuários com role = 'ADMIN'
-- - Vincula à instituição SIMP-ADMIN
-- - Adiciona role ADMIN na instituição
-- ====================================

DO $$
DECLARE
    v_admin_institution_id UUID;
    v_user_record RECORD;
    v_user_institution_id UUID;
    v_total_migrated INTEGER := 0;
BEGIN
    RAISE NOTICE '';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Iniciando migração de admins para SIMP-ADMIN';
    RAISE NOTICE '========================================';
    RAISE NOTICE '';

    -- Buscar instituição SIMP-ADMIN
    SELECT id INTO v_admin_institution_id
    FROM institutions
    WHERE acronym = 'SIMP-ADMIN'
    LIMIT 1;

    IF v_admin_institution_id IS NULL THEN
        RAISE EXCEPTION 'ERRO: Instituição SIMP-ADMIN não encontrada! Execute a migration V4 primeiro.';
    END IF;

    RAISE NOTICE 'Instituição SIMP-ADMIN encontrada: %', v_admin_institution_id;
    RAISE NOTICE '';

    -- Para cada usuário com role ADMIN antiga
    FOR v_user_record IN
        SELECT id, email, name
        FROM users
        WHERE role = 'ADMIN'
        ORDER BY email
    LOOP
        -- Verificar se já tem vínculo com SIMP-ADMIN
        SELECT id INTO v_user_institution_id
        FROM user_institutions
        WHERE user_id = v_user_record.id
          AND institution_id = v_admin_institution_id;

        IF v_user_institution_id IS NULL THEN
            -- Criar vínculo
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
                v_user_record.id,
                v_admin_institution_id,
                true,
                CURRENT_TIMESTAMP,
                v_user_record.id, -- Self-linked
                CURRENT_TIMESTAMP
            )
            RETURNING id INTO v_user_institution_id;

            RAISE NOTICE '  ✓ Vínculo criado para: % (%)', v_user_record.name, v_user_record.email;
        ELSE
            RAISE NOTICE '  ⊳ Vínculo já existe para: % (%)', v_user_record.name, v_user_record.email;
        END IF;

        -- Adicionar role ADMIN se não existir
        INSERT INTO user_institution_roles (user_institution_id, role)
        VALUES (v_user_institution_id, 'ADMIN')
        ON CONFLICT (user_institution_id, role) DO NOTHING;

        -- Garantir que usuário está ACTIVE
        UPDATE users
        SET status = 'ACTIVE', updated_at = CURRENT_TIMESTAMP
        WHERE id = v_user_record.id
          AND status != 'ACTIVE';

        v_total_migrated := v_total_migrated + 1;
    END LOOP;

    RAISE NOTICE '';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Migração concluída!';
    RAISE NOTICE 'Total de admins migrados: %', v_total_migrated;
    RAISE NOTICE '========================================';
    RAISE NOTICE '';
    RAISE NOTICE 'Próximo passo:';
    RAISE NOTICE '  1. Validar que todos os admins foram migrados';
    RAISE NOTICE '  2. Executar a migration V6 para remover a coluna role';
    RAISE NOTICE '     mvn flyway:migrate';
    RAISE NOTICE '';

EXCEPTION
    WHEN OTHERS THEN
        RAISE NOTICE '';
        RAISE NOTICE '========================================';
        RAISE NOTICE 'ERRO durante migração!';
        RAISE NOTICE 'Mensagem: %', SQLERRM;
        RAISE NOTICE '========================================';
        RAISE;
END $$;

-- Validação: Mostrar admins e seus vínculos
SELECT
    u.email,
    u.name,
    u.role as old_role,
    u.status,
    i.acronym as institution,
    array_agg(DISTINCT uir.role::text) as institution_roles
FROM users u
LEFT JOIN user_institutions ui ON ui.user_id = u.id AND ui.active = true
LEFT JOIN institutions i ON i.id = ui.institution_id
LEFT JOIN user_institution_roles uir ON uir.user_institution_id = ui.id
WHERE u.role = 'ADMIN'
GROUP BY u.email, u.name, u.role, u.status, i.acronym
ORDER BY u.email;
