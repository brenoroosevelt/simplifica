package com.simplifica.infrastructure.hibernate;

import com.simplifica.domain.entity.ProcessDocumentationStatus;

/**
 * Hibernate UserType for ProcessDocumentationStatus enum.
 * Maps to PostgreSQL documentation_status type.
 */
public class ProcessDocumentationStatusType extends PostgreSQLEnumType<ProcessDocumentationStatus> {

    public ProcessDocumentationStatusType() {
        super(ProcessDocumentationStatus.class, "documentation_status");
    }
}
