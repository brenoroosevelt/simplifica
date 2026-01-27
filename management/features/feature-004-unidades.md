# Feature 004 - CRUD Unidades

## Contexto
O sistema precisa gerenciar Unidades organizacionais de cada instituição, permitindo que gestores e administradores criem, visualizem, editem e organizem as unidades específicas de sua instituição. Cada unidade representa uma divisão organizacional (ex: departamento, setor, coordenação, diretoria) e pode incluir nome, sigla, descrição e status.

## Objetivo
Implementar um CRUD completo de Unidades com:
- Controle de permissão por tenant (instituição)
- Paginação, ordenação e filtros no backend
- Interface seguindo o padrão existente (Instituições, Cadeia de Valor)
- Usuário não escolhe a instituição - usa a atualmente selecionada
- Soft delete com flag `ativa`
- Validação de unicidade de sigla por instituição

## Usuários impactados
- Gestores de instituição (MANAGER role)
- Administradores de sistema (ADMIN role)
- Usuários com acesso à instituição ativa

## Escopo

### Inclui:

#### Backend:
- **Entidade Unit (Unidade):**
  - Relacionamento ManyToOne com Institution
  - Campos: nome, sigla, descrição, ativa, timestamps
  - Soft delete via flag `ativa`
  - Migration Flyway V8

- **Repository & Specifications:**
  - UnitRepository com JpaSpecificationExecutor
  - UnitSpecifications (belongsToInstitution, hasActive, searchByNameOrAcronym)
  - Queries otimizadas com índices

- **Service:**
  - UnitService com validação multi-tenant
  - Usa TenantContext para obter instituição atual
  - Validação de unicidade de sigla por instituição
  - Métodos: findAll, findById, create, update, delete

- **Controller REST:**
  - UnitController com endpoints protegidos
  - @PreAuthorize para MANAGER e ADMIN
  - Paginação server-side

- **DTOs:**
  - CreateUnitDTO, UpdateUnitDTO, UnitDTO
  - Validações Jakarta (Bean Validation)

#### Frontend:
- **Types TypeScript:**
  - Unit, UnitCreateRequest, UnitUpdateRequest
  - UnitListParams, PageResponse<T>

- **Service:**
  - UnitService com métodos async
  - Integração com apiClient (header X-Institution-Id automático)

- **Componentes Vue:**
  - UnitList.vue (tabela server-side com filtros)
  - UnitForm.vue (formulário create/edit)

- **Página:**
  - UnitsPage.vue (orchestration)
  - Dialogs (create/edit/delete)
  - Snackbars para feedback

- **Rota e Menu:**
  - Rota /units com meta requiresUserManagement
  - Item de menu "Unidades" no sidebar

### Não Inclui (Futuro):
- Hierarquia de unidades (unidade pai/filha)
- Responsáveis por unidade
- Localização geográfica
- Orçamento por unidade
- Integração com organograma
- Métricas por unidade

---

## Fluxo do usuário

### Fluxo Gestor/Admin - Criar Unidade
1. Usuário acessa menu "Unidades"
2. Visualiza lista de unidades da instituição ativa
3. Clica em "Nova Unidade"
4. Preenche formulário:
   - Nome (obrigatório, max 255 chars)
   - Sigla (obrigatório, max 50 chars, único por instituição)
   - Descrição (opcional, text area)
   - Status (Ativa/Inativa)
   - Instituição (exibida, read-only, não editável)
5. Clica "Criar"
6. Sistema valida unicidade da sigla
7. Unidade criada e vinculada à instituição ativa
8. Retorna à lista com mensagem de sucesso

### Fluxo Gestor/Admin - Editar Unidade
1. Na lista, clica no ícone de editar
2. Dialog abre com formulário preenchido
3. Altera campos desejados (nome, descrição, status)
4. Sigla aparece desabilitada (não pode ser alterada após criação)
5. Clica "Salvar"
6. Sistema valida
7. Unidade atualizada
8. Lista atualiza com mudanças

### Fluxo Gestor/Admin - Filtrar e Paginar
1. Na lista, digita termo de busca (nome ou sigla)
2. Sistema aplica debounce (500ms) e busca
3. Seleciona filtro de status (Ativa/Inativa)
4. Resultados filtrados em tempo real
5. Pode alterar ordenação clicando nos headers da tabela
6. Pode mudar itens por página (10, 25, 50, 100)
7. Navega entre páginas

### Fluxo Gestor/Admin - Excluir Unidade
1. Clica no ícone de deletar
2. Dialog de confirmação aparece com aviso
3. Clica "Excluir"
4. Sistema faz soft delete (ativa=false)
5. Unidade removida da lista (se filtro = Ativas)
6. Mensagem de sucesso

---

## Regras de negócio

### Unidade
- Uma unidade pertence a uma única instituição
- Nome deve ser informado (obrigatório)
- Sigla deve ser única dentro da instituição (obrigatória)
- Sigla é imutável após criação (não pode ser alterada)
- Sigla normalizada para MAIÚSCULAS automaticamente
- Descrição é opcional
- Status ativa controla soft delete
- Timestamps (createdAt, updatedAt) são automáticos
- Apenas MANAGER e ADMIN podem gerenciar unidades

