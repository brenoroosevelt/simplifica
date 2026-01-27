# Correções de Segurança Multi-Tenant

## Resumo Executivo

Este documento descreve as correções implementadas para resolver problemas críticos de permissões e segregação multi-tenant no sistema. As alterações garantem que a role ADMIN só pode ser atribuída a usuários vinculados à instituição administrativa SIMP-ADMIN.

## Data da Implementação
26 de Janeiro de 2026

## Problemas Identificados

### Backend
1. **UserAdminService.updateUserRoles** - Não validava se a role ADMIN estava sendo atribuída apenas para a instituição SIMP-ADMIN
2. **UserAdminService.linkUserToInstitution** - Não validava se a role ADMIN estava sendo atribuída apenas para a instituição SIMP-ADMIN
3. **InstitutionController** - Endpoints requeriam apenas `@PreAuthorize("hasRole('ADMIN')")` mas a validação de ADMIN no frontend estava incorreta

### Frontend
1. **auth.store.ts** - `isAdmin` verificava se o usuário tinha role ADMIN em QUALQUER instituição, mas deveria verificar apenas na SIMP-ADMIN
2. **UserRolesDialog.vue** - Não impedia a seleção da role ADMIN para instituições que não fossem SIMP-ADMIN

## Alterações Implementadas

### 1. Backend - Classe de Constantes

**Arquivo**: `/backend/src/main/java/com/simplifica/domain/constants/InstitutionConstants.java`

**Status**: Criado

**Descrição**: Nova classe para centralizar constantes relacionadas a instituições.

```java
public final class InstitutionConstants {
    public static final String ADMIN_INSTITUTION_ACRONYM = "SIMP-ADMIN";
}
```

**Justificativa**: Centralizar a definição da instituição administrativa evita hardcoding em múltiplos lugares e facilita manutenção futura.

### 2. Backend - UserAdminService.updateUserRoles

**Arquivo**: `/backend/src/main/java/com/simplifica/application/service/UserAdminService.java`

**Status**: Modificado

**Alteração**: Adicionada validação antes de atualizar roles de usuário.

```java
// Validate that ADMIN role can only be assigned to SIMP-ADMIN institution
if (request.getRoles() != null && request.getRoles().contains(InstitutionRole.ADMIN)) {
    if (!InstitutionConstants.ADMIN_INSTITUTION_ACRONYM.equals(institution.getAcronym())) {
        LOGGER.warn("Attempt to assign ADMIN role to non-admin institution: {} (acronym: {})",
                   institution.getName(), institution.getAcronym());
        throw new BadRequestException(
            "ADMIN role can only be assigned to users in the " +
            InstitutionConstants.ADMIN_INSTITUTION_ACRONYM + " institution");
    }
}
```

**Impacto**:
- Previne atribuição incorreta de role ADMIN
- Loga tentativas de atribuição incorreta para auditoria
- Lança exceção clara para o cliente

### 3. Backend - UserAdminService.linkUserToInstitution

**Arquivo**: `/backend/src/main/java/com/simplifica/application/service/UserAdminService.java`

**Status**: Modificado

**Alteração**: Adicionada validação antes de criar vínculo de usuário com instituição.

```java
// Validate that ADMIN role can only be assigned to SIMP-ADMIN institution
if (request.getRoles() != null && request.getRoles().contains(InstitutionRole.ADMIN)) {
    if (!InstitutionConstants.ADMIN_INSTITUTION_ACRONYM.equals(institution.getAcronym())) {
        LOGGER.warn("Attempt to link user with ADMIN role to non-admin institution: {} (acronym: {})",
                   institution.getName(), institution.getAcronym());
        throw new BadRequestException(
            "ADMIN role can only be assigned to users in the " +
            InstitutionConstants.ADMIN_INSTITUTION_ACRONYM + " institution");
    }
}
```

**Impacto**:
- Previne criação de vínculos com role ADMIN em instituições incorretas
- Loga tentativas para auditoria
- Lança exceção clara para o cliente

### 4. Frontend - auth.store.ts

**Arquivo**: `/frontend/src/stores/auth.store.ts`

**Status**: Modificado

**Alteração**: Corrigida a computed `isAdmin` para verificar apenas a instituição SIMP-ADMIN.

