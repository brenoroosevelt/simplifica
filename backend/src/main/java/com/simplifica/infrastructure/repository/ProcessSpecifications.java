package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.Process;
import com.simplifica.domain.entity.ProcessDocumentationStatus;
import com.simplifica.domain.entity.ProcessExternalGuidanceStatus;
import com.simplifica.domain.entity.ProcessRiskManagementStatus;
import com.simplifica.domain.entity.ProcessMappingStatus;
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
            return cb.conjunction();
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
     * Searches processes by multiple fields (name, description).
     * Uses OR logic between fields with case-insensitive partial match.
     *
     * @param search the search term
     * @return specification that matches processes whose name OR description contains the search term
     */
    public static Specification<Process> searchByMultipleFields(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * Filters processes by documentation status.
     *
     * @param status the documentation status
     * @return specification that matches processes with the given documentation status
     */
    public static Specification<Process> hasDocumentationStatus(ProcessDocumentationStatus status) {
        return (root, query, cb) ->
            status == null ? null : cb.equal(root.get("documentationStatus"), status);
    }

    /**
     * Filters processes by external guidance status.
     *
     * @param status the external guidance status
     * @return specification that matches processes with the given external guidance status
     */
    public static Specification<Process> hasExternalGuidanceStatus(ProcessExternalGuidanceStatus status) {
        return (root, query, cb) ->
            status == null ? null : cb.equal(root.get("externalGuidanceStatus"), status);
    }

    /**
     * Filters processes by risk management status.
     *
     * @param status the risk management status
     * @return specification that matches processes with the given risk management status
     */
    public static Specification<Process> hasRiskManagementStatus(ProcessRiskManagementStatus status) {
        return (root, query, cb) ->
            status == null ? null : cb.equal(root.get("riskManagementStatus"), status);
    }

    /**
     * Filters processes by mapping status.
     *
     * @param status the mapping status
     * @return specification that matches processes with the given mapping status
     */
    public static Specification<Process> hasMappingStatus(ProcessMappingStatus status) {
        return (root, query, cb) ->
            status == null ? null : cb.equal(root.get("mappingStatus"), status);
    }

    /**
     * Filters processes by responsible unit.
     *
     * @param unitId the responsible unit UUID
     * @return specification that matches processes with the given responsible unit
     */
    public static Specification<Process> hasResponsibleUnit(UUID unitId) {
        return (root, query, cb) ->
            unitId == null ? null : cb.equal(root.get("responsibleUnit").get("id"), unitId);
    }

    /**
     * Filters processes by direct unit.
     *
     * @param unitId the direct unit UUID
     * @return specification that matches processes with the given direct unit
     */
    public static Specification<Process> hasDirectUnit(UUID unitId) {
        return (root, query, cb) ->
            unitId == null ? null : cb.equal(root.get("directUnit").get("id"), unitId);
    }
}
