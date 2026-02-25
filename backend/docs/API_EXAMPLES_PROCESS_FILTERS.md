# Exemplos de Uso da API - Filtros de Processos

## Endpoint Base
```
GET /processes
```

## Headers Obrigatórios
```http
Authorization: Bearer {jwt_token}
X-Institution-Id: {institution_uuid}
```

## Parâmetros de Query

### Filtros Básicos (já existentes)
| Parâmetro | Tipo | Descrição | Exemplo |
|-----------|------|-----------|---------|
| `active` | Boolean | Filtrar por status ativo | `true`, `false` |
| `search` | String | Busca em nome E descrição (case-insensitive) | `"gestão"` |
| `valueChainId` | UUID | Filtrar por cadeia de valor | `"123e4567-e89b-12d3-a456-426614174000"` |
| `isCritical` | Boolean | Filtrar por processos críticos | `true`, `false` |

### Novos Filtros de Status
| Parâmetro | Tipo | Valores Válidos | Descrição |
|-----------|------|-----------------|-----------|
| `documentationStatus` | Enum | `DOCUMENTED`, `NOT_DOCUMENTED`, `DOCUMENTED_WITH_PENDING` | Status de documentação |
| `externalGuidanceStatus` | Enum | `AVAILABLE`, `NOT_AVAILABLE`, `AVAILABLE_WITH_PENDING`, `NOT_NECESSARY` | Status de orientação externa |
| `riskManagementStatus` | Enum | `PREPARED`, `NOT_PREPARED`, `PREPARED_WITH_PENDING` | Status de gestão de riscos |
| `mappingStatus` | Enum | `MAPPED`, `NOT_MAPPED`, `MAPPED_WITH_PENDING` | Status de mapeamento |

### Novos Filtros de Unidades
| Parâmetro | Tipo | Descrição | Exemplo |
|-----------|------|-----------|---------|
| `responsibleUnitId` | UUID | Filtrar por unidade responsável | `"456e7890-e89b-12d3-a456-426614174000"` |
| `directUnitId` | UUID | Filtrar por unidade direta | `"789e0123-e89b-12d3-a456-426614174000"` |

### Parâmetros de Paginação
| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| `page` | Integer | `0` | Número da página (zero-indexed) |
| `size` | Integer | `20` | Tamanho da página |
| `sort` | String | `"name"` | Campo para ordenação |
| `direction` | String | `"ASC"` | Direção da ordenação (`ASC` ou `DESC`) |

## Exemplos de Requisições

### Exemplo 1: Busca Simples
Busca processos com o termo "gestão" no nome OU na descrição.

```bash
curl -X GET "http://localhost:8080/processes?search=gestão" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "X-Institution-Id: 123e4567-e89b-12d3-a456-426614174000"
```

### Exemplo 2: Filtro por Status de Documentação
Lista apenas processos documentados.

```bash
curl -X GET "http://localhost:8080/processes?documentationStatus=DOCUMENTED" \
  -H "Authorization: Bearer {token}" \
  -H "X-Institution-Id: {uuid}"
```

### Exemplo 3: Filtro por Unidade Responsável
Lista processos de uma unidade específica.

```bash
curl -X GET "http://localhost:8080/processes?responsibleUnitId=456e7890-e89b-12d3-a456-426614174000" \
  -H "Authorization: Bearer {token}" \
  -H "X-Institution-Id: {uuid}"
```

### Exemplo 4: Múltiplos Filtros Combinados
Lista processos ativos, críticos, documentados, da unidade X, ordenados por nome.

```bash
curl -X GET "http://localhost:8080/processes?\
active=true&\
isCritical=true&\
documentationStatus=DOCUMENTED&\
responsibleUnitId=456e7890-e89b-12d3-a456-426614174000&\
sort=name&\
direction=ASC&\
page=0&\
size=20" \
  -H "Authorization: Bearer {token}" \
  -H "X-Institution-Id: {uuid}"
```

### Exemplo 5: Busca + Filtros de Status
Busca por "risco" em processos que estão preparados para gestão de riscos.

```bash
curl -X GET "http://localhost:8080/processes?\
search=risco&\
riskManagementStatus=PREPARED" \
  -H "Authorization: Bearer {token}" \
  -H "X-Institution-Id: {uuid}"
```

### Exemplo 6: Todos os Filtros Avançados
Exemplo completo usando todos os novos filtros.

```bash
curl -X GET "http://localhost:8080/processes?\
active=true&\
search=processos&\
valueChainId=111e1111-e89b-12d3-a456-426614174000&\
isCritical=false&\
documentationStatus=DOCUMENTED_WITH_PENDING&\
externalGuidanceStatus=AVAILABLE&\
riskManagementStatus=PREPARED&\
mappingStatus=MAPPED&\
responsibleUnitId=222e2222-e89b-12d3-a456-426614174000&\
directUnitId=333e3333-e89b-12d3-a456-426614174000&\
page=0&\
size=50&\
sort=name&\
direction=DESC" \
  -H "Authorization: Bearer {token}" \
  -H "X-Institution-Id: {uuid}"
```

## Formato de Resposta

