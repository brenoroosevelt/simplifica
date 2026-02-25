# Implementação: Importação de Unidades via CSV - Frontend

## Visão Geral

Implementação completa da funcionalidade de importação de unidades via CSV no frontend, seguindo o plano aprovado em `/home/breno/.claude/plans/iterative-purring-clock.md`.

## Arquivos Modificados/Criados

### 1. Types (`/frontend/src/types/unit.types.ts`)

**Modificado**: Adicionadas interfaces para importação CSV.

```typescript
export interface UnitImportError {
  lineNumber: number
  rowData: string
  errorMessage: string
  field?: string
}

export interface UnitImportResult {
  totalRows: number
  successCount: number
  failedCount: number
  errors: UnitImportError[]
  successfulUnits: string[]
}
```

### 2. Service (`/frontend/src/services/unit.service.ts`)

**Modificado**: Adicionado método para importação CSV.

```typescript
async importUnitsFromCsv(file: File): Promise<UnitImportResult> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await apiClient.post<UnitImportResult>(
    `${this.BASE_PATH}/import-csv`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }
  )

  return response.data
}
```

### 3. Componente (`/frontend/src/components/unit/UnitCsvImport.vue`)

**Criado**: Componente completo de importação CSV com todas as funcionalidades.

#### Funcionalidades Implementadas

1. **Dialog de Importação**
   - Botão "Importar CSV" na toolbar
   - Dialog responsivo (max-width: 800px)
   - Persistente durante upload

2. **Informações e Template**
   - Alert informativo sobre formato do CSV
   - Botão "Baixar Template CSV"
   - Template diferente para ADMIN e MANAGER:
     - MANAGER: `name,acronym,description,active`
     - ADMIN: `name,acronym,description,active,institutionId,institutionAcronym`

3. **Seleção e Preview de Arquivo**
   - File input com validação (.csv apenas)
   - Preview automático das primeiras 5 linhas
   - Exibição do total de linhas detectadas
   - Tabela formatada com headers e dados

4. **Upload e Processamento**
   - Progress bar durante importação
   - Desabilitar controles durante upload
   - Tratamento de erros robusto

5. **Exibição de Resultados**
   - **Sucesso Total**: Alert verde
   - **Sucesso Parcial**: Alert amarelo com estatísticas
   - **Falha Total**: Alert vermelho
   - Lista expansível de unidades importadas com sucesso
   - Lista expansível de erros com detalhes (linha, campo, mensagem)

6. **Relatório de Erros**
   - Botão "Baixar Relatório de Erros"
   - CSV formatado com colunas: Linha, Campo, Erro, Dados
   - Escape correto de quotes e caracteres especiais

7. **Integração com Auth Store**
   - Uso de `useAuthStore().isAdmin` para detectar role
   - Exibição condicional de coluna institutionId no template

### 4. View Principal (`/frontend/src/views/private/UnitsPage.vue`)

**Modificado**: Integração do componente de importação.

#### Alterações

1. Import do componente:
```typescript
import UnitCsvImport from '@/components/unit/UnitCsvImport.vue'
```

2. Adição no template (header actions):
```vue
<div class="d-flex" style="gap: 12px;">
  <UnitCsvImport @import-completed="loadUnits" />
  <v-btn color="primary" variant="flat" prepend-icon="mdi-plus" @click="openCreateDialog">
    Nova Unidade
  </v-btn>
</div>
```

3. Conexão do evento `@import-completed` para recarregar lista automaticamente.

## Fluxo de Usuário

### MANAGER (Gestor)

1. Acessa página de Unidades
2. Clica em "Importar CSV"
3. Clica em "Baixar Template CSV" (sem coluna institutionId)
4. Preenche CSV com dados das unidades
5. Seleciona arquivo no file input
6. Visualiza preview das primeiras 5 linhas
7. Clica em "Importar"
8. Acompanha progress bar
9. Visualiza resultado:
   - Lista de unidades importadas com sucesso
   - Lista de erros (se houver)
10. Se houver erros, clica em "Baixar Relatório de Erros"
11. Corrige erros no CSV
12. Re-importa apenas linhas com erro
13. Clica em "Fechar" (lista recarrega automaticamente)

