# Task #2 - Backend Service e Controller Unit - CONCLUÍDA

## Resumo

Implementação completa da camada de negócio e endpoints REST para o CRUD de Unidades (Units), seguindo os padrões existentes do projeto e com validação multi-tenant rigorosa.

## Arquivos Criados

### 1. DTOs (Data Transfer Objects)

#### CreateUnitDTO.java
- **Localização**: `/backend/src/main/java/com/simplifica/application/dto/CreateUnitDTO.java`
- **Validações Bean Validation**:
  - `name`: @NotBlank, @Size(max=255)
  - `acronym`: @NotBlank, @Size(max=50), @Pattern(regexp="^[A-Z0-9-]+$")
  - `description`: @Size(max=5000)
  - `active`: Boolean com default true
- **Nota**: Institution não é incluída (vem do TenantContext automaticamente)

#### UpdateUnitDTO.java
- **Localização**: `/backend/src/main/java/com/simplifica/application/dto/UpdateUnitDTO.java`
- **Características**:
  - Todos os campos opcionais
  - **Acronym ausente propositalmente** (imutável após criação)
  - Validações: @Size nas strings
  - Suporta atualização parcial (null-safe)

#### UnitDTO.java
- **Localização**: `/backend/src/main/java/com/simplifica/application/dto/UnitDTO.java`
- **Campos**:
  - Dados da unidade (id, name, acronym, description, active)
  - Dados da instituição (institutionId, institutionName, institutionAcronym)
  - Timestamps (createdAt, updatedAt)
- **Método**: `fromEntity(Unit)` - conversão estática Entity → DTO

### 2. UnitService.java

- **Localização**: `/backend/src/main/java/com/simplifica/application/service/UnitService.java`
- **Transações**: @Transactional(readOnly = true) na classe, @Transactional em modificações

#### Métodos Implementados:

##### findById(UUID id)
- Busca unidade por ID
- Valida acesso tenant (validateTenantAccess)
- Lança ResourceNotFoundException se não encontrar
- Lança UnauthorizedAccessException se acesso cross-tenant

##### findAll(Boolean active, String search, Pageable pageable)
- Lista unidades com filtros opcionais
- **SEMPRE filtra por institutionId** do TenantContext
- Usa UnitSpecifications para composição dinâmica
- Suporta filtros: active, search (nome ou sigla)
- Paginação server-side

##### create(CreateUnitDTO dto)
- Cria nova unidade
- **Normaliza acronym para UPPERCASE automaticamente**
- Valida unicidade de acronym por instituição
- Vincula à instituição do TenantContext
- Logs: INFO no início e fim, DEBUG para detalhes

##### update(UUID id, UpdateUnitDTO dto)
- Atualiza unidade existente
- Validação tenant antes de modificar
- **Acronym é ignorado** (imutável)
- Atualização parcial (apenas campos não-null)
- Logs apropriados

##### delete(UUID id)
- **Soft delete** (ativa=false)
- Validação tenant
- Dados preservados no banco

#### Métodos Privados de Segurança:

##### getCurrentInstitutionId()
- Obtém UUID da instituição do TenantContext
- Lança BadRequestException se contexto não está setado
- CRÍTICO para isolamento multi-tenant

##### validateTenantAccess(Unit unit)
- Valida que unidade pertence à instituição do contexto
- Lança UnauthorizedAccessException se acesso negado
- Log WARN em tentativas não autorizadas
- Chamado ANTES de qualquer modificação

### 3. UnitController.java

- **Localização**: `/backend/src/main/java/com/simplifica/presentation/controller/UnitController.java`
- **Base Path**: `/units`
- **Autorização**: @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')") em todos endpoints

#### Endpoints REST:

##### GET /units
- Lista unidades com paginação e filtros
- Query params: active, search, page, size, sort
- Default: 20 itens por página, ordenação por name ASC
- Response: Page<UnitDTO>

##### GET /units/{id}
- Retorna unidade específica por ID
- Validação tenant automática no service
- Response: UnitDTO

##### POST /units
- Cria nova unidade
- Body: CreateUnitDTO com @Valid
- Response: HTTP 201 Created + UnitDTO
- Validação Bean Validation automática

##### PUT /units/{id}
- Atualiza unidade existente
- Body: UpdateUnitDTO com @Valid
- **Acronym não pode ser alterado**
- Response: HTTP 200 OK + UnitDTO

##### DELETE /units/{id}
- Soft delete (marca ativa=false)
- Response: HTTP 204 No Content
- Dados preservados no banco

### 4. UnitServiceTest.java

- **Localização**: `/backend/src/test/java/com/simplifica/unit/service/UnitServiceTest.java`
- **Framework**: JUnit 5 + Mockito
- **Cobertura**: 11 testes, 100% de sucesso

#### Testes Implementados:

1. **findById_shouldReturnUnit_whenUnitExistsAndBelongsToCurrentInstitution**
   - Valida busca bem-sucedida com acesso correto