```json
{
  "content": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "name": "Processo de Gestão de Riscos",
      "description": "Processo responsável pela gestão de riscos organizacionais",
      "active": true,
      "isCritical": true,
      "documentationStatus": "DOCUMENTED",
      "documentationUrl": "https://...",
      "externalGuidanceStatus": "AVAILABLE",
      "externalGuidanceUrl": "https://...",
      "riskManagementStatus": "PREPARED",
      "riskManagementUrl": "https://...",
      "mappingStatus": "MAPPED",
      "valueChain": {
        "id": "111e1111-e89b-12d3-a456-426614174000",
        "name": "Cadeia de Valor Principal",
        "description": "Descrição da cadeia"
      },
      "responsibleUnit": {
        "id": "222e2222-e89b-12d3-a456-426614174000",
        "name": "Diretoria de Riscos",
        "acronym": "DIR"
      },
      "directUnit": {
        "id": "333e3333-e89b-12d3-a456-426614174000",
        "name": "Gerência de Controle",
        "acronym": "GER"
      },
      "mappings": [],
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-01-20T15:45:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "size": 20,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false,
    "empty": false
  },
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

## Códigos de Status HTTP

| Código | Descrição |
|--------|-----------|
| `200 OK` | Requisição bem-sucedida |
| `400 Bad Request` | Parâmetros inválidos (enum inválido, UUID malformado, etc.) |
| `401 Unauthorized` | Token JWT ausente ou inválido |
| `403 Forbidden` | Usuário não tem permissão (não é MANAGER ou ADMIN) |
| `500 Internal Server Error` | Erro interno do servidor |

## Notas Importantes

### Multi-Tenant Isolation
Todos os filtros respeitam o isolamento por instituição. O `X-Institution-Id` é obrigatório e apenas processos da instituição especificada são retornados.

### Case-Insensitive Search
A busca por `search` é case-insensitive e usa `LIKE %term%` no banco de dados, buscando em:
- `name` (nome do processo)
- `description` (descrição do processo)

Exemplos:
- `search=gestão` encontra "Gestão de Riscos", "PROCESSO DE GESTÃO", "gestão financeira"
- `search=risco` encontra "Gestão de Riscos", "Análise de riscos", "RISCO OPERACIONAL"

### Null/Empty Parameters
Parâmetros não enviados ou com valor `null` são ignorados pelo filtro. Apenas parâmetros com valores válidos são aplicados.

### Enum Validation
Se um enum inválido for enviado, o Spring Boot retorna automaticamente `400 Bad Request` com mensagem de erro:
```json
{
  "timestamp": "2024-01-20T10:30:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Failed to convert value of type 'java.lang.String' to required type 'ProcessDocumentationStatus'; nested exception is...",
  "path": "/processes"
}
```

### Performance
- Todos os filtros usam índices de banco de dados quando disponíveis
- Eager loading é aplicado para evitar N+1 queries
- Paginação é obrigatória (não é possível buscar todos os registros sem paginação)

## JavaScript/TypeScript Examples

### Usando Fetch API
```javascript
const filters = {
  active: true,
  search: 'gestão',
  documentationStatus: 'DOCUMENTED',
  responsibleUnitId: '456e7890-e89b-12d3-a456-426614174000',
  page: 0,
  size: 20,
  sort: 'name',
  direction: 'ASC'
};

const queryString = new URLSearchParams(
  Object.entries(filters).filter(([_, v]) => v != null)
).toString();

const response = await fetch(`http://localhost:8080/processes?${queryString}`, {
  headers: {
    'Authorization': `Bearer ${token}`,
    'X-Institution-Id': institutionId,
    'Content-Type': 'application/json'
  }
});

const data = await response.json();
```

### Usando Axios
```javascript
import axios from 'axios';

const response = await axios.get('http://localhost:8080/processes', {
  params: {
    active: true,
    search: 'gestão',
    documentationStatus: 'DOCUMENTED',
    responsibleUnitId: '456e7890-e89b-12d3-a456-426614174000',
    page: 0,
    size: 20,
    sort: 'name',
    direction: 'ASC'
  },
  headers: {
    'Authorization': `Bearer ${token}`,
    'X-Institution-Id': institutionId
  }
});

const processes = response.data;
```

## Testes com Postman

### Collection Structure
```json
{
  "info": {
    "name": "Process Filters API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "List Processes - Basic",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{jwt_token}}",
            "type": "text"
          },
          {
            "key": "X-Institution-Id",
            "value": "{{institution_id}}",
            "type": "text"
          }
        ],
        "url": {
          "raw": "{{base_url}}/processes",
          "host": ["{{base_url}}"],
          "path": ["processes"]
        }
      }
    },
    {
      "name": "List Processes - With Filters",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{jwt_token}}",
            "type": "text"
          },
          {
            "key": "X-Institution-Id",
            "value": "{{institution_id}}",
            "type": "text"
          }
        ],
        "url": {
          "raw": "{{base_url}}/processes?active=true&documentationStatus=DOCUMENTED&responsibleUnitId={{unit_id}}",
          "host": ["{{base_url}}"],
          "path": ["processes"],
          "query": [
            {
              "key": "active",
              "value": "true"
            },
            {
              "key": "documentationStatus",
              "value": "DOCUMENTED"
            },
            {
              "key": "responsibleUnitId",
              "value": "{{unit_id}}"
            }
          ]
        }
      }
    }
  ]
}
```

## Changelog

### v1.1.0 (2026-02-11)
- Adicionada busca unificada em múltiplos campos (name OR description)
- Adicionados 4 filtros de status (documentationStatus, externalGuidanceStatus, riskManagementStatus, mappingStatus)
- Adicionados 2 filtros de unidades (responsibleUnitId, directUnitId)
- Todos os novos filtros são opcionais e mantêm backward compatibility
