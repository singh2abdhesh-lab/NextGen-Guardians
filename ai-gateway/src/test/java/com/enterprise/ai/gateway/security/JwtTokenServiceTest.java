package com.enterprise.ai.gateway.security;

import com.enterprise.ai.common.enums.UserRole;
import com.enterprise.ai.gateway.security.JwtTokenService.JwtClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT Token Service Unit Tests")
class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenService("test-secret-key-32-bytes-minimum-length-security", 3600L);
    }

    @Test
    @DisplayName("Generated JWT token should be successfully parsed and claims verified")
    void testGenerateAndValidateToken() {
        String token = jwtTokenService.generateToken("user.alice@corp.com", UserRole.ENGINEER, "user.alice@corp.com");

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);

        boolean isValid = jwtTokenService.validateToken(token);
        assertTrue(isValid, "Token should be valid immediately after generation");

        JwtClaims claims = jwtTokenService.parseClaims(token);
        assertNotNull(claims);
        assertEquals("user.alice@corp.com", claims.getSub());
        assertEquals(UserRole.ENGINEER, claims.getRole());
        assertEquals("enterprise-ai-guardian", claims.getIss());
        assertTrue(claims.getExp() > claims.getIat());
    }

    @Test
    @DisplayName("Tampered JWT signature should fail verification")
    void testTamperedTokenFails() {
        String token = jwtTokenService.generateToken("user.bob@corp.com", UserRole.ANALYST, "user.bob@corp.com");
        String[] parts = token.split("\\.");

        // Tamper with payload
        String tamperedToken = parts[0] + ".eyJzdWIiOiJoYWNrZXIifQ." + parts[2];

        assertFalse(jwtTokenService.validateToken(tamperedToken));
        assertThrows(SecurityException.class, () -> jwtTokenService.parseClaims(tamperedToken));
    }

    @Test
    @DisplayName("Malformed JWT string should throw SecurityException")
    void testMalformedTokenThrows() {
        assertFalse(jwtTokenService.validateToken("not-a-valid-jwt"));
        assertThrows(IllegalArgumentException.class, () -> jwtTokenService.parseClaims(""));
    }
}

