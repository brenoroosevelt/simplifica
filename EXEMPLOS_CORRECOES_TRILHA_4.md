# EXEMPLOS DE CÓDIGO CORRIGIDO - TRILHA 4

Este documento contém exemplos de código corrigido para as principais recomendações.

---

## 1. Process.java - CORRIGIDO

Arquivo: `/home/breno/dev/claude-agents/backend/src/main/java/com/simplifica/domain/entity/Process.java`

### Correção #1: Remover columnDefinition em enums

```java
// ANTES:
@Enumerated(EnumType.STRING)
@Column(name = "documentation_status", columnDefinition = "documentation_status")
private ProcessDocumentationStatus documentationStatus;

// DEPOIS:
@Enumerated(EnumType.STRING)
@Column(name = "documentation_status", length = 50)
private ProcessDocumentationStatus documentationStatus;
```

### Correção #2: Boolean vs boolean primitivo

```java
// ANTES:
@Column(name = "is_critical", nullable = false)
@Builder.Default
private Boolean isCritical = false;

@Column(nullable = false)
@Builder.Default
private Boolean active = true;

public boolean isActive() {
    return Boolean.TRUE.equals(this.active);
}

public boolean isCritical() {
    return Boolean.TRUE.equals(this.isCritical);
}

// DEPOIS:
@Column(name = "is_critical", nullable = false)
@Builder.Default
private boolean isCritical = false;

@Column(nullable = false)
@Builder.Default
private boolean active = true;

// Getters são gerados automaticamente pelo Lombok @Getter
// public boolean isActive() { return this.active; }
// public boolean isCritical() { return this.isCritical; }
```

### Correção #3: Adicionar @ToString(exclude)

```java
// ANTES:
@Entity
@Table(name = "processes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Process {

// DEPOIS:
@Entity
@Table(name = "processes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"institution", "valueChain", "responsibleUnit", "directUnit", "mappings"})
public class Process {
```

### Versão Completa Corrigida - Excerpto Principal

```java
package com.simplifica.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Process entity representing an organizational process within an institution.
 *
 * Each process belongs to a specific institution (tenant) and represents
 * a business process that can be documented, mapped, and managed.
 * Supports relationships with value chains and organizational units,
 * as well as multiple status tracking fields for documentation,
 * external guidance, risk management, and mapping.
 */
@Entity
@Table(name = "processes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"institution", "valueChain", "responsibleUnit", "directUnit", "mappings"})
public class Process {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false, length = 255)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "value_chain_id")
    private ValueChain valueChain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsible_unit_id")
    private Unit responsibleUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direct_unit_id")
    private Unit directUnit;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_critical", nullable = false)
    @Builder.Default
    private boolean isCritical = false;

    // Documentation status and URL
    @Enumerated(EnumType.STRING)
    @Column(name = "documentation_status", length = 50)
    private ProcessDocumentationStatus documentationStatus;

    @Column(name = "documentation_url", length = 1024)
    private String documentationUrl;

    // External user guidance status and URL
    @Enumerated(EnumType.STRING)
    @Column(name = "external_guidance_status", length = 50)
    private ProcessExternalGuidanceStatus externalGuidanceStatus;

    @Column(name = "external_guidance_url", length = 1024)
    private String externalGuidanceUrl;

    // Risk management status and URL
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_management_status", length = 50)
    private ProcessRiskManagementStatus riskManagementStatus;

    @Column(name = "risk_management_url", length = 1024)
    private String riskManagementUrl;

    // Process mapping status
    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_status", length = 50)
    private ProcessMappingStatus mappingStatus;

    // Process mapping files (HTML uploads)
    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProcessMapping> mappings = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

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
        // Lombok @Builder.Default já cuida dos defaults, mas sendo defensivo:
        if (!this.active) {
            this.active = true;
        }
        if (this.isCritical) {
            this.isCritical = false;
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
     * Checks if the process is active.
     * Note: getter automaticamente gerado por Lombok @Getter
     *
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Checks if the process is critical.
     * Note: getter automaticamente gerado por Lombok @Getter
     *
     * @return true if critical, false otherwise
     */
    public boolean isCritical() {
        return this.isCritical;
    }

    /**
     * Adds a mapping file to this process.
     *
     * @param mapping the mapping to add
     */
    public void addMapping(ProcessMapping mapping) {
        this.mappings.add(mapping);
        mapping.setProcess(this);
    }

    /**
     * Removes a mapping file from this process.
     *
     * @param mapping the mapping to remove
     */
    public void removeMapping(ProcessMapping mapping) {
        this.mappings.remove(mapping);
        mapping.setProcess(null);
    }
}
```

