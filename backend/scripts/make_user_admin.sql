-- ====================================
-- Script: Tornar usuário ADMIN do Sistema
-- ====================================
-- Este script ativa um usuário e vincula à instituição
-- SIMP-ADMIN (Administração Simplifica) com papel ADMIN.
--
-- Nota: Não existe mais "role global" - administradores
-- do sistema são identificados por terem role ADMIN na
-- instituição SIMP-ADMIN.
--
-- Usuário: breno.roosevelt@gmail.com
-- ====================================

DO $$
DECLARE
    v_user_id UUID;
    v_institution_id UUID;
    v_user_institution_id UUID;
BEGIN
    -- Buscar ID do usuário pelo email
    SELECT id INTO v_user_id
    FROM users
    WHERE email = 'breno.roosevelt@gmail.com';

    -- Verificar se usuário existe
    IF v_user_id IS NULL THEN
        RAISE NOTICE 'ERRO: Usuário breno.roosevelt@gmail.com não encontrado!';
        RAISE NOTICE 'O usuário precisa fazer login primeiro via OAuth para ser criado no sistema.';
        RETURN;
    END IF;

    RAISE NOTICE 'Usuário encontrado: %', v_user_id;

    -- Buscar ID da instituição 1 (Administração Simplifica)
    SELECT id INTO v_institution_id
    FROM institutions
    WHERE acronym = 'SIMP-ADMIN'
    LIMIT 1;

    IF v_institution_id IS NULL THEN
        RAISE NOTICE 'ERRO: Instituição SIMP-ADMIN não encontrada!';
        RETURN;
    END IF;

    RAISE NOTICE 'Instituição encontrada: %', v_institution_id;

    -- 1. Ativar usuário (não há mais role global)
    UPDATE users
    SET
        status = 'ACTIVE',
        updated_at = CURRENT_TIMESTAMP
    WHERE id = v_user_id;

    RAISE NOTICE 'Usuário ativado: status=ACTIVE';

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
            v_user_id, -- Self-linked
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
    RAISE NOTICE 'SUCESSO! Usuário breno.roosevelt@gmail.com agora é:';
    RAISE NOTICE '- Status: ACTIVE';
    RAISE NOTICE '- Instituição: SIMP-ADMIN (Administração Simplifica)';
    RAISE NOTICE '- Papel na Instituição: ADMIN';
    RAISE NOTICE '- Vínculo: ATIVO';
    RAISE NOTICE '========================================';

END $$;
