package com.simplifica.infrastructure.hibernate;

import com.simplifica.domain.entity.ProcessExternalGuidanceStatus;

/**
 * Hibernate UserType for ProcessExternalGuidanceStatus enum.
 * Maps to PostgreSQL external_guidance_status type.
 */
public class ProcessExternalGuidanceStatusType extends PostgreSQLEnumType<ProcessExternalGuidanceStatus> {

    public ProcessExternalGuidanceStatusType() {
        super(ProcessExternalGuidanceStatus.class, "external_guidance_status");
    }
}