**Antes**:
```typescript
const isAdmin = computed(() => {
  if (!institutions.value || !Array.isArray(institutions.value) || institutions.value.length === 0) {
    return false
  }
  return institutions.value.some(ui => ui.roles && ui.roles.includes('ADMIN' as UserInstitutionRole))
})
```

**Depois**:
```typescript
// Verifica se o usuário tem role ADMIN na instituição SIMP-ADMIN
// IMPORTANTE: ADMIN role só é válida na instituição administrativa SIMP-ADMIN
const isAdmin = computed(() => {
  if (!institutions.value || !Array.isArray(institutions.value) || institutions.value.length === 0) {
    return false
  }
  // Admin apenas se tiver role ADMIN especificamente na instituição SIMP-ADMIN
  return institutions.value.some(ui =>
    ui.institution?.acronym === 'SIMP-ADMIN' &&
    ui.roles &&
    ui.roles.includes('ADMIN' as UserInstitutionRole)
  )
})
```

**Impacto**:
- Corrige verificação de permissão administrativa no frontend
- Impede que usuários com role ADMIN em outras instituições tenham acesso administrativo
- Garante segregação correta de funcionalidades administrativas

### 5. Frontend - UserRolesDialog.vue

**Arquivo**: `/frontend/src/components/user/UserRolesDialog.vue`

**Status**: Modificado

**Alterações**:

1. Adicionadas computed properties para verificar se a instituição selecionada permite role ADMIN:

```typescript
const selectedInstitution = computed(() => {
  if (!formData.value.institutionId) return null
  return props.institutions.find((ui) => ui.institutionId === formData.value.institutionId)
})

const isAdminInstitution = computed(() => {
  return selectedInstitution.value?.institution?.acronym === 'SIMP-ADMIN'
})

const isAdminRoleDisabled = computed(() => {
  return !isAdminInstitution.value
})

const availableRoleOptions = computed(() => {
  return roleOptions.map((role) => ({
    ...role,
    disabled: role.value === 'ADMIN' && isAdminRoleDisabled.value,
  }))
})
```

2. Modificado o template para usar `availableRoleOptions` e mostrar alerta quando ADMIN está desabilitado:

```vue
<v-chip
  v-for="role in availableRoleOptions"
  :key="role.value"
  :value="role.value"
  :disabled="role.disabled"
  filter
  variant="outlined"
>
```

```vue
<v-alert
  v-if="isAdminRoleDisabled"
  type="info"
  variant="tonal"
  density="compact"
  class="mt-2"
>
  O papel de Administrador só pode ser atribuído na instituição SIMP-ADMIN
</v-alert>
```

3. Adicionado watch para remover automaticamente a role ADMIN se o usuário trocar para uma instituição não-administrativa:

```typescript
watch(
  () => formData.value.institutionId,
  (institutionId) => {
    if (institutionId) {
      // ... código existente ...

      // Se mudou para uma instituição que não é SIMP-ADMIN, remover role ADMIN se presente
      const institution = props.institutions.find((ui) => ui.institutionId === institutionId)
      if (institution?.institution?.acronym !== 'SIMP-ADMIN') {
        formData.value.roles = formData.value.roles.filter((role) => role !== 'ADMIN')
      }
    }
  }
)
```

**Impacto**:
- Melhora UX ao desabilitar visualmente a opção ADMIN quando inapropriada
- Previne tentativas de atribuição incorreta no frontend
- Fornece feedback claro ao usuário sobre a restrição

### 6. Testes - UserAdminServiceSecurityTest

**Arquivo**: `/backend/src/test/java/com/simplifica/unit/service/UserAdminServiceSecurityTest.java`

**Status**: Criado

**Descrição**: Suite completa de testes unitários para validar as regras de segurança.

**Testes Incluídos**:

1. `shouldAllowAdminRoleInAdminInstitution_WhenUpdatingRoles` - Verifica que ADMIN pode ser atribuída na SIMP-ADMIN ao atualizar roles
2. `shouldRejectAdminRoleInRegularInstitution_WhenUpdatingRoles` - Verifica que ADMIN é rejeitada em outras instituições ao atualizar roles
3. `shouldAllowAdminRoleInAdminInstitution_WhenLinkingUser` - Verifica que ADMIN pode ser atribuída na SIMP-ADMIN ao criar vínculo
4. `shouldRejectAdminRoleInRegularInstitution_WhenLinkingUser` - Verifica que ADMIN é rejeitada em outras instituições ao criar vínculo
5. `shouldAllowManagerAndViewerRolesInRegularInstitution` - Verifica que outras roles funcionam normalmente