### Validações
- Nome: obrigatório, max 255 caracteres
- Sigla: obrigatória, max 50 caracteres, padrão [A-Z0-9-], única por instituição
- Descrição: opcional, max 5000 caracteres
- Ativa: obrigatório, padrão true

### Isolamento Multi-tenant
- GESTOR só acessa unidades de sua instituição
- ADMIN pode acessar unidades de qualquer instituição (via contexto)
- Todas as queries filtram por institution_id automaticamente
- TenantContext obtém instituição do header X-Institution-Id
- Tentativa de acesso cross-tenant resulta em 403 Forbidden
- Validação em múltiplas camadas (interceptor, service, repository)

### Contexto de Instituição
- Instituição não é escolhida pelo usuário no formulário
- Sistema usa a instituição atualmente ativa (InstitutionStore)
- Frontend exibe instituição mas como campo read-only
- Backend valida que usuário pertence à instituição
- Usuário não pode trocar instituição de unidade existente

---

## Critérios de aceite

### Backend - Unidade

- [ ] Migration V8__create_units_table.sql criada
- [ ] Tabela units criada com:
  - [ ] FK institution_id com ON DELETE CASCADE
  - [ ] UNIQUE constraint (institution_id, acronym)
  - [ ] Índices: institution_id, active, institution_id+active, acronym
  - [ ] Trigger para updated_at
- [ ] Entidade Unit.java criada com:
  - [ ] ManyToOne com Institution
  - [ ] @PrePersist e @PreUpdate para timestamps
  - [ ] Métodos: isActive(), normalizeAcronym()
- [ ] UnitRepository criado com:
  - [ ] Extends JpaRepository e JpaSpecificationExecutor
  - [ ] Queries: findByIdAndInstitutionId, existsByAcronymAndInstitutionId, findByInstitutionIdAndActiveTrue
- [ ] UnitSpecifications criado com:
  - [ ] belongsToInstitution(UUID) - CRÍTICO para multi-tenant
  - [ ] hasActive(Boolean)
  - [ ] searchByNameOrAcronym(String)
- [ ] DTOs criados (CreateUnitDTO, UpdateUnitDTO, UnitDTO)
- [ ] UnitService implementado com:
  - [ ] getCurrentInstitutionId() usa TenantContext
  - [ ] validateTenantAccess(Unit)
  - [ ] findAll() filtra por institutionId + filtros opcionais
  - [ ] create() valida sigla única por instituição, normaliza para UPPER
  - [ ] update() valida tenant, não permite alterar sigla
  - [ ] delete() soft delete (ativa=false)
- [ ] UnitController criado com:
  - [ ] GET /units (lista com filtros e paginação)
  - [ ] GET /units/{id}
  - [ ] POST /units
  - [ ] PUT /units/{id}
  - [ ] DELETE /units/{id}
  - [ ] @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
- [ ] Teste manual backend:
  - [ ] GESTOR lista unidades → só retorna da instituição dele
  - [ ] GESTOR cria unidade → institution_id = sua instituição
  - [ ] GESTOR cria unidade com sigla duplicada → falha "already exists"
  - [ ] GESTOR tenta acessar unidade de outra instituição → 403
  - [ ] ADMIN lista unidades → retorna da instituição no contexto
  - [ ] Update tenta alterar sigla → campo ignorado (imutável)

### Frontend - Unidade

- [ ] Types criados em unit.types.ts
- [ ] UnitService criado com:
  - [ ] list(), getById(), create(), update(), delete()
- [ ] UnitList.vue criado com:
  - [ ] v-data-table-server (paginação server-side)
  - [ ] Filtros: busca (debounce 500ms), status (Ativa/Inativa)
  - [ ] Colunas: Sigla, Nome, Descrição (truncada), Status chip, Data criação, Ações
  - [ ] Emits: update:filters, update:pagination, edit, delete
- [ ] UnitForm.vue criado com:
  - [ ] Nome: v-text-field obrigatório, counter 255
  - [ ] Sigla: v-text-field obrigatório, counter 50, pattern [A-Z0-9-], uppercase transform
  - [ ] Sigla: desabilitada em modo edição
  - [ ] Descrição: v-textarea opcional, counter 5000
  - [ ] Instituição: v-text-field readonly (exibe instituição ativa)
  - [ ] Status: v-select (Ativa/Inativa)
  - [ ] Validação: hasChanges computed
  - [ ] Emits: submit, cancel
- [ ] UnitsPage.vue criado com:
  - [ ] PageHeader + UnitList
  - [ ] Dialogs: create/edit, delete confirmation
  - [ ] Snackbar para feedback
  - [ ] Methods: loadUnits(), handleFormSubmit(), handleDelete()
- [ ] Rota /units adicionada com meta:
  - [ ] requiresAuth: true
  - [ ] requiresInstitution: true
  - [ ] requiresUserManagement: true
