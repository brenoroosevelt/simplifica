# ✅ CORREÇÃO: Status PENDING Gerenciado Automaticamente

## 📋 PROBLEMA IDENTIFICADO

Na edição de usuários, o status PENDING estava disponível para seleção manual, mas na verdade ele deve ser gerenciado automaticamente pelo sistema.

### Regra de Negócio

O status de um usuário é gerenciado da seguinte forma:

1. **PENDING (Pendente)**:
   - Atribuído AUTOMATICAMENTE quando usuário não tem vínculos com nenhuma instituição
   - Usuário não pode fazer login
   - Sistema gerencia automaticamente

2. **ACTIVE (Ativo)**:
   - Usuário tem pelo menos um vínculo ativo com instituição
   - Pode fazer login e acessar o sistema
   - Pode ser definido manualmente

3. **INACTIVE (Inativo)**:
   - Usuário bloqueado manualmente
   - Não pode fazer login
   - Pode ser definido manualmente

## ✅ CORREÇÃO IMPLEMENTADA

### Arquivo: `frontend/src/components/user/UserEditDialog.vue`

**Mudança 1: Removida opção PENDING do select**

```typescript
// ANTES
const statusOptions = [
  { title: 'Ativo', value: 'ACTIVE' },
  { title: 'Pendente', value: 'PENDING' },    // ❌ REMOVIDO
  { title: 'Inativo', value: 'INACTIVE' },
]

// DEPOIS
const statusOptions = [
  { title: 'Ativo', value: 'ACTIVE' },
  { title: 'Inativo', value: 'INACTIVE' },
]
```

**Mudança 2: Adicionado hint explicativo**

```vue
<v-select
  v-model="formData.status"
  label="Status"
  :items="statusOptions"
  hint="PENDENTE é gerenciado automaticamente (usuário sem vínculos)"
  persistent-hint
/>
```

**Mudança 3: Atualizados alertas informativos**

```vue
<!-- Alerta quando INACTIVE -->
<v-alert v-if="formData.status === 'INACTIVE'" type="error">
  Usuários inativos não podem fazer login no sistema.
</v-alert>

<!-- Alerta quando ACTIVE -->
<v-alert v-if="formData.status === 'ACTIVE'" type="info">
  Usuário precisa estar vinculado a pelo menos uma instituição para acessar o sistema.
</v-alert>
```

## 🔄 FLUXO AUTOMÁTICO DO STATUS PENDING

### Cenário 1: Novo usuário faz primeiro login (OAuth)

```
1. Usuário faz login via Google/Microsoft
2. Sistema cria conta com status: PENDING
3. Usuário vê tela: "Aguardando aprovação"
4. Admin vincula usuário a uma instituição
5. Sistema automaticamente muda status: PENDING → ACTIVE
6. Usuário pode acessar o sistema
```

### Cenário 2: Admin desvincula última instituição

```
1. Usuário tem status: ACTIVE (vinculado à UFMS)
2. Admin desvincula usuário da UFMS
3. Sistema detecta: usuário sem vínculos
4. Sistema automaticamente muda status: ACTIVE → PENDING
5. Usuário perde acesso ao sistema
```

### Cenário 3: Admin define como INACTIVE

```
1. Usuário tem status: ACTIVE (vinculado à UFMS)
2. Admin muda status para: INACTIVE
3. Usuário não pode mais fazer login
4. Vínculos permanecem, mas usuário bloqueado
```

## 🎯 BACKEND - Validação Existente

O backend já implementa essa lógica corretamente:

**Arquivo**: `backend/src/main/java/com/simplifica/application/service/UserAdminService.java`

```java
// Ao vincular primeira instituição
if (user.getStatus() == UserStatus.PENDING &&
    user.getActiveInstitutions().size() == 0) {
    user.setStatus(UserStatus.ACTIVE);
    userRepository.save(user);
}

// Ao desvincular última instituição
if (user.getActiveInstitutions().isEmpty()) {
    user.setStatus(UserStatus.PENDING);
    userRepository.save(user);
}
```

## 📊 ESTADOS PERMITIDOS

