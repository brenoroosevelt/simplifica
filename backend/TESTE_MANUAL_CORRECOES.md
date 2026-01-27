# Teste Manual - Validação das Correções Feature 004

**Data**: 2026-01-27

## Setup

```bash
# Terminal 1 - Backend
cd backend
mvn spring-boot:run

# Terminal 2 - Frontend
cd frontend
npm run dev

# Terminal 3 - Testes cURL
cd backend
```

---

## 🔍 Teste 1: Pattern Validation com Lowercase

### ANTES da correção:
```bash
# Backend rejeitava "ti" (lowercase)
curl -X POST "http://localhost:8080/units" \
  -H "Content-Type: application/json" \
  -H "X-Institution-Id: [UUID_INSTITUICAO]" \
  -H "Authorization: Bearer [TOKEN]" \
  -d '{
    "name": "Tecnologia da Informação",
    "acronym": "ti",
    "description": "Setor de TI",
    "active": true
  }'

# Resposta esperada (ANTES):
# HTTP 400 - "Acronym must contain only uppercase letters, numbers, and hyphens"
```

### DEPOIS da correção:
```bash
# Mesmo comando
curl -X POST "http://localhost:8080/units" \
  -H "Content-Type: application/json" \
  -H "X-Institution-Id: [UUID_INSTITUICAO]" \
  -H "Authorization: Bearer [TOKEN]" \
  -d '{
    "name": "Tecnologia da Informação",
    "acronym": "ti",
    "description": "Setor de TI",
    "active": true
  }'

# Resposta esperada (DEPOIS):
# HTTP 201 Created
# Body: { "id": "...", "acronym": "TI", ... }
# ✅ "ti" foi normalizado para "TI" sem erro
```

**Resultado esperado**: ✅ Criação bem-sucedida, acronym normalizado para "TI"

---

## 🔍 Teste 2: Busca com Espaços em Branco

### ANTES da correção:
```bash
# Busca com espaços não funcionava
curl -X GET "http://localhost:8080/units?search=   TI   " \
  -H "X-Institution-Id: [UUID_INSTITUICAO]" \
  -H "Authorization: Bearer [TOKEN]"

# Resposta esperada (ANTES):
# HTTP 200
# Body: { "content": [], "totalElements": 0 }
# ❌ Nenhum resultado encontrado (pattern era "%   ti   %")
```

### DEPOIS da correção:
```bash
# Mesmo comando
curl -X GET "http://localhost:8080/units?search=   TI   " \
  -H "X-Institution-Id: [UUID_INSTITUICAO]" \
  -H "Authorization: Bearer [TOKEN]"

# Resposta esperada (DEPOIS):
# HTTP 200
# Body: {
#   "content": [{ "id": "...", "acronym": "TI", "name": "Tecnologia..." }],
#   "totalElements": 1
# }
# ✅ Busca funciona (espaços foram removidos com trim())
```

**Resultado esperado**: ✅ Unidade "TI" encontrada corretamente

---

## 🔍 Teste 3: Filtro active=null (Todas as Unidades)

### Criar unidades para teste:
```bash
# Criar unidade ATIVA
curl -X POST "http://localhost:8080/units" \
  -H "Content-Type: application/json" \
  -H "X-Institution-Id: [UUID_INSTITUICAO]" \
  -H "Authorization: Bearer [TOKEN]" \
  -d '{"name": "TI Ativa", "acronym": "TI-A", "active": true}'

# Criar unidade INATIVA
curl -X POST "http://localhost:8080/units" \
  -H "Content-Type: application/json" \
  -H "X-Institution-Id: [UUID_INSTITUICAO]" \
  -H "Authorization: Bearer [TOKEN]" \
  -d '{"name": "TI Inativa", "acronym": "TI-I", "active": false}'
```

### Testar filtro active=null:
```bash
# Listar TODAS (active=null -> padrão)
curl -X GET "http://localhost:8080/units" \
  -H "X-Institution-Id: [UUID_INSTITUICAO]" \
  -H "Authorization: Bearer [TOKEN]"

# Resposta esperada:
# HTTP 200
# Body: {
#   "content": [
#     { "acronym": "TI-A", "active": true },
#     { "acronym": "TI-I", "active": false }
#   ],
#   "totalElements": 2
# }
# ✅ Retorna ATIVAS + INATIVAS (conforme documentado)
```

### Testar filtro active=true:
```bash
# Listar apenas ATIVAS
curl -X GET "http://localhost:8080/units?active=true" \
  -H "X-Institution-Id: [UUID_INSTITUICAO]" \
  -H "Authorization: Bearer [TOKEN]"

# Resposta esperada:
# Body: {
#   "content": [{ "acronym": "TI-A", "active": true }],
#   "totalElements": 1
# }
# ✅ Retorna apenas ATIVAS
```

