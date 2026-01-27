package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.ValueChain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ValueChain entity operations.
 *
 * Provides CRUD operations and custom queries for managing value chains
 * with multi-tenant awareness (institution-based filtering).
 */
@Repository
public interface ValueChainRepository extends JpaRepository<ValueChain, UUID>, JpaSpecificationExecutor<ValueChain> {

    /**
     * Finds a value chain by ID and institution ID (multi-tenant safe).
     *
     * @param id the value chain's UUID
     * @param institutionId the institution's UUID
     * @return an Optional containing the value chain if found
     */
    Optional<ValueChain> findByIdAndInstitutionId(UUID id, UUID institutionId);

    /**
     * Finds all active value chains for a specific institution.
     *
     * @param institutionId the institution's UUID
     * @return list of active value chains
     */
    List<ValueChain> findByInstitutionIdAndActiveTrue(UUID institutionId);

    /**
     * Checks if a value chain with the given name exists for a specific institution.
     *
     * @param name the value chain name
     * @param institutionId the institution's UUID
     * @return true if a value chain with this name exists for this institution
     */
    boolean existsByNameAndInstitutionId(String name, UUID institutionId);
}
