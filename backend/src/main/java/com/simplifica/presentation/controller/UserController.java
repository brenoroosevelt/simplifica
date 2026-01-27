package com.simplifica.presentation.controller;

import com.simplifica.application.dto.InstitutionDTO;
import com.simplifica.application.dto.UserDTO;
import com.simplifica.application.dto.UserInstitutionDTO;
import com.simplifica.application.service.UserInstitutionService;
import com.simplifica.application.service.UserService;
import com.simplifica.config.security.UserPrincipal;
import com.simplifica.domain.entity.User;
import com.simplifica.domain.entity.UserInstitution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User controller for user-related endpoints.
 *
 * Provides endpoints for retrieving and managing user information.
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserInstitutionService userInstitutionService;

    /**
     * Returns the complete profile of the currently authenticated user.
     *
     * @param userPrincipal the authenticated user principal
     * @return the user's complete profile as a UserDTO
     */
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        User user = userService.findById(userPrincipal.getId());
        UserDTO userDTO = UserDTO.fromEntity(user);

        return ResponseEntity.ok(userDTO);
    }

    /**
     * Returns all institutions that the currently authenticated user is linked to.
     *
     * Includes complete user-institution relationship data with roles, active status,
     * and link metadata.
     *
     * @param userPrincipal the authenticated user principal
     * @return list of user-institution relationships
     */
    @GetMapping("/institutions")
    public ResponseEntity<List<UserInstitutionDTO>> getUserInstitutions(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<UserInstitution> userInstitutions = userInstitutionService.getUserInstitutions(userPrincipal.getId());
        List<UserInstitutionDTO> institutions = userInstitutions.stream()
                .map(UserInstitutionDTO::fromEntitySimplified)
                .collect(Collectors.toList());
        return ResponseEntity.ok(institutions);
    }

    /**
     * Validates if the currently authenticated user has access to a specific institution.
     *
     * @param userPrincipal the authenticated user principal
     * @param institutionId the institution's UUID
     * @return a map containing hasAccess (boolean) and institutionId
     */
    @GetMapping("/institutions/{institutionId}/validate")
    public ResponseEntity<Map<String, Object>> validateInstitutionAccess(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID institutionId) {
        boolean hasAccess = userInstitutionService.userBelongsToInstitution(
                userPrincipal.getId(), institutionId);
        return ResponseEntity.ok(Map.of(
                "hasAccess", hasAccess,
                "institutionId", institutionId
        ));
    }
}