- [ ] Menu "Unidades" adicionado ao sidebar
- [ ] Teste manual frontend:
  - [ ] Login GESTOR → acessa Unidades
  - [ ] Cria nova unidade → sucesso
  - [ ] Sigla automaticamente em maiúsculas
  - [ ] Instituição exibida mas não editável
  - [ ] Lista mostra a unidade criada
  - [ ] Tenta criar unidade com sigla duplicada → erro exibido
  - [ ] Filtro por nome funciona com debounce
  - [ ] Filtro por sigla funciona
  - [ ] Paginação funciona (mudar tamanho, trocar página)
  - [ ] Editar: altera nome, descrição → mudanças salvas
  - [ ] Editar: sigla aparece desabilitada
  - [ ] Excluir: soft delete, unidade some da lista Ativas
  - [ ] Filtrar Inativas: unidade excluída aparece

### Segurança Multi-tenant

- [ ] TenantContext.getCurrentInstitution() usado em todos métodos de service
- [ ] belongsToInstitution(institutionId) incluído em TODAS queries
- [ ] validateTenantAccess() chamado antes de modificações
- [ ] @PreAuthorize aplicado em todos endpoints
- [ ] Header X-Institution-Id enviado automaticamente (apiClient)
- [ ] Teste: GESTOR não acessa unidade de instituição diferente
- [ ] Teste: ADMIN com contexto Inst A não acessa unidade de Inst B
- [ ] Teste: Query SQL sempre tem WHERE institution_id = ?
- [ ] Teste: UNIQUE constraint funciona (institution_id, acronym)

### Qualidade

- [ ] Código segue padrões (Checkstyle backend, ESLint frontend)
- [ ] Logs apropriados (INFO para operações, DEBUG para detalhes, WARN para acessos negados)
- [ ] Exceções customizadas (ResourceNotFoundException, ResourceAlreadyExistsException, UnauthorizedAccessException)
- [ ] Transações corretas (@Transactional)
- [ ] Documentação clara em javadoc/jsdoc

---

## Tarefas

### FASE 1: Backend - Entidade e Repository

#### 1.1 - Criar Migration Flyway
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 45min

**Objetivo**: Schema de banco para units

**Arquivo**: `/backend/src/main/resources/db/migration/V8__create_units_table.sql`

**Conteúdo**:
```sql
-- ====================================
-- Units Table Migration
-- ====================================
-- Creates the units table for storing organizational units
-- per institution (multi-tenant).
-- ====================================

CREATE TABLE units (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    institution_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    acronym VARCHAR(50) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_units_institution
        FOREIGN KEY (institution_id)
        REFERENCES institutions(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_units_institution_acronym
        UNIQUE (institution_id, acronym)
);

-- Create indexes for performance
CREATE INDEX idx_units_institution_id ON units(institution_id);
CREATE INDEX idx_units_active ON units(active);
CREATE INDEX idx_units_acronym ON units(acronym);
CREATE INDEX idx_units_institution_active ON units(institution_id, active);
CREATE INDEX idx_units_created_at ON units(created_at DESC);

-- Create trigger for automatic updated_at
CREATE TRIGGER trg_units_updated_at
    BEFORE UPDATE ON units
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON TABLE units IS 'Stores organizational units per institution (multi-tenant)';
COMMENT ON COLUMN units.institution_id IS 'Foreign key to institutions table (tenant isolation)';
COMMENT ON COLUMN units.name IS 'Name of the unit (required, max 255 chars)';
COMMENT ON COLUMN units.acronym IS 'Acronym of the unit (required, unique per institution, max 50 chars)';
COMMENT ON COLUMN units.description IS 'Detailed description of the unit (optional)';
COMMENT ON COLUMN units.active IS 'Whether the unit is active (soft delete flag)';
```

**Validação**: mvn flyway:migrate executa sem erros, tabela criada, UNIQUE constraint funciona

---

#### 1.2 - Criar Entidade Unit
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Entidade JPA com relacionamento

**Arquivo**: `/backend/src/main/java/com/simplifica/domain/entity/Unit.java`

```java
package com.simplifica.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Unit entity representing an organizational unit.
 *
 * Each unit belongs to a specific institution (tenant) and represents
 * a division, department, sector, or any organizational structure.
 * Supports soft delete via the active flag.
 */
@Entity
@Table(
    name = "units",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_units_institution_acronym",
        columnNames = {"institution_id", "acronym"}
    )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 50)
    private String acronym;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Sets timestamps before persisting a new entity.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.active == null) {
            this.active = true;
        }
        // Normalize acronym to uppercase
        if (this.acronym != null) {
            this.acronym = this.acronym.toUpperCase().trim();
        }
    }

    /**
     * Updates the updatedAt timestamp before updating the entity.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if the unit is active.
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.active);
    }

    /**
     * Normalizes acronym to uppercase.
     * Used before setting the acronym field.
     *
     * @param acronym the acronym to normalize
     */
    public void setAcronym(String acronym) {
        this.acronym = acronym != null ? acronym.toUpperCase().trim() : null;
    }
}
```

**Validação**: Aplicação inicia, entidade reconhecida pelo JPA, UNIQUE constraint no banco

