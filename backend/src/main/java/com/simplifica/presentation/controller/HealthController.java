package com.simplifica.presentation.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for public endpoints.
 *
 * Provides a simple health check endpoint that can be used by
 * monitoring tools, load balancers, and the Docker healthcheck.
 */
@RestController
@RequestMapping("/public")
public class HealthController {

    @Value("${spring.application.name}")
    private String applicationName;

    /**
     * Returns the health status of the application.
     *
     * @return a response containing status, timestamp, and service name
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", applicationName);

        return ResponseEntity.ok(response);
    }
}
