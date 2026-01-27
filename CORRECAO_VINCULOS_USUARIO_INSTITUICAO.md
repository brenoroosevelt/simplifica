# Correção: Vínculos de Usuário com Instituição

## Problema Reportado

1. Os vínculos de usuário com instituição não estavam vindo corretos
2. Ao tentar vincular, o sistema retornava erro "User is already linked to this institution" mesmo quando o vínculo estava inativo

## Causa Raiz

O método `linkUserToInstitution` em `UserAdminService.java` estava usando `existsByUserIdAndInstitutionId` para verificar se já existia um vínculo. Este método retorna `true` mesmo para vínculos inativos (soft deleted), causando dois problemas:

1. **Falso positivo**: Quando um vínculo era desativado (soft delete) e o admin tentava vincular novamente, o sistema recusava dizendo que já estava vinculado
2. **Violação de constraint**: Se o admin forçasse a criação de um novo vínculo, violaria a constraint única `@UniqueConstraint(columnNames = {"user_id", "institution_id"})` na tabela `user_institutions`

## Solução Implementada

### 1. Verificação Correta de Vínculos Ativos

**Antes:**
```java
// Check if link already exists
if (userInstitutionRepository.existsByUserIdAndInstitutionId(
        userId, request.getInstitutionId())) {
    throw new BadRequestException("User is already linked to this institution");
}

UserInstitution userInstitution = UserInstitution.builder()
        .user(user)
        .institution(institution)
        .roles(request.getRoles())
        .linkedBy(linkedBy)
        .active(true)
        .build();
```

**Depois:**
```java
// Check if link already exists (active or inactive)
Optional<UserInstitution> existingLink = userInstitutionRepository
        .findByUserIdAndInstitutionId(userId, request.getInstitutionId());

UserInstitution userInstitution;
if (existingLink.isPresent()) {
    userInstitution = existingLink.get();

    if (userInstitution.getActive()) {
        // Link is already active
        throw new BadRequestException("User is already linked to this institution");
    }

    // Reactivate inactive link
    LOGGER.info("Reactivating existing inactive link for user {} and institution {}",
               userId, request.getInstitutionId());
    userInstitution.reactivate();
    userInstitution.setRoles(request.getRoles());
    userInstitution.setLinkedBy(linkedBy);
} else {
    // Create new link
    userInstitution = UserInstitution.builder()
            .user(user)
            .institution(institution)
            .roles(request.getRoles())
            .linkedBy(linkedBy)
            .active(true)
            .build();
}
```

### 2. Adicionado Import do Optional

Adicionado import necessário:
```java
import java.util.Optional;
```

## Comportamento Correto Após a Correção

### Cenário 1: Vínculo Não Existe
- Sistema cria novo vínculo normalmente

### Cenário 2: Vínculo Ativo Existe
- Sistema retorna erro "User is already linked to this institution"
- Comportamento esperado e correto

### Cenário 3: Vínculo Inativo Existe (NOVO)
- Sistema **reativa** o vínculo existente ao invés de criar um novo
- Atualiza roles com os novos valores
- Atualiza linkedBy para o admin que está reativando
- Mantém o histórico do vínculo original (linkedAt e id permanecem os mesmos)

## Benefícios da Solução

1. **Reutilização de Vínculos**: Vínculos inativos são reativados ao invés de criar duplicatas
2. **Preservação de Histórico**: O histórico de linkedAt é mantido
3. **Sem Violação de Constraints**: Não tenta criar vínculos duplicados que violariam a constraint única
4. **Flexibilidade**: Permite reativar usuários que foram previamente desvinculados

## Arquivos Modificados

- `backend/src/main/java/com/simplifica/application/service/UserAdminService.java` (linhas 336-365)

## Testes Recomendados

1. **Teste de Vinculação Normal**
   - Vincular usuário sem vínculo prévio
   - Esperado: Sucesso

2. **Teste de Vínculo Duplicado Ativo**
   - Tentar vincular usuário já vinculado ativamente
   - Esperado: Erro "User is already linked to this institution"

3. **Teste de Reativação**
   - Desvincular usuário (soft delete)
   - Vincular novamente o mesmo usuário à mesma instituição
   - Esperado: Vínculo reativado com sucesso, sem erro

4. **Teste de Consulta de Vínculos**
   - Buscar usuário por ID
   - Esperado: Retornar apenas vínculos ativos no campo `institutions`

## Status

✅ Correção implementada e compilada com sucesso
⏳ Aguardando testes em ambiente de desenvolvimento/produção
