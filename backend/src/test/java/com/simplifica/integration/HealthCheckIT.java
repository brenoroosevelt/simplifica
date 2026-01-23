package com.simplifica.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the health check endpoint.
 *
 * This test verifies the complete Spring Boot application context
 * with the health endpoint working correctly.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HealthCheckIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnHealthStatusWhenAccessingPublicHealthEndpoint() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
                "/public/health",
                Map.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("status");
        assertThat(response.getBody()).containsKey("timestamp");
        assertThat(response.getBody()).containsKey("service");
        assertThat(response.getBody().get("status")).isEqualTo("UP");
        assertThat(response.getBody().get("service")).isEqualTo("claude-agents");
    }
}