2. **findById_shouldThrowResourceNotFoundException_whenUnitDoesNotExist**
   - Valida exceção quando unidade não existe

3. **findById_shouldThrowUnauthorizedAccessException_whenUnitBelongsToDifferentInstitution**
   - **CRÍTICO**: Valida isolamento multi-tenant

4. **findAll_shouldReturnPagedUnits_whenFiltersApplied**
   - Valida paginação e filtros

5. **findAll_shouldThrowBadRequestException_whenNoInstitutionContext**
   - Valida que contexto de tenant é obrigatório

6. **create_shouldCreateUnit_whenDataIsValid**
   - Valida criação bem-sucedida

7. **create_shouldNormalizeAcronymToUppercase**
   - **CRÍTICO**: Valida que "ti" vira "TI" automaticamente

8. **create_shouldThrowResourceAlreadyExistsException_whenAcronymAlreadyExists**
   - Valida unicidade de acronym por instituição

9. **update_shouldUpdateUnit_whenDataIsValid**
   - Valida atualização bem-sucedida

10. **delete_shouldSoftDeleteUnit_whenUnitExists**
    - Valida soft delete

11. **delete_shouldThrowResourceNotFoundException_whenUnitDoesNotExist**
    - Valida exceção em delete

## Validações Multi-Tenant Implementadas

### Camada Service (UnitService):

1. **getCurrentInstitutionId()**
   - TODAS as operações começam obtendo institutionId do TenantContext
   - Falha rápida se contexto não está setado

2. **validateTenantAccess(Unit)**
   - Chamado ANTES de TODA modificação (update, delete)
   - Compara institution_id da unidade com contexto atual
   - Log WARN + UnauthorizedAccessException em tentativa não autorizada

3. **Specifications com belongsToInstitution(institutionId)**
   - TODAS queries de listagem filtram por institutionId
   - Impossível vazar dados cross-tenant nas listagens

4. **Validação de Unicidade Scoped**
   - existsByAcronymAndInstitutionId() garante sigla única POR INSTITUIÇÃO
   - Instituições diferentes podem ter siglas iguais

### Camada Controller (UnitController):

1. **@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")**
   - Spring Security valida roles ANTES do controller
   - Apenas GESTOR e ADMIN podem acessar endpoints

2. **Tenant Context via Interceptor**
   - Header X-Institution-Id extraído por TenantInterceptor
   - Setado no TenantContext antes do controller
   - Limpo após resposta (via try-finally)

## Regras de Negócio Implementadas

### 1. Sigla (Acronym)

#### Normalização Automática:
- Input "ti" → "TI"
- Input "rh " → "RH" (trim + uppercase)
- Ocorre em: UnitService.create() e Unit.setAcronym()

#### Imutabilidade:
- UpdateUnitDTO **não possui campo acronym**
- Backend ignora se acronym for enviado em update
- Frontend desabilita campo em modo edição

#### Unicidade por Instituição:
- UNIQUE constraint no banco: (institution_id, acronym)
- Validação no service: existsByAcronymAndInstitutionId()
- Instituição A pode ter "TI", Instituição B também pode

### 2. Soft Delete

- Campo `active` controla visibilidade
- delete() → setActive(false) + save()
- Dados preservados permanentemente
- Filtro opcional em listagens

### 3. Contexto de Instituição

- **NUNCA escolhido pelo usuário no formulário**
- Sempre vem do TenantContext (header X-Institution-Id)
- InstitutionService.findById() valida existência
- UnitDTO exibe instituição (read-only)

## Logs Implementados

### INFO Level:
```java
"Creating new unit '{}' for institution: {}"
"Created unit with ID: {}"
"Updating unit: {}"
"Updated unit: {}"
"Soft deleting unit: {}"
"Unit {} marked as inactive"
```

### DEBUG Level:
```java
"Finding unit by ID: {}"
"Finding units for institution {} with filters - active: {}, search: {}"
```

### WARN Level:
```java
"Unauthorized access attempt to unit {} from institution {}"
```

## Padrões Seguidos

### 1. Nomenclatura
- Entidade: Unit
- DTO: CreateUnitDTO, UpdateUnitDTO, UnitDTO
- Service: UnitService
- Controller: UnitController
- Endpoint base: `/units`

### 2. Arquitetura
- **Presentation Layer**: Controller REST
- **Application Layer**: Service (lógica de negócio) + DTOs
- **Domain Layer**: Entity Unit
- **Infrastructure Layer**: Repository + Specifications

### 3. Validações em Múltiplas Camadas
- **Controller**: @Valid Bean Validation
- **Service**: Validações de negócio (unicidade, tenant)
- **Entity**: @PrePersist normalização
- **Repository**: UNIQUE constraint no banco

### 4. Transações
- Service: @Transactional(readOnly = true) padrão
- Métodos de modificação: @Transactional (sobrescreve)
- Propagation default: REQUIRED

