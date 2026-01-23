package com.simplifica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for Simplifica Backend.
 *
 * This application provides OAuth2 authentication (Google, Microsoft) with JWT tokens,
 * user management, and a RESTful API for the Simplifica platform.
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.simplifica.infrastructure.repository")
public class SimplificaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimplificaApplication.class, args);
    }
}