### ADMIN (Administrador)

1. Acessa página de Unidades
2. Clica em "Importar CSV"
3. Clica em "Baixar Template CSV" (com colunas institutionId/institutionAcronym opcionais)
4. Preenche CSV, podendo especificar instituições diferentes
5. Seleciona arquivo no file input
6. Visualiza preview das primeiras 5 linhas
7. Clica em "Importar"
8. Acompanha progress bar
9. Visualiza resultado detalhado
10. Se houver erros, baixa relatório
11. Corrige e re-importa
12. Clica em "Fechar" (lista recarrega automaticamente)

## Características Técnicas

### Segurança
- Validação de tipo de arquivo (.csv)
- Tratamento de erros robusto
- Escape correto de caracteres especiais em CSV
- Sem execução de scripts no preview

### Performance
- Preview limitado a 5 linhas (performance)
- FileReader assíncrono
- Progress indicator durante upload
- Recarregamento automático apenas após sucesso

### UX/UI
- Componentes Vuetify 3
- Design consistente com resto da aplicação
- Feedback visual claro (cores, ícones)
- Alerts informativos
- Listas expansíveis para grandes volumes de dados
- Botões desabilitados durante operações

### Manutenibilidade
- Composition API (script setup)
- TypeScript strict
- Código limpo e bem documentado
- Separação de responsabilidades
- Funções auxiliares reutilizáveis

## Validações

### Client-Side
1. Arquivo deve ter extensão .csv
2. Arquivo não pode estar vazio
3. Preview mostra máximo 5 linhas
4. Template correto baseado em role (isAdmin)

### Server-Side (esperado pelo backend)
1. Validação de MIME type
2. Validação de tamanho (5MB)
3. Validação de headers obrigatórios
4. Validação de dados linha por linha
5. Multi-tenancy (MANAGER não pode alterar instituição)

## Integração com Backend

### Endpoint
- **URL**: `POST /units/import-csv`
- **Content-Type**: `multipart/form-data`
- **Parâmetro**: `file` (MultipartFile)
- **Response**: `UnitImportResult`

### Estrutura de Response
```typescript
{
  totalRows: number
  successCount: number
  failedCount: number
  errors: Array<{
    lineNumber: number
    rowData: string
    errorMessage: string
    field?: string
  }>
  successfulUnits: string[]
}
```

## Testes Recomendados

### Casos de Teste

1. **Sucesso Total**
   - Importar CSV válido com 3 unidades
   - Verificar lista recarregada
   - Verificar todas as 3 unidades criadas

2. **Sucesso Parcial**
   - Importar CSV com 2 válidas + 1 com acronym duplicado
   - Verificar 2 sucessos e 1 falha
   - Verificar erro exibido corretamente

3. **Falha Total**
   - Importar CSV com headers inválidos
   - Verificar erro imediato

4. **Template MANAGER**
   - Login como MANAGER
   - Baixar template
   - Verificar colunas: name,acronym,description,active

5. **Template ADMIN**
   - Login como ADMIN
   - Baixar template
   - Verificar colunas: name,acronym,description,active,institutionId,institutionAcronym

6. **Relatório de Erros**
   - Importar CSV com erros
   - Baixar relatório
   - Verificar formato CSV correto

7. **Preview**
   - Selecionar CSV com 10 linhas
   - Verificar preview mostra apenas 5 linhas
   - Verificar contador mostra 10 linhas

8. **Reload Automático**
   - Importar CSV com sucesso
   - Fechar dialog
   - Verificar lista recarregada automaticamente

## Próximos Passos

1. Testar integração com backend quando endpoint estiver implementado
2. Adicionar testes unitários para componente
3. Adicionar testes e2e para fluxo completo
4. Considerar adicionar validação de tamanho máximo client-side
5. Considerar adicionar loading skeleton no preview

## Notas

- Implementação segue padrão estabelecido em `ProcessMappingUpload.vue`
- Usa Composition API e TypeScript como resto do projeto
- Integrado com auth store para detecção de role
- Evento `@import-completed` permite reuso em outros contextos
- Código pronto para produção