### Testar filtro active=false:
```bash
# Listar apenas INATIVAS
curl -X GET "http://localhost:8080/units?active=false" \
  -H "X-Institution-Id: [UUID_INSTITUICAO]" \
  -H "Authorization: Bearer [TOKEN]"

# Resposta esperada:
# Body: {
#   "content": [{ "acronym": "TI-I", "active": false }],
#   "totalElements": 1
# }
# ✅ Retorna apenas INATIVAS
```

**Resultado esperado**:
- ✅ `active=null` → Todas (ativas + inativas)
- ✅ `active=true` → Apenas ativas
- ✅ `active=false` → Apenas inativas

---

## 🎨 Teste 4: Frontend - Validação Lowercase

### Passos no Frontend:

1. Acesse http://localhost:5173
2. Faça login como GESTOR
3. Selecione uma instituição
4. Acesse "Unidades" > "Nova Unidade"

### ANTES da correção:
```
1. Digite nome: "Recursos Humanos"
2. Digite sigla: "rh" (lowercase)
3. Observe: ❌ ERRO vermelho "Use apenas letras maiúsculas, números e hífens"
4. Usuário é forçado a digitar "RH" manualmente
```

### DEPOIS da correção:
```
1. Digite nome: "Recursos Humanos"
2. Digite sigla: "rh" (lowercase)
3. Observe: ✅ SEM ERRO (hint: "Será convertida automaticamente para maiúsculas")
4. Ao sair do campo: sigla muda para "RH" automaticamente
5. Clique "Criar": Sucesso! Unidade criada com acronym="RH"
```

**Resultado esperado**: ✅ Sem erro de validação, conversão automática funciona

---

## 🎨 Teste 5: Frontend - Botão Submit Desabilitado

### ANTES da correção:

```
1. Acesse "Nova Unidade"
2. Deixe todos os campos vazios
3. Observe: ❌ Botão "Criar" está HABILITADO (hasChanges=true sempre)
4. Clique "Criar": Erro de validação do formulário
```

### DEPOIS da correção:

```
1. Acesse "Nova Unidade"
2. Deixe todos os campos vazios
3. Observe: ✅ Botão "Criar" está DESABILITADO
4. Digite apenas "nome"
5. Observe: Botão ainda DESABILITADO (falta sigla)
6. Digite "sigla"
7. Observe: ✅ Botão agora HABILITADO (campos obrigatórios preenchidos)
```

**Resultado esperado**: ✅ Botão só habilita quando campos obrigatórios estão preenchidos

---

## ✅ Checklist Final

### Backend
- [ ] Teste 1: Criação com lowercase "ti" → sucesso, normalizado para "TI"
- [ ] Teste 2: Busca "   TI   " (com espaços) → encontra resultados
- [ ] Teste 3: Filtro `active=null` → retorna todas (ativas + inativas)
- [ ] Testes unitários: `mvn test -Dtest=UnitServiceTest` → 11/11 passando

### Frontend
- [ ] Teste 4: Digitar "rh" → sem erro, conversão automática para "RH"
- [ ] Teste 5: Botão "Criar" desabilitado quando campos vazios
- [ ] Teste 5: Botão "Criar" habilita quando campos obrigatórios preenchidos

---

## 🚀 Comandos Rápidos

```bash
# Executar todos os testes unitários
cd backend
mvn test -Dtest=UnitServiceTest

# Recompilar backend com correções
mvn clean install -DskipTests

# Reiniciar backend
mvn spring-boot:run

# Reiniciar frontend
cd ../frontend
npm run dev
```

---

## 📝 Observações

1. **Substituir placeholders**:
   - `[UUID_INSTITUICAO]`: UUID da instituição de teste
   - `[TOKEN]`: JWT token de autenticação (obter do login)

2. **Obter token de autenticação**:
   ```bash
   # Fazer login e capturar token
   curl -X POST "http://localhost:8080/auth/login" \
     -H "Content-Type: application/json" \
     -d '{"email": "gestor@instituicao.com", "password": "senha123"}' \
     | jq -r '.token'
   ```

3. **Obter UUID da instituição**:
   - Frontend: Console do navegador → `localStorage.getItem('activeInstitutionId')`
   - Backend: Tabela `institutions` → `SELECT id FROM institutions WHERE acronym = 'INST'`

---

## ✅ Resultado Final Esperado

Todas as correções devem funcionar perfeitamente:

1. ✅ Pattern validation aceita lowercase
2. ✅ Busca com espaços funciona
3. ✅ Filtro default retorna todas as unidades (documentado)
4. ✅ Frontend valida após normalização (UX melhorada)
5. ✅ Botão submit desabilita quando campos vazios

**Status**: ✅ **PRONTO PARA TESTES E2E**
