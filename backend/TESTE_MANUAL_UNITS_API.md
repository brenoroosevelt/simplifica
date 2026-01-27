# Guia de Teste Manual - Units API

## Pré-requisitos

1. Backend rodando na porta 8080
2. Banco de dados PostgreSQL configurado
3. Migration V8 executada (tabela units criada)
4. Postman ou curl instalado
5. Autenticação OAuth2 configurada

## Setup Inicial

### 1. Obter Token de Autenticação

```bash
# Login como GESTOR ou ADMIN
# Guarde o access_token retornado
```

### 2. Variáveis de Ambiente (Postman)

```
BASE_URL = http://localhost:8080
ACCESS_TOKEN = <seu_token_aqui>
INSTITUTION_ID = <uuid_da_instituicao>
```

---

## Cenário 1: Criar Unidade (POST /units)

### Request:

```http
POST {{BASE_URL}}/units
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: {{INSTITUTION_ID}}
Content-Type: application/json

{
  "name": "Tecnologia da Informação",
  "acronym": "TI",
  "description": "Departamento de TI responsável pela infraestrutura tecnológica",
  "active": true
}
```

### Validações Esperadas:

✅ **Status**: 201 Created
✅ **Response Body**:
```json
{
  "id": "uuid-gerado",
  "institutionId": "uuid-da-instituicao",
  "institutionName": "Nome da Instituição",
  "institutionAcronym": "SIGLA",
  "name": "Tecnologia da Informação",
  "acronym": "TI",
  "description": "Departamento de TI responsável pela infraestrutura tecnológica",
  "active": true,
  "createdAt": "2026-01-27T10:00:00",
  "updatedAt": "2026-01-27T10:00:00"
}
```

✅ **Logs no Console** (INFO):
```
Creating new unit 'TI' for institution: uuid-da-instituicao
Created unit with ID: uuid-gerado
```

---

## Cenário 2: Normalização de Acronym (UPPERCASE)

### Request:

```http
POST {{BASE_URL}}/units
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: {{INSTITUTION_ID}}
Content-Type: application/json

{
  "name": "Recursos Humanos",
  "acronym": "rh",
  "active": true
}
```

### Validações Esperadas:

✅ **Status**: 201 Created
✅ **Acronym na Response**: "RH" (uppercase, mesmo enviando "rh")
✅ **Banco de dados**: acronym salvo como "RH"

---

## Cenário 3: Validação de Acronym Duplicado

### Request 1 (primeira unidade):

```http
POST {{BASE_URL}}/units
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: {{INSTITUTION_ID}}
Content-Type: application/json

{
  "name": "Financeiro",
  "acronym": "FIN",
  "active": true
}
```

✅ **Status**: 201 Created

### Request 2 (tentar duplicar acronym):

```http
POST {{BASE_URL}}/units
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: {{INSTITUTION_ID}}
Content-Type: application/json

{
  "name": "Finanças",
  "acronym": "FIN",
  "active": true
}
```

### Validações Esperadas:

❌ **Status**: 409 Conflict
❌ **Response Body**:
```json
{
  "status": 409,
  "message": "Unit with acronym 'FIN' already exists",
  "timestamp": "2026-01-27T10:05:00"
}
```

---

## Cenário 4: Validação Bean Validation

### Request (acronym inválido):

```http
POST {{BASE_URL}}/units
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: {{INSTITUTION_ID}}
Content-Type: application/json

{
  "name": "Marketing",
  "acronym": "MKT@",
  "active": true
}
```

### Validações Esperadas:

