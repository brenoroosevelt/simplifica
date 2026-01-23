package com.simplifica.presentation.controller;

import com.simplifica.application.dto.UserDTO;
import com.simplifica.application.service.UserService;
import com.simplifica.config.security.UserPrincipal;
import com.simplifica.domain.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication controller for authenticated endpoints.
 *
 * Provides endpoints for retrieving information about the
 * currently authenticated user.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns the currently authenticated user's information.
     *
     * @param principal the authenticated user principal
     * @return the user's information as a UserDTO
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.findById(principal.getId());
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }
}
