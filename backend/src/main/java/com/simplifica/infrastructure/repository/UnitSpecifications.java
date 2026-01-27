package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.Unit;
import jakarta.persistence.criteria.JoinType;
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
     * Eagerly fetches the institution relationship to avoid LazyInitializationException.
     * This should be combined with other specifications when querying units.
     *
     * @return specification that performs a fetch join on institution
     */
    public static Specification<Unit> withInstitution() {
        return (root, query, cb) -> {
            if (query != null && query.getResultType().equals(Unit.class)) {
                root.fetch("institution", JoinType.LEFT);
                query.distinct(true);
            }
            return null;
        };
    }

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
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("acronym")), pattern)
            );
        };
    }
}
