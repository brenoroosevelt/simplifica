# Implementação de Importação de Unidades via CSV - Backend

## Resumo

Implementação completa da funcionalidade de importação em massa de unidades através de arquivos CSV, com suporte total a multi-tenancy e processamento com sucesso parcial.

## Arquivos Criados/Modificados

### 1. Dependências (pom.xml)
- ✅ Adicionada dependência `org.apache.commons:commons-csv:1.10.0`

### 2. Exceções
- ✅ **ValidationException.java** (criado)
  - Nova exceção para validação de dados de negócio
  - Handler adicionado no GlobalExceptionHandler
  - Retorna HTTP 400 Bad Request

### 3. DTOs (application/dto)
- ✅ **UnitImportRowDTO.java** (criado)
  - Representa uma linha do CSV
  - Campos: name, acronym, description, active, institutionId, institutionAcronym

- ✅ **ImportErrorDTO.java** (criado)
  - Representa um erro de importação
  - Campos: lineNumber, rowData, errorMessage, field

- ✅ **UnitImportResultDTO.java** (criado)
  - Resultado completo da importação
  - Estatísticas: totalRows, successCount, failedCount
  - Listas: errors, successfulUnits
  - Métodos helper: incrementSuccess(), incrementFailed(), addError(), addSuccessfulUnit()

### 4. Services (application/service)
- ✅ **CsvValidationService.java** (criado)
  - Validação de arquivo (tamanho max 5MB, extensão .csv, MIME type)
  - Parsing de CSV usando Apache Commons CSV
  - Validação de headers obrigatórios (name, acronym)
  - Configuração: primeira linha como header, trim de valores, ignora linhas vazias

- ✅ **UnitImportService.java** (criado)
  - Método principal: `importUnitsFromCsv(MultipartFile, UserPrincipal)`
  - Processamento linha por linha com captura de exceções
  - Resolução de instituição baseada em role:
    - MANAGER: sempre usa instituição do TenantContext
    - ADMIN: pode especificar institutionId ou institutionAcronym no CSV
  - Validações por linha:
    - Campos obrigatórios (name, acronym)
    - Tamanhos (name ≤ 255, acronym ≤ 50, description ≤ 5000)
    - Unicidade de acronym via UnitService.create()
    - Instituição ativa
  - Troca temporária de contexto para importar em diferentes instituições

### 5. Controller (presentation/controller)
- ✅ **UnitController.java** (modificado)
  - Novo endpoint: `POST /units/import-csv`
  - Autorização: `@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")`
  - Parâmetro: `@RequestParam("file") MultipartFile`
  - Injeta `@AuthenticationPrincipal UserPrincipal`
  - Retorna: `UnitImportResultDTO`

### 6. Exception Handler
- ✅ **GlobalExceptionHandler.java** (modificado)
  - Adicionado handler para ValidationException
  - Retorna HTTP 400 com mensagem de erro

## Arquitetura da Solução

### Multi-Tenancy
- **MANAGER**: Sempre usa instituição do `TenantContext.getCurrentInstitution()`
  - Não pode alterar instituição via CSV
  - Colunas institutionId e institutionAcronym são ignoradas

- **ADMIN**: Pode especificar instituição no CSV
  - Se institutionId fornecido: usa esse UUID
  - Se institutionAcronym fornecido: busca instituição pelo acronym
  - Se nenhum fornecido: usa instituição do TenantContext
  - Valida se instituição está ativa

### Processamento
- **Síncrono**: Resposta imediata (adequado para arquivos até ~1000 linhas)
- **Linha por linha**: Erros não bloqueiam processamento de outras linhas
- **Sem rollback completo**: Permite sucesso parcial
- **Transacional**: Toda a operação em uma transação, mas exceções são capturadas por linha

### Validações

#### Arquivo
- Tamanho máximo: 5MB
- Extensão: `.csv` obrigatória
- MIME type: `text/csv`, `application/csv`, ou `text/plain`

#### Headers
- Obrigatórios: `name`, `acronym`
- Opcionais: `description`, `active`, `institutionId`, `institutionAcronym`

#### Por Linha
- **name**: obrigatório, máximo 255 caracteres
- **acronym**: obrigatório, máximo 50 caracteres, único por instituição
- **description**: opcional, máximo 5000 caracteres
- **active**: opcional, padrão `true`, aceita `true/false`
- **institutionId**: opcional (apenas ADMIN), formato UUID válido
- **institutionAcronym**: opcional (apenas ADMIN), deve existir e estar ativa

### Formato CSV

#### Template para MANAGER
```csv
name,acronym,description,active
Unidade de Tecnologia,TI,Gestão de TI,true
Recursos Humanos,RH,Gestão de pessoas,true
```

#### Template para ADMIN
```csv
name,acronym,description,active,institutionId,institutionAcronym
Unidade de Tecnologia,TI,Gestão de TI,true,,INST-1
Recursos Humanos,RH,Gestão de pessoas,true,550e8400-e29b-41d4-a716-446655440000,
```

Nota: ADMIN pode fornecer institutionId OU institutionAcronym, ou nenhum (usa contexto atual).

## Segurança

### Validações de Segurança
1. **Role-based access**: Apenas MANAGER e ADMIN podem importar
2. **Tenant isolation**: MANAGER não pode alterar instituição
3. **File validation**: Tamanho, extensão, MIME type
4. **Input sanitization**: Trim de valores, validação de formatos
5. **Size limits**: Previne memory exhaustion