---

#### 1.3 - Criar Repository e Specifications
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h

**Objetivo**: Acesso a dados com queries dinâmicas

**Arquivo 1**: `/backend/src/main/java/com/simplifica/infrastructure/repository/UnitRepository.java`

```java
package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Unit entity operations.
 *
 * Provides CRUD operations and custom queries for managing units
 * with multi-tenant support.
 */
@Repository
public interface UnitRepository extends JpaRepository<Unit, UUID>,
                                        JpaSpecificationExecutor<Unit> {

    /**
     * Finds a unit by ID and institution (tenant-aware).
     *
     * @param id the unit UUID
     * @param institutionId the institution UUID
     * @return an Optional containing the unit if found
     */
    Optional<Unit> findByIdAndInstitutionId(UUID id, UUID institutionId);

    /**
     * Finds all units for a specific institution with pagination.
     *
     * @param institutionId the institution UUID
     * @param pageable pagination information
     * @return a page of units
     */
    Page<Unit> findByInstitutionId(UUID institutionId, Pageable pageable);

    /**
     * Finds active units for a specific institution with pagination.
     *
     * @param institutionId the institution UUID
     * @param pageable pagination information
     * @return a page of active units
     */
    Page<Unit> findByInstitutionIdAndActiveTrue(UUID institutionId, Pageable pageable);

    /**
     * Checks if a unit with the given acronym exists for an institution.
     *
     * @param acronym the acronym to check
     * @param institutionId the institution UUID
     * @return true if a unit with this acronym exists
     */
    boolean existsByAcronymAndInstitutionId(String acronym, UUID institutionId);

    /**
     * Checks if a unit with the given acronym exists for an institution,
     * excluding a specific unit ID (useful for updates).
     *
     * @param acronym the acronym to check
     * @param institutionId the institution UUID
     * @param excludeId the ID to exclude from the check
     * @return true if a unit with this acronym exists
     */
    boolean existsByAcronymAndInstitutionIdAndIdNot(String acronym, UUID institutionId, UUID excludeId);
}
```

**Arquivo 2**: `/backend/src/main/java/com/simplifica/infrastructure/repository/UnitSpecifications.java`

```java
package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.Unit;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Specifications for building dynamic queries on Unit entity.
 *
 * Provides reusable query specifications for filtering units
 * by various criteria with multi-tenant support.
 */
public class UnitSpecifications {

    /**
     * Filters units by institution (tenant isolation).
     *
     * @param institutionId the institution UUID
     * @return specification that matches units of the institution
     */
    public static Specification<Unit> belongsToInstitution(UUID institutionId) {
        return (root, query, cb) ->
            institutionId == null ? null : cb.equal(root.get("institution").get("id"), institutionId);
    }

    /**
     * Filters units by active status.
     *
     * @param active the active status
     * @return specification that matches units with the given status
     */
    public static Specification<Unit> hasActive(Boolean active) {
        return (root, query, cb) ->
            active == null ? null : cb.equal(root.get("active"), active);
    }

    /**
     * Searches units by name or acronym (case-insensitive partial match).
     *
     * @param search the search term
     * @return specification that matches units whose name or acronym contains the search term
     */
    public static Specification<Unit> searchByNameOrAcronym(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("acronym")), pattern)
            );
        };
    }
}
```

**Validação**: Queries funcionam, Specifications retornam Predicate correto

---

### FASE 2: Backend - Service e Controller

#### 2.1 - Criar DTOs
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 45min

**Objetivo**: Objetos de transferência

**Arquivo 1**: `/backend/src/main/java/com/simplifica/application/dto/CreateUnitDTO.java`

```java
package com.simplifica.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new unit.
 *
 * Contains validation constraints to ensure data integrity.
 * Institution is not included as it's automatically set from the tenant context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUnitDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Acronym is required")
    @Size(max = 50, message = "Acronym must not exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Acronym must contain only uppercase letters, numbers, and hyphens")
    private String acronym;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Builder.Default
    private Boolean active = true;
}
```

**Arquivo 2**: `/backend/src/main/java/com/simplifica/application/dto/UpdateUnitDTO.java`

```java
package com.simplifica.application.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating an existing unit.
 *
 * All fields are optional - only provided fields will be updated.
 * Acronym cannot be changed after creation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUnitDTO {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Boolean active;

    // Note: acronym is intentionally NOT included (immutable after creation)
}
```

**Arquivo 3**: `/backend/src/main/java/com/simplifica/application/dto/UnitDTO.java`

