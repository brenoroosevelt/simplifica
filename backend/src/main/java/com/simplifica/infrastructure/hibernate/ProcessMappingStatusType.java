package com.simplifica.infrastructure.hibernate;

import com.simplifica.domain.entity.ProcessMappingStatus;

/**
 * Hibernate UserType for ProcessMappingStatus enum.
 * Maps to PostgreSQL process_mapping_status type.
 */
public class ProcessMappingStatusType extends PostgreSQLEnumType<ProcessMappingStatus> {

    public ProcessMappingStatusType() {
        super(ProcessMappingStatus.class, "process_mapping_status");
    }
}
