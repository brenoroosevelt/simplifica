# Correção: Desvinculamento de Instituição não Removia o Vínculo

## Problema Reportado

O usuário reportou que ao desvincular um usuário de uma instituição:
- O frontend mostrava mensagem de sucesso
- Após refresh (F5), a instituição ainda aparecia vinculada
- O backend não estava removendo o vínculo de fato

## Causa Raiz

O bug estava no DTO `UserDetailDTO.fromEntity()` (linha 74), que não filtrava instituições inativas ao mapear a entidade `User` para o DTO de resposta:

```java
// ANTES (BUGGY)
List<UserInstitutionDetailDTO> institutions = user.getInstitutions().stream()
        .map(UserDetailDTO::mapUserInstitution)
        .collect(Collectors.toList());
```

### Por que isso acontecia?

1. O método `unlinkUserFromInstitution` no `UserAdminService` estava funcionando corretamente:
   - Marcava o vínculo como `active = false` (soft delete)
   - Salvava no banco de dados

2. O problema era que o DTO retornava **TODAS** as instituições (ativas e inativas)

3. Quando o frontend buscava os detalhes do usuário após desvincular, recebia tanto instituições ativas quanto inativas

4. O frontend não verificava o campo `active` e exibia todas as instituições retornadas

## Solução Implementada

### Backend - Correções Realizadas

#### 1. Correção do DTO (Problema Principal)

Adicionado filtro no `UserDetailDTO.fromEntity()` para retornar apenas instituições ativas:

```java
// DEPOIS (CORRIGIDO)
List<UserInstitutionDetailDTO> institutions = user.getInstitutions().stream()
        .filter(UserInstitution::isActive)  // BUGFIX: Filter only ACTIVE institutions
        .map(UserDetailDTO::mapUserInstitution)
        .collect(Collectors.toList());
```

**Arquivo modificado:**
- `backend/src/main/java/com/simplifica/application/dto/UserDetailDTO.java` (linha 74)

#### 2. Correção da Query JPQL (Problema de Sintaxe)

Durante a tentativa de subir o backend, foi identificado erro na query `findByIdWithInstitutions`:

```
Could not create query for public abstract java.util.Optional
com.simplifica.infrastructure.repository.UserRepository.findByIdWithInstitutions(java.util.UUID)
```

**Problema:** A sintaxe `LEFT JOIN FETCH ... ON` não é válida em JPQL. O `ON` não é suportado com `JOIN FETCH`.

**Correção aplicada:**

```java
// ANTES (INVÁLIDO)
@Query("SELECT DISTINCT u FROM User u "
     + "LEFT JOIN FETCH u.institutions ui ON ui.active = true "  // ❌ ON não funciona com FETCH
     + "LEFT JOIN FETCH ui.institution "
     + "LEFT JOIN FETCH ui.linkedBy "
     + "WHERE u.id = :id")

// DEPOIS (VÁLIDO)
@Query("SELECT DISTINCT u FROM User u "
     + "LEFT JOIN FETCH u.institutions ui "  // ✅ Carrega todas
     + "LEFT JOIN FETCH ui.institution "
     + "LEFT JOIN FETCH ui.linkedBy "
     + "WHERE u.id = :id")
```

**Explicação:** Agora a query carrega TODAS as instituições (ativas e inativas), e o filtro é aplicado na camada DTO conforme a correção #1 acima.

**Arquivo modificado:**
- `backend/src/main/java/com/simplifica/infrastructure/repository/UserRepository.java` (linhas 60-65)

### Testes Criados

#### 1. Teste de Unidade do Service (`UserAdminServiceUnlinkTest.java`)

Criado teste completo para validar o comportamento do método `unlinkUserFromInstitution`:

- ✅ Deve desvincular com sucesso
- ✅ Deve lançar exceção quando vínculo não existe
- ✅ Deve mudar status do usuário para PENDING quando última instituição é removida
- ✅ Não deve mudar status quando outras instituições ativas permanecem
- ✅ Deve lidar com desvinculamento de vínculo já inativo
- ✅ Deve persistir o flag `active` corretamente

**Resultado:** 6/6 testes passando ✅

#### 2. Teste de Regressão do DTO (`UserDetailDTOTest.java`)

Criado teste específico para o bug corrigido:

- ✅ Deve retornar apenas instituições ATIVAS
- ✅ Deve retornar lista vazia quando todas instituições estão inativas
- ✅ Deve lidar com usuário sem instituições
- ✅ Deve mapear corretamente detalhes de instituição ativa
- ✅ **TESTE DE REGRESSÃO:** Não deve retornar instituição desvinculada após desativação

**Resultado:** 5/5 testes passando ✅

### Execução de Todos os Testes

```bash
mvn test
```

**Resultado Final:** 34/34 testes passando ✅

## Impacto

### Comportamento Anterior (Buggy)
1. Admin desvinculava usuário de instituição
2. Backend marcava vínculo como inativo (`active = false`)
3. Requisição de detalhes do usuário retornava instituições ativas E inativas
4. Frontend exibia todas, dando impressão que não desvinculou

### Comportamento Atual (Corrigido)
1. Admin desvincula usuário de instituição
2. Backend marca vínculo como inativo (`active = false`)
3. Requisição de detalhes do usuário retorna **apenas** instituições ativas
4. Frontend exibe apenas vínculos ativos ✅

## Arquivos Modificados

```
backend/src/main/java/com/simplifica/application/dto/UserDetailDTO.java
backend/src/main/java/com/simplifica/infrastructure/repository/UserRepository.java
```

## Arquivos de Teste Criados

```
backend/src/test/java/com/simplifica/unit/service/UserAdminServiceUnlinkTest.java
backend/src/test/java/com/simplifica/unit/dto/UserDetailDTOTest.java
```

## Validação

Para validar a correção em ambiente de desenvolvimento:

1. **Backend:** Reiniciar a aplicação
   ```bash
   mvn spring-boot:run
   ```

2. **Frontend:** Testar o fluxo:
   - Acessar gestão de usuários como ADMIN
   - Abrir diálogo de gerenciar instituições de um usuário
   - Desvincular uma instituição
   - ✅ Verificar que instituição some imediatamente da lista
   - ✅ Fazer refresh da página
   - ✅ Verificar que instituição permanece desvinculada

## Notas Técnicas

- O desvinculamento é um **soft delete** (marca `active = false`)
- Não remove fisicamente o registro do banco de dados
- Mantém histórico de vínculos para auditoria
- Todos os métodos de consulta devem filtrar por `active = true`

## Prevenção de Regressão

Os testes criados garantem que:
1. O método de desvinculamento funciona corretamente
2. O DTO filtra apenas instituições ativas
3. Cenário exato do bug está coberto por teste de regressão

Se alguém remover o filtro `.filter(UserInstitution::isActive)` no futuro, o teste `regressionTest_shouldNotReturnUnlinkedInstitution()` irá falhar imediatamente.
