package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.InstitutionRole;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserInstitution;
import com.simplifica.domain.entity.UserStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Specifications for complex User queries.
 *
 * Provides reusable query specifications for filtering users by various
 * criteria including status, institution membership, roles, and search terms.
 * These specifications can be composed using AND/OR logic.
 */
public class UserSpecifications {

    /**
     * Filters users by status.
     *
     * @param status the user status to filter by
     * @return a specification that matches users with the given status
     */
    public static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    /**
     * Filters users by institution membership.
     *
     * @param institutionId the institution UUID
     * @return a specification that matches users belonging to the institution
     */
    public static Specification<User> belongsToInstitution(UUID institutionId) {
        return (root, query, cb) -> {
            if (institutionId == null) {
                return cb.conjunction();
            }

            query.distinct(true);
            Join<User, UserInstitution> userInstitution = root.join("institutions", JoinType.INNER);

            return cb.and(
                cb.equal(userInstitution.get("institution").get("id"), institutionId),
                cb.equal(userInstitution.get("active"), true)
            );
        };
    }

    /**
     * Filters users by institution role.
     *
     * @param role the institution role to filter by
     * @return a specification that matches users with the given role in any institution
     */
    public static Specification<User> hasInstitutionRole(InstitutionRole role) {
        return (root, query, cb) -> {
            if (role == null) {
                return cb.conjunction();
            }

            query.distinct(true);
            Join<User, UserInstitution> userInstitution = root.join("institutions", JoinType.INNER);

            return cb.and(
                cb.isMember(role, userInstitution.get("roles")),
                cb.equal(userInstitution.get("active"), true)
            );
        };
    }

    /**
     * Searches users by name or email.
     *
     * @param search the search term (case-insensitive partial match)
     * @return a specification that matches users whose name or email contains the search term
     */
    public static Specification<User> searchByNameOrEmail(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }

            String searchPattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), searchPattern),
                cb.like(cb.lower(root.get("email")), searchPattern)
            );
        };
    }

    /**
     * Combines multiple filter specifications with AND logic.
     *
     * @param status user status filter (optional)
     * @param institutionId institution membership filter (optional)
     * @param role institution role filter (optional)
     * @param search search term for name or email (optional)
     * @return a combined specification with all non-null filters
     */
    public static Specification<User> withFilters(UserStatus status, UUID institutionId,
                                                   InstitutionRole role, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(hasStatus(status).toPredicate(root, query, cb));
            }

            if (institutionId != null) {
                predicates.add(belongsToInstitution(institutionId).toPredicate(root, query, cb));
            }

            if (role != null) {
                predicates.add(hasInstitutionRole(role).toPredicate(root, query, cb));
            }

            if (search != null && !search.isBlank()) {
                predicates.add(searchByNameOrEmail(search).toPredicate(root, query, cb));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
