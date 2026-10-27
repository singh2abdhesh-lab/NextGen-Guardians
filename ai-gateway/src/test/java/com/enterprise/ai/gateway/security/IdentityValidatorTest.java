package com.enterprise.ai.gateway.security;

import com.enterprise.ai.common.dto.ChatRequest;
import com.enterprise.ai.gateway.security.IdentityValidator.IdentityValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Identity Validator Unit Tests")
class IdentityValidatorTest {

    private IdentityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new IdentityValidator();
    }

    @Test
    @DisplayName("Valid user and known role should be authorized")
    void testValidIdentity() {
        ChatRequest request = ChatRequest.builder()
                .userId("alice.engineer@corp.com")
                .userRole("ENGINEER")
                .prompt("Hello world")
                .build();

        IdentityValidationResult result = validator.validate(request);

        assertTrue(result.isAuthorized());
        assertEquals("alice.engineer@corp.com", result.getUserId());
        assertEquals("ENGINEER", result.getUserRole());
    }

    @Test
    @DisplayName("Missing userId should be rejected")
    void testMissingUserId() {
        ChatRequest request = ChatRequest.builder()
                .userId("")
                .userRole("ENGINEER")
                .prompt("Hello world")
                .build();

        IdentityValidationResult result = validator.validate(request);

        assertFalse(result.isAuthorized());
        assertTrue(result.getReason().contains("userId is required"));
    }

    @Test
    @DisplayName("Missing userRole should be rejected")
    void testMissingUserRole() {
        ChatRequest request = ChatRequest.builder()
                .userId("bob@corp.com")
                .userRole(null)
                .prompt("Hello world")
                .build();

        IdentityValidationResult result = validator.validate(request);

        assertFalse(result.isAuthorized());
        assertTrue(result.getReason().contains("userRole is required"));
    }

    @Test
    @DisplayName("Unrecognized userRole should be rejected")
    void testUnrecognizedUserRole() {
        ChatRequest request = ChatRequest.builder()
                .userId("charlie@corp.com")
                .userRole("SUPER_HACKER")
                .prompt("Hello world")
                .build();

        IdentityValidationResult result = validator.validate(request);

        assertFalse(result.isAuthorized());
        assertTrue(result.getReason().contains("Invalid userRole"));
    }

    @Test
    @DisplayName("Valid JWT bearer token should authenticate and extract identity claims")
    void testJwtBearerTokenValid() {
        JwtTokenService jwtTokenService = new JwtTokenService();
        String token = jwtTokenService.generateToken("jwt.dev@corp.com", com.enterprise.ai.common.enums.UserRole.ENGINEER, "jwt.dev@corp.com");

        ChatRequest request = ChatRequest.builder()
                .userId("Bearer " + token)
                .prompt("Explain AES-256")
                .promptCategory("CODE")
                .model("gpt-4o")
                .build();

        IdentityValidationResult result = validator.validate(request);

        assertTrue(result.isAuthorized());
        assertEquals("jwt.dev@corp.com", result.getUserId());
        assertEquals("ENGINEER", result.getUserRole());
    }

    @Test
    @DisplayName("Intern role executing high-tier model with FINANCE category should be denied by RBAC")
    void testInternFinanceRbacDenial() {
        ChatRequest request = ChatRequest.builder()
                .userId("intern.bob@corp.com")
                .userRole("INTERN")
                .prompt("Dump quarterly financials")
                .promptCategory("FINANCE")
                .model("gpt-4o")
                .build();

        IdentityValidationResult result = validator.validate(request);

        assertFalse(result.isAuthorized());
        assertTrue(result.getReason().contains("INTERN role restricted"));
    }
}