### 5. Exceções Customizadas
- ResourceNotFoundException (404)
- ResourceAlreadyExistsException (409)
- UnauthorizedAccessException (403)
- BadRequestException (400)

## Compilação e Testes

### Compilação:
```bash
mvn clean compile -DskipTests
```
**Resultado**: ✅ BUILD SUCCESS (90 source files compiled)

### Testes Unitários:
```bash
mvn test -Dtest=UnitServiceTest
```
**Resultado**: ✅ 11 tests passed, 0 failures, 0 errors, 0 skipped

## Próximos Passos

### Validações Manuais Recomendadas:

1. **Teste com Postman/curl**:
   - POST /units → criar unidade
   - GET /units → listar com filtros
   - PUT /units/{id} → atualizar (tentar mudar acronym)
   - DELETE /units/{id} → soft delete

2. **Teste Multi-tenant**:
   - GESTOR Inst A cria unidade → institution_id correto
   - GESTOR Inst A tenta GET /units/{id} de Inst B → 403
   - GESTOR Inst A lista unidades → só vê da Inst A
   - ADMIN troca contexto → lista muda conforme instituição

3. **Teste Validações**:
   - Criar com acronym "ti" → salva como "TI"
   - Criar duas unidades com acronym "TI" mesma inst → 2ª falha
   - Atualizar unidade enviando acronym no body → campo ignorado

4. **Teste Banco de Dados**:
   - Verificar UNIQUE constraint: INSERT manual duplicado → erro
   - Verificar trigger updated_at funciona
   - Verificar soft delete: active=false preserva dados

## Critérios de Aceite (Checklist)

- ✅ DTOs criados com validações Jakarta
- ✅ UnitService implementado com isolamento multi-tenant
- ✅ TenantContext.getCurrentInstitution() usado em todos métodos
- ✅ validateTenantAccess() chamado antes de modificações
- ✅ Acronym normalizado para UPPERCASE automaticamente
- ✅ Acronym validado como único por instituição
- ✅ Acronym imutável (UpdateUnitDTO sem campo acronym)
- ✅ UnitController criado com endpoints REST
- ✅ @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')") aplicado
- ✅ Paginação server-side com @PageableDefault
- ✅ Status HTTP corretos (201 para create, 204 para delete)
- ✅ Soft delete implementado (ativa=false)
- ✅ Logs apropriados (INFO, DEBUG, WARN)
- ✅ Transações @Transactional corretas
- ✅ Testes unitários passando (11/11)
- ✅ Compilação sem erros

## Considerações de Segurança

### ✅ Implementadas:

1. **Isolamento Multi-tenant RIGOROSO**:
   - TenantContext obrigatório
   - Validação em TODAS operações
   - Queries SEMPRE filtradas por institutionId

2. **Autorização por Roles**:
   - Apenas MANAGER e ADMIN acessam endpoints
   - Spring Security valida antes do controller

3. **Validação de Acesso Cross-tenant**:
   - validateTenantAccess() em update/delete
   - Log WARN em tentativas não autorizadas
   - Exceção UnauthorizedAccessException

4. **Validações Bean Validation**:
   - Input sanitizado (@Pattern para acronym)
   - Size limits em todos campos
   - @NotBlank em campos obrigatórios

### 🔒 Garantias:

- ❌ GESTOR **NUNCA** acessa unidade de outra instituição
- ❌ Listagens **NUNCA** retornam dados cross-tenant
- ❌ Update/Delete **NUNCA** permite modificação cross-tenant
- ✅ Acronym **SEMPRE** único dentro da instituição
- ✅ Sigla **SEMPRE** normalizada UPPERCASE
- ✅ Institution context **SEMPRE** validado

## Diferenças com ValueChainService (Referência)

### Similaridades:
- Mesmo padrão de validação multi-tenant
- Mesma estrutura de métodos (findAll, findById, create, update, delete)
- Mesmos logs (INFO, DEBUG, WARN)
- Mesma arquitetura (DTOs, Service, Controller)

### Diferenças:
- **ValueChain**: tem upload de imagem (FileStorageService)
- **Unit**: campo acronym imutável e normalizado
- **Unit**: validação de unicidade por acronym+institutionId
- **Unit**: sem upload de arquivos

## Conclusão

A implementação da **Task #2 - Backend Service e Controller Unit** está **100% completa** e **validada por testes**.

Todos os critérios de aceite foram atendidos:
- ✅ Código limpo e bem documentado
- ✅ Isolamento multi-tenant rigoroso
- ✅ Validações em múltiplas camadas
- ✅ Logs apropriados
- ✅ Testes passando
- ✅ Seguindo padrões do projeto

**Próximo passo**: Task #4 - Frontend - Componentes Unit (já em progresso)

---

**Data da Implementação**: 2026-01-27
**Desenvolvido por**: Claude Sonnet 4.5
**Status**: ✅ CONCLUÍDO