### Proteções
- Max file size: 5MB
- Encoding: UTF-8 forçado
- SQL injection: Uso de JPA/Hibernate com prepared statements
- Path traversal: Não há manipulação de filesystem além do upload temporário

## Tratamento de Erros

### Tipos de Erro

1. **Erro de Arquivo**
   - Arquivo vazio, extensão inválida, MIME type inválido
   - **Comportamento**: Rejeita upload imediatamente, nenhuma linha processada

2. **Erro de Header**
   - Falta colunas obrigatórias (name, acronym)
   - **Comportamento**: Rejeita CSV imediatamente, nenhuma linha processada

3. **Erro por Linha**
   - Validação falha, acronym duplicado, instituição inválida
   - **Comportamento**: Captura exceção, registra erro, continua próxima linha

### Resposta de Erro
```json
{
  "totalRows": 10,
  "successCount": 8,
  "failedCount": 2,
  "errors": [
    {
      "lineNumber": 3,
      "rowData": "Unidade X,UX,Description,true",
      "errorMessage": "Unit with acronym 'UX' already exists",
      "field": "acronym"
    }
  ],
  "successfulUnits": ["Unidade A", "Unidade B", ...]
}
```

## Fluxo de Execução

1. **Controller** recebe arquivo e UserPrincipal
2. **UnitImportService.importUnitsFromCsv()**
   - Valida arquivo (CsvValidationService)
   - Parseia CSV (CsvValidationService)
   - Obtém instituição padrão (TenantContext)
   - **Loop por linha:**
     - Parse linha → UnitImportRowDTO
     - Resolve instituição target (baseado em role)
     - Valida dados
     - Cria CreateUnitDTO
     - Troca contexto temporariamente se necessário
     - Chama UnitService.create()
     - Registra sucesso ou captura erro
   - Retorna UnitImportResultDTO
3. **Controller** retorna resultado (HTTP 200)

## Testes Sugeridos

### Teste 1: MANAGER - Importação Simples
- Login como MANAGER
- Upload CSV com 3 unidades válidas
- **Esperado**: 3 sucessos, 0 falhas, unidades criadas na instituição do MANAGER

### Teste 2: MANAGER - Tentativa de Mudar Instituição
- Login como MANAGER
- CSV com institutionId diferente da instituição do MANAGER
- **Esperado**: Campo ignorado, unidades criadas na instituição do MANAGER

### Teste 3: ADMIN - Múltiplas Instituições
- Login como ADMIN
- CSV com unidades para 3 instituições diferentes (via institutionAcronym)
- **Esperado**: Unidades criadas nas instituições corretas

### Teste 4: Sucesso Parcial
- CSV com 5 linhas: 3 válidas, 1 com acronym duplicado, 1 com name vazio
- **Esperado**: 3 sucessos, 2 falhas, lista de erros detalhada

### Teste 5: Validação de Arquivo
- Upload de arquivo .txt
- **Esperado**: HTTP 400, mensagem "File must be a CSV"

### Teste 6: Instituição Inativa
- ADMIN tenta importar para instituição inativa
- **Esperado**: Linha falha com "Institution is inactive"

## Endpoints

### POST /units/import-csv

**Autorização**: ROLE_MANAGER, ROLE_ADMIN

**Headers**:
```
Authorization: Bearer {jwt-token}
X-Institution-Id: {uuid}
Content-Type: multipart/form-data
```

**Body**:
```
file: (binary CSV file)
```

**Response 200 OK**:
```json
{
  "totalRows": 10,
  "successCount": 9,
  "failedCount": 1,
  "errors": [
    {
      "lineNumber": 5,
      "rowData": "...",
      "errorMessage": "Acronym already exists",
      "field": "acronym"
    }
  ],
  "successfulUnits": ["Unit A", "Unit B", ...]
}
```

**Response 400 Bad Request**:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "File must be a CSV (.csv extension)",
  "path": "/units/import-csv"
}
```

## Templates CSV

Templates de exemplo criados em:
- `/backend/docs/unit-import-template-manager.csv` (para MANAGER)
- `/backend/docs/unit-import-template-admin.csv` (para ADMIN)

## Dependências

- **Apache Commons CSV 1.10.0**: Parsing robusto de CSV
- **Spring Security**: Autorização via @PreAuthorize
- **Spring Transaction**: Gerenciamento de transação
- **Lombok**: Redução de boilerplate

## Considerações de Performance

- **Memória**: Parser lê arquivo completo na memória (limite 5MB previne problemas)
- **Banco de dados**: Cada linha é um INSERT separado (transacional)
- **Escala**: Adequado para importações pequenas/médias (~1000 linhas)
- **Melhoria futura**: Para volumes maiores, considerar processamento assíncrono com batch

## Próximos Passos

1. ✅ Backend implementado
2. ⏳ Frontend: Criar componente UnitCsvImport.vue
3. ⏳ Frontend: Integrar na view UnitsPage.vue
4. ⏳ Testes de integração
5. ⏳ Documentação para usuário final

## Compilação

Compilação testada com sucesso:
```bash
mvn clean compile -DskipTests
```

Resultado: **BUILD SUCCESS** ✅