```java
package com.simplifica.application.dto;

import com.simplifica.domain.entity.Unit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for Unit entity.
 *
 * Used to transfer unit data between layers without exposing
 * the entity directly. Includes institution information and metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitDTO {

    private UUID id;
    private UUID institutionId;
    private String institutionName;
    private String institutionAcronym;
    private String name;
    private String acronym;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts a Unit entity to a DTO.
     *
     * @param unit the unit entity
     * @return the unit DTO, or null if the input is null
     */
    public static UnitDTO fromEntity(Unit unit) {
        if (unit == null) {
            return null;
        }

        return UnitDTO.builder()
                .id(unit.getId())
                .institutionId(unit.getInstitution().getId())
                .institutionName(unit.getInstitution().getName())
                .institutionAcronym(unit.getInstitution().getAcronym())
                .name(unit.getName())
                .acronym(unit.getAcronym())
                .description(unit.getDescription())
                .active(unit.getActive())
                .createdAt(unit.getCreatedAt())
                .updatedAt(unit.getUpdatedAt())
                .build();
    }
}
```

**Validação**: DTOs compilam, validações Bean Validation funcionam

---

#### 2.2 - Criar UnitService
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 2h30min

**Objetivo**: Lógica de negócio com multi-tenant

**Arquivo**: `/backend/src/main/java/com/simplifica/application/service/UnitService.java`

```java
package com.simplifica.application.service;

import com.simplifica.application.dto.CreateUnitDTO;
import com.simplifica.application.dto.UpdateUnitDTO;
import com.simplifica.config.tenant.TenantContext;
import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.Unit;
import com.simplifica.infrastructure.repository.UnitRepository;
import com.simplifica.infrastructure.repository.UnitSpecifications;
import com.simplifica.presentation.exception.BadRequestException;
import com.simplifica.presentation.exception.ResourceAlreadyExistsException;
import com.simplifica.presentation.exception.ResourceNotFoundException;
import com.simplifica.presentation.exception.UnauthorizedAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for managing units (organizational units).
 *
 * Provides business logic for CRUD operations on units with
 * multi-tenant support. All operations are scoped to the current institution
 * from the TenantContext.
 *
 * Security:
 * - GESTOR: Can only access units from their own institution
 * - ADMIN: Can access units from any institution (if institution context is set)
 */
@Service
@Transactional(readOnly = true)
public class UnitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnitService.class);

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private InstitutionService institutionService;

    /**
     * Finds a unit by ID with tenant validation.
     *
     * @param id the unit UUID
     * @return the Unit entity
     * @throws ResourceNotFoundException if not found
     * @throws UnauthorizedAccessException if not authorized to access
     */
    public Unit findById(UUID id) {
        LOGGER.debug("Finding unit by ID: {}", id);

        Unit unit = unitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Unit", id.toString()));

        // Validate tenant access
        validateTenantAccess(unit);

        return unit;
    }

    /**
     * Finds all units with optional filtering and pagination.
     * Automatically scoped to the current institution from TenantContext.
     *
     * @param active filter by active status (null for all)
     * @param search search term for name or acronym (null for no search)
     * @param pageable pagination and sorting parameters
     * @return paginated list of units
     */
    public Page<Unit> findAll(Boolean active, String search, Pageable pageable) {
        UUID institutionId = getCurrentInstitutionId();

        LOGGER.debug("Finding units for institution {} with filters - active: {}, search: {}",
                     institutionId, active, search);

        Specification<Unit> spec = Specification
                .where(UnitSpecifications.belongsToInstitution(institutionId));

        if (active != null) {
            spec = spec.and(UnitSpecifications.hasActive(active));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(UnitSpecifications.searchByNameOrAcronym(search));
        }

        return unitRepository.findAll(spec, pageable);
    }

    /**
     * Creates a new unit.
     * Automatically assigned to the current institution from TenantContext.
     *
     * @param dto the unit data
     * @return the created Unit entity
     * @throws ResourceAlreadyExistsException if acronym already exists for this institution
     */
    @Transactional
    public Unit create(CreateUnitDTO dto) {
        UUID institutionId = getCurrentInstitutionId();
        LOGGER.info("Creating new unit '{}' for institution: {}", dto.getAcronym(), institutionId);

        // Normalize acronym to uppercase
        String normalizedAcronym = dto.getAcronym().toUpperCase().trim();

        // Validate acronym uniqueness within institution
        if (unitRepository.existsByAcronymAndInstitutionId(normalizedAcronym, institutionId)) {
            throw new ResourceAlreadyExistsException("Unit", "acronym", normalizedAcronym);
        }

        // Load institution
        Institution institution = institutionService.findById(institutionId);

        // Build entity
        Unit unit = Unit.builder()
                .institution(institution)
                .name(dto.getName())
                .acronym(normalizedAcronym)
                .description(dto.getDescription())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        Unit saved = unitRepository.save(unit);
        LOGGER.info("Created unit with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Updates an existing unit.
     *
     * @param id the unit UUID
     * @param dto the updated data (only non-null fields are updated)
     * @return the updated Unit entity
     * @throws ResourceNotFoundException if not found
     * @throws UnauthorizedAccessException if not authorized
     */
    @Transactional
    public Unit update(UUID id, UpdateUnitDTO dto) {
        LOGGER.info("Updating unit: {}", id);

        Unit unit = findById(id); // Includes tenant validation

        // Update name if provided
        if (dto.getName() != null && !dto.getName().isBlank()) {
            unit.setName(dto.getName());
        }

        // Update description if provided
        if (dto.getDescription() != null) {
            unit.setDescription(dto.getDescription());
        }

        // Update active status if provided
        if (dto.getActive() != null) {
            unit.setActive(dto.getActive());
        }

        // Note: acronym is immutable - cannot be changed after creation

        Unit saved = unitRepository.save(unit);
        LOGGER.info("Updated unit: {}", id);

        return saved;
    }

    /**
     * Soft deletes a unit by setting its active status to false.
     *
     * @param id the unit UUID
     * @throws ResourceNotFoundException if not found
     * @throws UnauthorizedAccessException if not authorized
     */
    @Transactional
    public void delete(UUID id) {
        LOGGER.info("Soft deleting unit: {}", id);

        Unit unit = findById(id);
        unit.setActive(false);
        unitRepository.save(unit);

        LOGGER.info("Unit {} marked as inactive", id);
    }

    /**
     * Gets the current institution ID from TenantContext.
     *
     * @return the institution UUID
     * @throws BadRequestException if no institution context is set
     */
    private UUID getCurrentInstitutionId() {
        UUID institutionId = TenantContext.getCurrentInstitution();
        if (institutionId == null) {
            throw new BadRequestException("No institution context set. Please select an institution.");
        }
        return institutionId;
    }

    /**
     * Validates that the current user has access to the unit's institution.
     *
     * @param unit the unit to validate
     * @throws UnauthorizedAccessException if not authorized
     */
    private void validateTenantAccess(Unit unit) {
        UUID currentInstitutionId = getCurrentInstitutionId();

        if (!unit.getInstitution().getId().equals(currentInstitutionId)) {
            LOGGER.warn("Unauthorized access attempt to unit {} from institution {}",
                       unit.getId(), currentInstitutionId);
            throw new UnauthorizedAccessException(
                "You do not have permission to access this unit"
            );
        }
    }
}
```

