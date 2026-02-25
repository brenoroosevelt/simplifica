package com.simplifica.infrastructure.repository;

import com.simplifica.domain.entity.Normative;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface NormativeRepository extends JpaRepository<Normative, UUID>,
                                              JpaSpecificationExecutor<Normative> {
}
