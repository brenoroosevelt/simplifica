package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.Institution;
import com.simplifica.domain.entity.InstitutionType;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specifications for building dynamic queries on Institution entity.
 */
public class InstitutionSpecifications {

    public static Specification<Institution> hasActive(Boolean active) {
        return (root, query, cb) -> active == null ? null : cb.equal(root.get("active"), active);
    }

    public static Specification<Institution> hasType(InstitutionType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Institution> searchByNameOrAcronym(String search) {
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