**Validação**: Métodos funcionam, transações corretas, logs apropriados

---

#### 2.3 - Criar UnitController
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h30min

**Objetivo**: Endpoints REST

**Arquivo**: `/backend/src/main/java/com/simplifica/presentation/controller/UnitController.java`

```java
package com.simplifica.presentation.controller;

import com.simplifica.application.dto.CreateUnitDTO;
import com.simplifica.application.dto.UpdateUnitDTO;
import com.simplifica.application.dto.UnitDTO;
import com.simplifica.application.service.UnitService;
import com.simplifica.domain.entity.Unit;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller for managing units (organizational units).
 *
 * Provides endpoints for CRUD operations on units with multi-tenant support.
 * All operations are automatically scoped to the current institution from the
 * X-Institution-Id header via TenantInterceptor.
 *
 * Access Control:
 * - ROLE_MANAGER (GESTOR): Can manage units in their institution
 * - ROLE_ADMIN: Can manage units in any institution (when context is set)
 */
@RestController
@RequestMapping("/units")
public class UnitController {

    @Autowired
    private UnitService unitService;

    /**
     * Lists all units with optional filters and pagination.
     * Automatically filtered by the current institution from TenantContext.
     *
     * @param active filter by active status (optional)
     * @param search search term for name or acronym (optional)
     * @param pageable pagination and sorting parameters
     * @return paginated list of units
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<UnitDTO>> listUnits(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<Unit> units = unitService.findAll(active, search, pageable);
        Page<UnitDTO> dtos = units.map(UnitDTO::fromEntity);

        return ResponseEntity.ok(dtos);
    }

    /**
     * Gets a single unit by ID.
     *
     * @param id the unit UUID
     * @return the unit DTO
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<UnitDTO> getUnit(@PathVariable UUID id) {
        Unit unit = unitService.findById(id);
        return ResponseEntity.ok(UnitDTO.fromEntity(unit));
    }

    /**
     * Creates a new unit.
     * Automatically assigned to the current institution from TenantContext.
     *
     * @param dto the unit data
     * @return the created unit DTO with HTTP 201 status
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<UnitDTO> createUnit(@Valid @RequestBody CreateUnitDTO dto) {
        Unit unit = unitService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UnitDTO.fromEntity(unit));
    }

    /**
     * Updates an existing unit.
     * Note: Acronym cannot be changed after creation.
     *
     * @param id the unit UUID
     * @param dto the updated data
     * @return the updated unit DTO
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<UnitDTO> updateUnit(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUnitDTO dto) {

        Unit unit = unitService.update(id, dto);
        return ResponseEntity.ok(UnitDTO.fromEntity(unit));
    }

    /**
     * Soft deletes a unit by setting its active status to false.
     *
     * @param id the unit UUID
     * @return no content (HTTP 204)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Void> deleteUnit(@PathVariable UUID id) {
        unitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

**Validação**: Postman/curl testam endpoints, status HTTP corretos, validações funcionam

---

### FASE 3: Frontend - Types e Service

#### 3.1 - Criar Types TypeScript
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 30min

**Objetivo**: Interfaces TypeScript

**Arquivo**: `/frontend/src/types/unit.types.ts`

```typescript
export interface Unit {
  id: string
  institutionId: string
  institutionName: string
  institutionAcronym: string
  name: string
  acronym: string
  description?: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface UnitCreateRequest {
  name: string
  acronym: string
  description?: string
  active?: boolean
}

export interface UnitUpdateRequest {
  name?: string
  description?: string
  active?: boolean
  // Note: acronym is intentionally NOT included (immutable after creation)
}

export interface UnitListParams {
  page: number
  size: number
  sort?: string
  direction?: 'asc' | 'desc'
  search?: string
  active?: boolean
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}
```

**Validação**: Types compilam, autocomplete funciona

---

#### 3.2 - Criar UnitService
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 45min

**Objetivo**: API client

**Arquivo**: `/frontend/src/services/unit.service.ts`

```typescript
import apiClient from './api'
import type {
  Unit,
  UnitCreateRequest,
  UnitUpdateRequest,
  UnitListParams,
  PageResponse,
} from '@/types/unit.types'

/**
 * Service para gerenciamento de unidades organizacionais.
 * Responsável por todas as operações CRUD de unidades.
 */
class UnitService {
  private readonly BASE_PATH = '/units'