**Resultado dos Testes**:
```
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

**Todos os testes passaram com sucesso.**

## Verificação de Compatibilidade

### Testes Gerais do Backend

Executados todos os testes existentes para garantir que nenhuma funcionalidade foi quebrada:

```
Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Compilação

Backend compilado com sucesso sem erros ou warnings de compilação.

## Comportamento Esperado Após Correções

### Cenários Válidos

1. **Atribuir ADMIN na SIMP-ADMIN**:
   - Backend: Permite a operação
   - Frontend: Opção ADMIN habilitada
   - Resultado: Sucesso

2. **Atribuir MANAGER/VIEWER em qualquer instituição**:
   - Backend: Permite a operação
   - Frontend: Opções habilitadas
   - Resultado: Sucesso

### Cenários Bloqueados

1. **Tentar atribuir ADMIN em instituição não-administrativa via Backend**:
   - Backend: Retorna `400 Bad Request` com mensagem clara
   - Log: Registra tentativa para auditoria
   - Resultado: Operação bloqueada

2. **Tentar atribuir ADMIN em instituição não-administrativa via Frontend**:
   - Frontend: Opção ADMIN desabilitada
   - Frontend: Mostra alerta informativo
   - Frontend: Remove automaticamente ADMIN se trocar de instituição
   - Resultado: Prevenção proativa

## Logs de Auditoria

Todas as tentativas de atribuição incorreta de role ADMIN são logadas com nível WARN:

```
WARN com.simplifica.application.service.UserAdminService -- Attempt to assign ADMIN role to non-admin institution: Regular University (acronym: REG-UNIV)
```

```
WARN com.simplifica.application.service.UserAdminService -- Attempt to link user with ADMIN role to non-admin institution: Regular University (acronym: REG-UNIV)
```

## Segurança

### Defesa em Profundidade

As correções implementam múltiplas camadas de segurança:

1. **Frontend (UX)**: Previne tentativas através da interface
2. **Backend (Validação)**: Bloqueia tentativas via API
3. **Auditoria (Logs)**: Registra todas as tentativas

### Princípios Aplicados

- **Fail-Safe Defaults**: Por padrão, ADMIN é negada
- **Least Privilege**: ADMIN apenas onde estritamente necessário
- **Defense in Depth**: Múltiplas camadas de validação
- **Audit Trail**: Registro de tentativas de violação

## Compatibilidade

### Backward Compatibility

Todas as alterações são 100% compatíveis com o código existente:
- Nenhuma API pública foi alterada
- Novos campos são validações adicionais
- Testes existentes continuam passando

### Database

Nenhuma alteração no schema do banco de dados foi necessária.

## Próximos Passos Recomendados

1. **Revisão de Dados Existentes**: Verificar se existem usuários com role ADMIN em instituições incorretas e corrigi-los
2. **Dashboard de Auditoria**: Implementar dashboard para visualizar logs de tentativas de violação
3. **Notificações**: Considerar notificar administradores sobre tentativas de violação de segurança
4. **Documentação**: Atualizar documentação de usuário sobre restrições de roles

## Conclusão

Todas as correções foram implementadas com sucesso, testadas e validadas. O sistema agora possui validações adequadas de segregação multi-tenant, garantindo que a role ADMIN só pode ser atribuída a usuários da instituição SIMP-ADMIN.

As alterações seguem as melhores práticas de segurança e engenharia de software, mantendo compatibilidade total com o sistema existente.

## Arquivos Modificados

### Backend
- `/backend/src/main/java/com/simplifica/domain/constants/InstitutionConstants.java` (NOVO)
- `/backend/src/main/java/com/simplifica/application/service/UserAdminService.java` (MODIFICADO)
- `/backend/src/test/java/com/simplifica/unit/service/UserAdminServiceSecurityTest.java` (NOVO)

### Frontend
- `/frontend/src/stores/auth.store.ts` (MODIFICADO)
- `/frontend/src/components/user/UserRolesDialog.vue` (MODIFICADO)

## Autores
- Claude Code (Anthropic)
- Data: 26/01/2026
