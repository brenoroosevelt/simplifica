package com.simplifica.domain.entity;

import com.simplifica.infrastructure.hibernate.ProcessDocumentationStatusType;
import com.simplifica.infrastructure.hibernate.ProcessExternalGuidanceStatusType;
import com.simplifica.infrastructure.hibernate.ProcessMappingStatusType;
import com.simplifica.infrastructure.hibernate.ProcessRiskManagementStatusType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.Type;

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
    @Type(ProcessDocumentationStatusType.class)
    @Column(name = "documentation_status")
    private ProcessDocumentationStatus documentationStatus;

    @Column(name = "documentation_url", length = 1024)
    private String documentationUrl;

    // External user guidance status and URL
    @Type(ProcessExternalGuidanceStatusType.class)
    @Column(name = "external_guidance_status")
    private ProcessExternalGuidanceStatus externalGuidanceStatus;

    @Column(name = "external_guidance_url", length = 1024)
    private String externalGuidanceUrl;

    // Risk management status and URL
    @Type(ProcessRiskManagementStatusType.class)
    @Column(name = "risk_management_status")
    private ProcessRiskManagementStatus riskManagementStatus;

    @Column(name = "risk_management_url", length = 1024)
    private String riskManagementUrl;

    // Process mapping status
    @Type(ProcessMappingStatusType.class)
    @Column(name = "mapping_status")
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
    }

    /**
     * Updates the updatedAt timestamp before updating the entity.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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
