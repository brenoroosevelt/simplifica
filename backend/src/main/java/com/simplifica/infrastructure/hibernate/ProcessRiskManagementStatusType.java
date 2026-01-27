package com.simplifica.infrastructure.hibernate;

import com.simplifica.domain.entity.ProcessRiskManagementStatus;

/**
 * Hibernate UserType for ProcessRiskManagementStatus enum.
 * Maps to PostgreSQL risk_management_status type.
 */
public class ProcessRiskManagementStatusType extends PostgreSQLEnumType<ProcessRiskManagementStatus> {

    public ProcessRiskManagementStatusType() {
        super(ProcessRiskManagementStatus.class, "risk_management_status");
    }
}