❌ **Status**: 400 Bad Request
❌ **Response Body**:
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "acronym",
      "message": "Acronym must contain only uppercase letters, numbers, and hyphens"
    }
  ],
  "timestamp": "2026-01-27T10:07:00"
}
```

---

## Cenário 5: Listar Unidades (GET /units)

### Request:

```http
GET {{BASE_URL}}/units?page=0&size=20&sort=name,asc
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: {{INSTITUTION_ID}}
```

### Validações Esperadas:

✅ **Status**: 200 OK
✅ **Response Body** (Page<UnitDTO>):
```json
{
  "content": [
    {
      "id": "uuid-1",
      "institutionId": "uuid-instituicao",
      "institutionName": "Nome da Instituição",
      "institutionAcronym": "SIGLA",
      "name": "Financeiro",
      "acronym": "FIN",
      "active": true,
      "createdAt": "2026-01-27T10:00:00",
      "updatedAt": "2026-01-27T10:00:00"
    },
    {
      "id": "uuid-2",
      "institutionId": "uuid-instituicao",
      "institutionName": "Nome da Instituição",
      "institutionAcronym": "SIGLA",
      "name": "Recursos Humanos",
      "acronym": "RH",
      "active": true,
      "createdAt": "2026-01-27T10:02:00",
      "updatedAt": "2026-01-27T10:02:00"
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true
}
```

✅ **Filtro por instituição**: Apenas unidades da institution_id do header

---

## Cenário 6: Filtrar Unidades (GET /units?search=TI&active=true)

### Request:

```http
GET {{BASE_URL}}/units?search=TI&active=true&page=0&size=10
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: {{INSTITUTION_ID}}
```

### Validações Esperadas:

✅ **Status**: 200 OK
✅ **Filtro aplicado**: Apenas unidades com "TI" no nome ou sigla
✅ **Filtro active**: Apenas unidades ativas (active=true)
✅ **Response**: Busca case-insensitive ("Tecnologia da Informação" e "TI" encontrados)

---

## Cenário 7: Buscar Unidade por ID (GET /units/{id})

### Request:

```http
GET {{BASE_URL}}/units/{{UNIT_ID}}
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: {{INSTITUTION_ID}}
```

### Validações Esperadas:

✅ **Status**: 200 OK
✅ **Response Body**: UnitDTO completo
✅ **Validação tenant**: Só retorna se institution_id da unidade = institution_id do header

### Teste Cross-Tenant (CRÍTICO):

```http
GET {{BASE_URL}}/units/{{UNIT_ID_DE_OUTRA_INSTITUICAO}}
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: {{INSTITUTION_ID}}
```

❌ **Status**: 403 Forbidden
❌ **Response Body**:
```json
{
  "status": 403,
  "message": "You do not have permission to access this unit",
  "timestamp": "2026-01-27T10:10:00"
}
```

✅ **Log WARN**:
```
Unauthorized access attempt to unit uuid-outra-inst from institution uuid-atual
```

---

## Cenário 8: Atualizar Unidade (PUT /units/{id})

### Request:

```http
PUT {{BASE_URL}}/units/{{UNIT_ID}}
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: {{INSTITUTION_ID}}
Content-Type: application/json

{
  "name": "Tecnologia da Informação e Comunicação",
  "description": "Departamento de TIC - infraestrutura e comunicação",
  "active": true
}
```

### Validações Esperadas:

✅ **Status**: 200 OK
✅ **Response Body**: UnitDTO com campos atualizados
✅ **updatedAt**: Timestamp atualizado
✅ **acronym**: PERMANECE INALTERADO (imutável)

---

## Cenário 9: Tentar Alterar Acronym (PUT /units/{id})

### Request (enviando acronym no body):

```http
PUT {{BASE_URL}}/units/{{UNIT_ID}}
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: {{INSTITUTION_ID}}
Content-Type: application/json

{
  "name": "Tecnologia da Informação",
  "acronym": "TECH",
  "active": true
}
```

### Validações Esperadas:

✅ **Status**: 200 OK (não retorna erro)
✅ **acronym na Response**: PERMANECE O ORIGINAL (campo ignorado pelo UpdateUnitDTO)
✅ **Banco de dados**: acronym NÃO foi alterado

**Nota**: Acronym é imutável - mesmo enviando no body, UpdateUnitDTO não possui o campo.

---

## Cenário 10: Soft Delete (DELETE /units/{id})

### Request:

```http
DELETE {{BASE_URL}}/units/{{UNIT_ID}}
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: {{INSTITUTION_ID}}
```

### Validações Esperadas:

✅ **Status**: 204 No Content
✅ **Response Body**: Vazio
✅ **Banco de dados**: active=false (soft delete)
✅ **Dados preservados**: Unidade ainda existe na tabela

### Validação no Banco:

```sql
SELECT id, name, acronym, active
FROM units
WHERE id = 'uuid-da-unidade';
```

✅ **Resultado**: active = false, outros dados intactos

### Listar com Filtro Inativas:

```http
GET {{BASE_URL}}/units?active=false
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: {{INSTITUTION_ID}}
```

✅ **Status**: 200 OK
✅ **Response**: Contém a unidade deletada (soft delete)

---

## Cenário 11: Validação de Context Ausente

### Request (sem header X-Institution-Id):

```http
POST {{BASE_URL}}/units
Authorization: Bearer {{ACCESS_TOKEN}}
Content-Type: application/json

{
  "name": "Marketing",
  "acronym": "MKT",
  "active": true
}
```

### Validações Esperadas:

❌ **Status**: 400 Bad Request
❌ **Response Body**:
```json
{
  "status": 400,
  "message": "No institution context set. Please select an institution.",
  "timestamp": "2026-01-27T10:15:00"
}
```

---

## Cenário 12: Teste Multi-tenant com Múltiplas Instituições

### Setup:
1. Criar 2 instituições no banco: Inst A (uuid-a) e Inst B (uuid-b)
2. Login como ADMIN (acessa qualquer instituição via context)

### Teste 1: Criar unidade na Inst A

```http
POST {{BASE_URL}}/units
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: uuid-a
Content-Type: application/json

{
  "name": "Departamento TI",
  "acronym": "TI",
  "active": true
}
```

✅ **Status**: 201 Created
✅ **institutionId na Response**: uuid-a

### Teste 2: Criar unidade com mesmo acronym na Inst B

```http
POST {{BASE_URL}}/units
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: uuid-b
Content-Type: application/json

{
  "name": "Setor TI",
  "acronym": "TI",
  "active": true
}
```

✅ **Status**: 201 Created (SUCESSO - instituições diferentes)
✅ **institutionId na Response**: uuid-b
✅ **Acronym**: "TI" (mesmo da Inst A, mas OK pois são instituições diferentes)

### Teste 3: Listar unidades com context Inst A

```http
GET {{BASE_URL}}/units
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: uuid-a
```

✅ **Status**: 200 OK
✅ **Response**: Apenas unidade da Inst A ("Departamento TI")

### Teste 4: Listar unidades com context Inst B

```http
GET {{BASE_URL}}/units
Authorization: Bearer {{ACCESS_TOKEN}}
X-Institution-Id: uuid-b
```

✅ **Status**: 200 OK
✅ **Response**: Apenas unidade da Inst B ("Setor TI")

---

## Validação SQL (Banco de Dados)

### 1. Verificar UNIQUE Constraint

```sql
-- Tentar inserir manualmente acronym duplicado na mesma instituição
INSERT INTO units (id, institution_id, name, acronym, active, created_at, updated_at)
VALUES (uuid_generate_v4(), 'uuid-inst-a', 'Test', 'TI', true, now(), now());

-- Segunda inserção com mesmo acronym e institution_id
INSERT INTO units (id, institution_id, name, acronym, active, created_at, updated_at)
VALUES (uuid_generate_v4(), 'uuid-inst-a', 'Test 2', 'TI', true, now(), now());
```

❌ **Resultado Esperado**:
```
ERROR: duplicate key value violates unique constraint "uk_units_institution_acronym"
```

### 2. Verificar Normalização de Acronym

```sql
SELECT acronym FROM units WHERE name = 'Recursos Humanos';
```

✅ **Resultado Esperado**: "RH" (uppercase, mesmo enviando "rh")

### 3. Verificar Soft Delete

```sql
SELECT id, name, active
FROM units
WHERE name = 'Unidade Deletada';
```

✅ **Resultado Esperado**: active = false (dados preservados)

### 4. Verificar Trigger updated_at

```sql
-- Buscar updated_at antes da atualização
SELECT updated_at FROM units WHERE id = 'uuid-unidade';

-- Atualizar via API (PUT /units/{id})

-- Buscar updated_at depois da atualização
SELECT updated_at FROM units WHERE id = 'uuid-unidade';
```

✅ **Resultado Esperado**: updated_at mudou automaticamente

---

## Checklist de Validação Final

### Funcionalidades Básicas:
- [ ] POST /units cria unidade com sucesso
- [ ] GET /units lista unidades com paginação
- [ ] GET /units/{id} retorna unidade específica
- [ ] PUT /units/{id} atualiza unidade
- [ ] DELETE /units/{id} faz soft delete

### Validações:
- [ ] Acronym normalizado para UPPERCASE
- [ ] Acronym único por instituição
- [ ] Acronym duplicado retorna 409 Conflict
- [ ] Acronym com caracteres inválidos retorna 400 Bad Request
- [ ] Campo name obrigatório
- [ ] Tamanho máximo dos campos respeitado

### Imutabilidade:
- [ ] UpdateUnitDTO não aceita acronym
- [ ] PUT com acronym no body ignora o campo
- [ ] Acronym permanece inalterado após criação

### Multi-tenant:
- [ ] Unidade criada com institution_id do header
- [ ] Listagem filtra por institution_id automaticamente
- [ ] Acesso cross-tenant retorna 403 Forbidden
- [ ] Log WARN em tentativa não autorizada
- [ ] Mesma sigla pode existir em instituições diferentes

### Soft Delete:
- [ ] DELETE marca active=false
- [ ] Dados preservados no banco
- [ ] Filtro active=false retorna unidades inativas

### Segurança:
- [ ] Endpoints protegidos por @PreAuthorize
- [ ] Apenas MANAGER e ADMIN acessam
- [ ] TenantContext validado em todas operações
- [ ] validateTenantAccess chamado antes de modificações

### Performance:
- [ ] Paginação funciona corretamente
- [ ] Ordenação por sort param funciona
- [ ] Filtros (search, active) aplicados no banco
- [ ] Índices usados nas queries (verificar EXPLAIN)

---

## Troubleshooting

### Erro: "No institution context set"
**Causa**: Header X-Institution-Id não enviado
**Solução**: Adicionar header em todas requisições

### Erro: 403 Forbidden ao acessar unidade
**Causa**: Unidade pertence a outra instituição
**Solução**: Verificar institution_id da unidade vs header

### Erro: 409 Conflict ao criar unidade
**Causa**: Acronym já existe para esta instituição
**Solução**: Usar acronym diferente ou verificar se já foi criada

### Acronym não está em UPPERCASE
**Causa**: Normalização não funcionou
**Solução**: Verificar Unit.setAcronym() e UnitService.create()

### Soft delete não preserva dados
**Causa**: Hard delete implementado incorretamente
**Solução**: Verificar UnitService.delete() usa setActive(false)

---

## Logs para Monitorar

### Console Backend (INFO):
```
Creating new unit 'TI' for institution: uuid
Created unit with ID: uuid
Updating unit: uuid
Updated unit: uuid
Soft deleting unit: uuid
Unit uuid marked as inactive
```

### Console Backend (WARN):
```
Unauthorized access attempt to unit uuid from institution uuid
```

### Console Backend (DEBUG):
```
Finding unit by ID: uuid
Finding units for institution uuid with filters - active: true, search: TI
```

---

**Conclusão**: Este guia cobre todos os cenários críticos para validar a implementação da Units API. Execute todos os testes para garantir que o isolamento multi-tenant, validações e regras de negócio estão funcionando corretamente.