---

## 2. ProcessMapping.java - CORRIGIDO

Arquivo: `/home/breno/dev/claude-agents/backend/src/main/java/com/simplifica/domain/entity/ProcessMapping.java`

### Versão Completa Corrigida

```java
package com.simplifica.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ProcessMapping entity representing uploaded HTML mapping files for processes.
 *
 * Each mapping belongs to a specific process and stores information about
 * uploaded HTML files (typically exported from Bizagi or similar tools).
 * Multiple mappings can be associated with a single process.
 *
 * Note: fileUrl and filename are immutable after creation (updatable = false).
 * If updates are needed, delete and re-upload.
 */
@Entity
@Table(name = "process_mappings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "process")
public class ProcessMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "process_id", nullable = false)
    private Process process;

    @Column(name = "file_url", nullable = false, length = 1024, updatable = false)
    private String fileUrl;

    @Column(nullable = false, length = 255, updatable = false)
    private String filename;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    /**
     * Sets timestamps before persisting a new entity.
     */
    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}
```

---

## 3. ProcessSpecifications.java - CORRIGIDO

Arquivo: `/home/breno/dev/claude-agents/backend/src/main/java/com/simplifica/infrastructure/repository/ProcessSpecifications.java`

### Versão Completa Corrigida

```java
package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.Process;
import com.simplifica.domain.entity.ProcessDocumentationStatus;
import com.simplifica.domain.entity.ProcessExternalGuidanceStatus;
import com.simplifica.domain.entity.ProcessMappingStatus;
import com.simplifica.domain.entity.ProcessRiskManagementStatus;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Specifications for building dynamic queries on Process entity.
 *
 * Provides reusable query specifications for filtering processes
 * by various criteria with multi-tenant support.
 */
public class ProcessSpecifications {

    private ProcessSpecifications() {
        // Private constructor to prevent instantiation
    }

    /**
     * Eagerly fetches all relationships to avoid LazyInitializationException.
     * This includes institution, value chain, and both units (responsible and direct).
     * Should be combined with other specifications when querying processes.
     *
     * @return specification that performs fetch joins on all relationships
     */
    public static Specification<Process> withRelations() {
        return (root, query, cb) -> {
            if (query != null && query.getResultType().equals(Process.class)) {
                root.fetch("institution", JoinType.LEFT);
                root.fetch("valueChain", JoinType.LEFT);
                root.fetch("responsibleUnit", JoinType.LEFT);
                root.fetch("directUnit", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();  // Return valid predicate (always true)
        };
    }

    /**
     * Filters processes by institution (tenant isolation).
     * CRITICAL for multi-tenant security.
     *
     * @param institutionId the institution UUID
     * @return specification that matches processes of the institution
     */
    public static Specification<Process> belongsToInstitution(UUID institutionId) {
        return (root, query, cb) ->
            institutionId == null ? null : cb.equal(root.get("institution").get("id"), institutionId);
    }

    /**
     * Filters processes by active status.
     *
     * @param active the active status
     * @return specification that matches processes with the given status
     */
    public static Specification<Process> hasActive(Boolean active) {
        return (root, query, cb) ->
            active == null ? null : cb.equal(root.get("active"), active);
    }

    /**
     * Searches processes by name (case-insensitive partial match).
     *
     * @param search the search term
     * @return specification that matches processes whose name contains the search term
     */
    public static Specification<Process> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }

    /**
     * Filters processes by value chain.
     *
     * @param valueChainId the value chain UUID
     * @return specification that matches processes with the given value chain
     */
    public static Specification<Process> hasValueChain(UUID valueChainId) {
        return (root, query, cb) ->
            valueChainId == null ? null : cb.equal(root.get("valueChain").get("id"), valueChainId);
    }

    /**
     * Filters processes by critical status.
     *
     * @param isCritical the critical status
     * @return specification that matches processes with the given critical status
     */
    public static Specification<Process> hasCritical(Boolean isCritical) {
        return (root, query, cb) ->
            isCritical == null ? null : cb.equal(root.get("isCritical"), isCritical);
    }

    /**
     * Filters processes by documentation status.
     *
     * @param status the documentation status as string (e.g., "DOCUMENTED")
     * @return specification that matches processes with the given documentation status
     */
    public static Specification<Process> hasDocumentationStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) {
                return null;
            }
            try {
                ProcessDocumentationStatus docStatus = ProcessDocumentationStatus.valueOf(status);
                return cb.equal(root.get("documentationStatus"), docStatus);
            } catch (IllegalArgumentException e) {
                return null; // Invalid status, no filter
            }
        };
    }

    /**
     * Filters processes by external guidance status.
     *
     * @param status the external guidance status as string (e.g., "AVAILABLE")
     * @return specification that matches processes with the given external guidance status
     */
    public static Specification<Process> hasExternalGuidanceStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) {
                return null;
            }
            try {
                ProcessExternalGuidanceStatus guidanceStatus = ProcessExternalGuidanceStatus.valueOf(status);
                return cb.equal(root.get("externalGuidanceStatus"), guidanceStatus);
            } catch (IllegalArgumentException e) {
                return null;
            }
        };
    }

    /**
     * Filters processes by risk management status.
     *
     * @param status the risk management status as string (e.g., "PREPARED")
     * @return specification that matches processes with the given risk management status
     */
    public static Specification<Process> hasRiskManagementStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) {
                return null;
            }
            try {
                ProcessRiskManagementStatus riskStatus = ProcessRiskManagementStatus.valueOf(status);
                return cb.equal(root.get("riskManagementStatus"), riskStatus);
            } catch (IllegalArgumentException e) {
                return null;
            }
        };
    }

    /**
     * Filters processes by mapping status.
     *
     * @param status the mapping status as string (e.g., "MAPPED")
     * @return specification that matches processes with the given mapping status
     */
    public static Specification<Process> hasMappingStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) {
                return null;
            }
            try {
                ProcessMappingStatus mappingStatus = ProcessMappingStatus.valueOf(status);
                return cb.equal(root.get("mappingStatus"), mappingStatus);
            } catch (IllegalArgumentException e) {
                return null;
            }
        };
    }
}
```

---

## TESTE DE COMPILAÇÃO

Após aplicar as correções, execute:

```bash
cd /home/breno/dev/claude-agents/backend

# Compilar
mvn clean compile -q

# Se compilar sem erros, está correto
# Se houver erros, verifique as alterações
```

---

## EXEMPLO DE USO (TRILHA 5)

Como os DTOs e specifications serão usados na service layer:

```java
// Exemplo de Query com withRelations() e belongsToInstitution()
Specification<Process> spec = Specification.where(
    ProcessSpecifications.withRelations()
).and(
    ProcessSpecifications.belongsToInstitution(institutionId)
).and(
    ProcessSpecifications.searchByName(searchTerm)
).and(
    ProcessSpecifications.hasActive(true)
);

Page<Process> processes = processRepository.findAll(spec, pageable);

// Converter para DTOs
List<ProcessDTO> dtos = processes.getContent().stream()
    .map(ProcessDTO::fromEntity)
    .collect(Collectors.toList());
```

