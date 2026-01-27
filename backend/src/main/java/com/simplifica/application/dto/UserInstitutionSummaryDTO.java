package com.simplifica.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO simplificado para exibir instituição vinculada ao usuário na listagem.
 *
 * Usado para exibir resumo de instituições vinculadas ao usuário na listagem
 * de usuários (para admins), sem carregar todos os detalhes desnecessários.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInstitutionSummaryDTO {

    /**
     * ID da instituição.
     */
    private UUID institutionId;

    /**
     * Nome completo da instituição.
     */
    private String institutionName;

    /**
     * Sigla da instituição.
     */
    private String institutionAcronym;
}
