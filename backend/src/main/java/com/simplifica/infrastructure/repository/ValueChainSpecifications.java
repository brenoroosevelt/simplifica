package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.ValueChain;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Specifications for building dynamic queries on ValueChain entity.
 *
 * These specifications enable flexible querying with multi-tenant awareness,
 * ensuring that value chains are always filtered by institution.
 */
public class ValueChainSpecifications {

    /**
     * Specification to filter value chains by institution ID.
     * CRITICAL: This must be used in ALL queries to ensure multi-tenant isolation.
     *
     * @param institutionId the institution ID to filter by
     * @return a specification that filters by institution
     */
    public static Specification<ValueChain> belongsToInstitution(UUID institutionId) {
        return (root, query, cb) -> institutionId == null ? null : cb.equal(root.get("institution").get("id"), institutionId);
    }

    /**
     * Specification to filter value chains by active status.
     *
     * @param active the active status to filter by (null for all)
     * @return a specification that filters by active status
     */
    public static Specification<ValueChain> hasActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }

    /**
     * Specification to search value chains by name (case-insensitive).
     *
     * @param search the search term (will be matched with LIKE %term%)
     * @return a specification that searches by name
     */
    public static Specification<ValueChain> searchByName(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }
}
