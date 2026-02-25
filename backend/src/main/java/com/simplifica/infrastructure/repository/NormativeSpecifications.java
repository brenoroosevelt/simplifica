package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.Normative;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;

public class NormativeSpecifications {

    private NormativeSpecifications() {}

    public static Specification<Normative> withInstitution() {
        return (root, query, cb) -> {
            if (query != null && !query.getResultType().equals(Long.class)) {
                root.fetch("institution", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    public static Specification<Normative> belongsToInstitution(UUID institutionId) {
        return (root, query, cb) -> cb.equal(root.get("institution").get("id"), institutionId);
    }

    public static Specification<Normative> searchByTitle(String search) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%");
    }
}
