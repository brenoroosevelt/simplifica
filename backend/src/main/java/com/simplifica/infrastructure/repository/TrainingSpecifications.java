package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.Training;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Specifications for building dynamic queries on Training entity.
 *
 * Provides reusable query specifications for filtering trainings
 * by various criteria with multi-tenant support.
 */
public class TrainingSpecifications {

    /**
     * Eagerly fetches all relationships to avoid LazyInitializationException.
     * This includes institution and videos.
     * Should be combined with other specifications when querying trainings.
     *
     * @return specification that performs fetch joins on all relationships
     */
    public static Specification<Training> withRelations() {
        return (root, query, cb) -> {
            if (query != null && query.getResultType().equals(Training.class)) {
                root.fetch("institution", JoinType.LEFT);
                root.fetch("videos", JoinType.LEFT);
                query.distinct(true);
            }
            return cb.conjunction();
        };
    }

    /**
     * Filters trainings by institution (tenant isolation).
     * CRITICAL for multi-tenant security.
     *
     * @param institutionId the institution UUID
     * @return specification that matches trainings of the institution
     */
    public static Specification<Training> belongsToInstitution(UUID institutionId) {
        return (root, query, cb) ->
            institutionId == null ? null : cb.equal(root.get("institution").get("id"), institutionId);
    }

    /**
     * Filters trainings by active status.
     *
     * @param active the active status
     * @return specification that matches trainings with the given status
     */
    public static Specification<Training> hasActive(Boolean active) {
        return (root, query, cb) ->
            active == null ? null : cb.equal(root.get("active"), active);
    }

    /**
     * Searches trainings by title (case-insensitive partial match).
     *
     * @param search the search term
     * @return specification that matches trainings whose title contains the search term
     */
    public static Specification<Training> searchByTitle(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("title")), pattern);
        };
    }

    /**
     * Searches trainings by multiple fields (title, description).
     * Uses OR logic between fields with case-insensitive partial match.
     *
     * @param search the search term
     * @return specification that matches trainings whose title OR description contains the search term
     */
    public static Specification<Training> searchByMultipleFields(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * Filters trainings that have a cover image.
     *
     * @param hasCoverImage true to filter trainings with cover image, false for without
     * @return specification that matches trainings with or without cover image
     */
    public static Specification<Training> hasCoverImage(Boolean hasCoverImage) {
        return (root, query, cb) -> {
            if (hasCoverImage == null) {
                return null;
            }
            if (hasCoverImage) {
                return cb.isNotNull(root.get("coverImageUrl"));
            } else {
                return cb.isNull(root.get("coverImageUrl"));
            }
        };
    }

    /**
     * Filters trainings by minimum number of videos.
     *
     * @param minVideos minimum number of videos
     * @return specification that matches trainings with at least minVideos
     */
    public static Specification<Training> hasMinimumVideos(Integer minVideos) {
        return (root, query, cb) -> {
            if (minVideos == null || minVideos <= 0) {
                return null;
            }
            return cb.greaterThanOrEqualTo(cb.size(root.get("videos")), minVideos);
        };
    }
}
