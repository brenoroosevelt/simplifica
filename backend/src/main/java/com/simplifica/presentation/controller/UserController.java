package com.simplifica.presentation.controller;

import com.simplifica.application.dto.UserDTO;
import com.simplifica.application.service.UserService;
import com.simplifica.config.security.UserPrincipal;
import com.simplifica.domain.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