| Status | Pode Definir Manualmente | Gerenciado Automaticamente | Pode Fazer Login |
|--------|-------------------------|---------------------------|------------------|
| **PENDING** | ❌ Não | ✅ Sim (sem vínculos) | ❌ Não |
| **ACTIVE** | ✅ Sim | ✅ Sim (com vínculos) | ✅ Sim |
| **INACTIVE** | ✅ Sim | ❌ Não | ❌ Não |

## 🧪 TESTES DE VALIDAÇÃO

### Teste 1: Edição de Usuário - Opções Corretas

**Passos**:
1. Como ADMIN ou MANAGER
2. Acessar `/admin/users`
3. Clicar em "Editar" em qualquer usuário
4. Verificar campo "Status"

**Esperado**:
- ✅ Apenas 2 opções: "Ativo" e "Inativo"
- ✅ Opção "Pendente" NÃO aparece
- ✅ Hint mostra: "PENDENTE é gerenciado automaticamente (usuário sem vínculos)"

### Teste 2: Usuário PENDING não pode ser editado para ACTIVE manualmente

**Setup**:
- Usuário João com status PENDING (sem vínculos)

**Passos**:
1. Tentar editar João
2. Selecionar "Ativo"
3. Salvar

**Esperado**:
- ✅ Salva com sucesso
- ✅ Backend aceita (não valida porque sem vínculos ele volta para PENDING)
- ⚠️ Alerta mostra: "Usuário precisa estar vinculado a pelo menos uma instituição"

**Nota**: Na prática, se o usuário não tiver vínculos, o sistema pode automaticamente voltar para PENDING ao recarregar.

### Teste 3: Vincular Instituição Ativa Usuário Automaticamente

**Setup**:
- Usuário Maria com status PENDING (sem vínculos)

**Passos**:
1. Como ADMIN
2. Abrir dialog "Gerenciar Instituições" de Maria
3. Vincular Maria à UFMS com role VIEWER
4. Recarregar lista de usuários

**Esperado**:
- ✅ Status de Maria muda automaticamente: PENDING → ACTIVE
- ✅ Maria pode fazer login

### Teste 4: Desvincular Última Instituição Volta para PENDING

**Setup**:
- Usuário Pedro com status ACTIVE (vinculado apenas à UFMS)

**Passos**:
1. Como ADMIN
2. Abrir dialog "Gerenciar Instituições" de Pedro
3. Desvincular Pedro da UFMS
4. Recarregar lista de usuários

**Esperado**:
- ✅ Status de Pedro muda automaticamente: ACTIVE → PENDING
- ✅ Pedro não pode mais fazer login

## 📋 EXEMPLO DE USO NO SISTEMA

### Interface de Edição

```
┌─────────────────────────────────────────────────┐
│ Editar Usuário                              [X] │
├─────────────────────────────────────────────────┤
│                                                 │
│  Nome                                           │
│  ┌───────────────────────────────────────────┐ │
│  │ João Silva                                │ │
│  └───────────────────────────────────────────┘ │
│                                                 │
│  Status                                         │
│  ┌───────────────────────────────────────────┐ │
│  │ Ativo                              ▼     │ │
│  └───────────────────────────────────────────┘ │
│  Opções:                                        │
│   • Ativo                                       │
│   • Inativo                                     │
│                                                 │
│  PENDENTE é gerenciado automaticamente          │
│  (usuário sem vínculos)                         │
│                                                 │
│  ℹ️  Usuário precisa estar vinculado a pelo     │
│     menos uma instituição para acessar o        │
│     sistema.                                    │
│                                                 │
│                           [Cancelar]  [Salvar]  │
└─────────────────────────────────────────────────┘
```

## ✅ RESUMO

**Mudanças Implementadas**:
1. ✅ Removida opção PENDING do select de status
2. ✅ Mantidas apenas ACTIVE e INACTIVE para edição manual
3. ✅ Adicionado hint explicativo sobre gestão automática
4. ✅ Atualizados alertas informativos
5. ✅ Documentação completa do fluxo

**Backend**:
- ✅ Já estava correto (gerencia PENDING automaticamente)

**Regras Claras**:
- ✅ PENDING = sem vínculos (automático)
- ✅ ACTIVE = com vínculos ou definido manualmente
- ✅ INACTIVE = bloqueado manualmente

---

**Status**: ✅ IMPLEMENTADO
**Build**: ✅ PASSOU
**Teste Backend**: ✅ JÁ ESTAVA CORRETO