  /**
   * Lista unidades com filtros e paginação.
   * Automaticamente filtrado pela instituição ativa.
   */
  async list(params: UnitListParams): Promise<PageResponse<Unit>> {
    const response = await apiClient.get<PageResponse<Unit>>(this.BASE_PATH, {
      params: {
        page: params.page,
        size: params.size,
        sort: params.sort ? `${params.sort},${params.direction || 'asc'}` : undefined,
        search: params.search,
        active: params.active,
      },
    })
    return response.data
  }

  /**
   * Busca unidade por ID.
   */
  async getById(id: string): Promise<Unit> {
    const response = await apiClient.get<Unit>(`${this.BASE_PATH}/${id}`)
    return response.data
  }

  /**
   * Cria nova unidade.
   */
  async create(data: UnitCreateRequest): Promise<Unit> {
    const response = await apiClient.post<Unit>(this.BASE_PATH, data)
    return response.data
  }

  /**
   * Atualiza unidade existente.
   * Nota: sigla não pode ser alterada após criação.
   */
  async update(id: string, data: UnitUpdateRequest): Promise<Unit> {
    const response = await apiClient.put<Unit>(`${this.BASE_PATH}/${id}`, data)
    return response.data
  }

  /**
   * Remove unidade (soft delete).
   */
  async delete(id: string): Promise<void> {
    await apiClient.delete(`${this.BASE_PATH}/${id}`)
  }
}

export const unitService = new UnitService()
```

**Validação**: Service funciona, chamadas API corretas

---

### FASE 4: Frontend - Componentes e Página

#### 4.1 - Criar UnitList.vue
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 2h30min

**Objetivo**: Tabela com filtros e paginação

**Arquivo**: `/frontend/src/components/unit/UnitList.vue`

**Características**:
- v-data-table-server do Vuetify
- Filtros: busca (debounce 500ms nome/sigla), status (Ativa/Inativa)
- Colunas: Sigla (chip), Nome, Descrição (truncada 50 chars), Status chip, Data criação, Ações
- Emits: update:filters, update:pagination, edit, delete
- No-data slot com mensagem apropriada

**Validação**: Componente renderiza, filtros funcionam, eventos emitidos

---

#### 4.2 - Criar UnitForm.vue
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 2h

**Objetivo**: Formulário create/edit

**Arquivo**: `/frontend/src/components/unit/UnitForm.vue`

**Campos**:
- Nome: v-text-field obrigatório, counter 255, prepend-icon="mdi-office-building-outline"
- Sigla: v-text-field obrigatório, counter 50, pattern [A-Z0-9-], uppercase transform, prepend-icon="mdi-tag"
  - **Desabilitada em modo edição** com hint explicativo
- Descrição: v-textarea opcional, counter 5000, rows=4, prepend-inner-icon="mdi-text"
- Instituição: v-text-field readonly (exibe instituição ativa), prepend-icon="mdi-office-building", variant="outlined"
- Status: v-select (Ativa/Inativa), prepend-inner-icon="mdi-check-circle"

**Validação**:
- Rules: required, maxLength(n), acronymPattern
- hasChanges computed
- Uppercase transform em tempo real na sigla

**Validação**: Form valida, hasChanges funciona, sigla desabilitada em edit

---

#### 4.3 - Criar UnitsPage.vue
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h30min

**Objetivo**: Página principal

**Arquivo**: `/frontend/src/views/private/UnitsPage.vue`

**Estrutura**:
- PageHeader: título "Unidades", subtitle "Gerencie as unidades da instituição", botão "Nova Unidade"
- v-card com UnitList
- v-dialog create/edit (max-width 800, persistent, scrollable)
- v-dialog delete confirmation (max-width 500)
- v-snackbar (location="top right", timeout=3000)

**State**:
- units, totalUnits, isLoading
- formDialog, selectedUnit, formLoading
- deleteDialog, deleteLoading
- filters (search, active), pagination

**Validação**: Página funciona, CRUD completo operacional

---

#### 4.4 - Adicionar Rota e Menu
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 20min

**Modificar**: `/frontend/src/router/index.ts`
```typescript
{
  path: '/units',
  name: 'units',
  component: () => import('@/views/private/UnitsPage.vue'),
  meta: {
    layout: 'private',
    requiresAuth: true,
    requiresInstitution: true,
    requiresUserManagement: true,
  },
}
```

**Modificar**: `/frontend/src/components/navigation/AppSidebar.vue`
```typescript
{
  title: 'Unidades',
  icon: 'mdi-office-building-outline',
  to: '/units',
}
```

**Validação**: Rota funciona, menu aparece, navegação OK

---

### FASE 5: Testes e Validação

#### 5.1 - Teste Multi-tenant Backend
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h30min

**Cenários**:
1. GESTOR cria unidade → institution_id = sua instituição
2. GESTOR cria unidade com sigla "TI" → sucesso
3. GESTOR tenta criar outra unidade com sigla "TI" → falha "already exists"
4. GESTOR lista unidades → só retorna da instituição dele
5. GESTOR tenta GET /units/{id} de outra instituição → 403
6. ADMIN troca instituição → lista muda conforme contexto
7. UNIQUE constraint no banco: INSERT duplicado → erro

**Validação**: Isolamento total, sigla única por instituição

---

#### 5.2 - Teste Imutabilidade de Sigla
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 30min

**Cenários**:
1. Backend: PUT /units/{id} com acronym no body → campo ignorado
2. Frontend: modo edição → campo sigla desabilitado
3. Frontend: UpdateUnitRequest não inclui acronym

**Validação**: Sigla não pode ser alterada após criação

---

#### 5.3 - Teste End-to-End
**Status**: [ ] Pendente
**Responsável**: coder
**Estimativa**: 1h

**Fluxo completo**:
1. Login GESTOR → acessa Unidades
2. Cria nova unidade: nome "Tecnologia da Informação", sigla "ti" (auto uppercase)
3. Valida que sigla foi normalizada para "TI"
4. Valida que aparece na lista
5. Tenta criar outra com sigla "TI" → erro exibido
6. Edita nome e descrição → mudanças salvas
7. Tenta editar sigla → campo desabilitado
8. Testa filtros (busca "tecno", busca "TI")
9. Testa paginação e ordenação
10. Exclui unidade (soft delete)
11. Filtra Inativas → unidade aparece

**Validação**: Fluxo completo funciona sem erros

---

## Dependências Técnicas

### Backend
```xml
<!-- Já existentes - nenhuma nova dependência -->
spring-boot-starter-web
spring-boot-starter-data-jpa
spring-boot-starter-security
spring-boot-starter-validation
postgresql
flyway
lombok
```

### Frontend
```json
// Já existentes - nenhuma nova dependência
"vue": "^3.4"
"vuetify": "^3.5"
"pinia": "^2.1"
"vue-router": "^4.2"
"axios": "^1.6"
"typescript": "^5.2"
```

---

## Riscos e Mitigações

### Risco 1: Sigla duplicada causa confusão
**Mitigação**: UNIQUE constraint no banco (institution_id, acronym), validação no service, normalização uppercase

### Risco 2: Usuário tenta alterar sigla após criação
**Mitigação**: Campo desabilitado no frontend, UpdateUnitDTO não aceita acronym, backend ignora se enviado

### Risco 3: Vazamento cross-tenant
**Mitigação**: Validação em múltiplas camadas (interceptor, service, repository), testes exaustivos de isolamento

### Risco 4: Performance com muitas unidades
**Mitigação**: Paginação server-side, índices otimizados, queries com Specifications

---

## Validação Final

**Checklist de Aceite**:
- [ ] Entidade Unit criada com relacionamento Institution
- [ ] UNIQUE constraint (institution_id, acronym) no banco
- [ ] Repository e Specifications funcionam
- [ ] Service valida multi-tenant em todas operações
- [ ] Sigla normalizada para UPPERCASE automaticamente
- [ ] Sigla imutável após criação (frontend e backend)
- [ ] Controller REST com endpoints protegidos
- [ ] Frontend: lista, formulário, página completa
- [ ] Rota e menu adicionados
- [ ] GESTOR só acessa unidades de sua instituição
- [ ] ADMIN acessa conforme instituição ativa
- [ ] Soft delete preserva dados
- [ ] Logs apropriados (INFO, DEBUG, WARN)
- [ ] Validação de sigla duplicada funciona
- [ ] Busca por nome ou sigla funciona

**Estimativa Total**: 16-20 horas de desenvolvimento

---

## Próximos Passos

1. **Implementação**: Seguir fases 1-4 sequencialmente
2. **Teste**: Executar fase 5 (testes multi-tenant, imutabilidade, e2e)
3. **Code Review**: Revisar código, padrões, segurança
4. **Deploy**: Subir para ambiente de staging
5. **Melhorias Futuras**:
   - Hierarquia de unidades (unidade pai/filha)
   - Responsáveis por unidade
   - Localização geográfica
   - Organograma visual
   - Métricas por unidade
