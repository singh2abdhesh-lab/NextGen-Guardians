package com.enterprise.ai.gateway.security;

import com.enterprise.ai.common.enums.UserRole;
import com.enterprise.ai.gateway.security.RateLimitingService.RateLimitResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Rate Limiting Service Unit Tests")
class RateLimitingServiceTest {

    private RateLimitingService rateLimitingService;

    @BeforeEach
    void setUp() {
        rateLimitingService = new RateLimitingService();
    }

    @Test
    @DisplayName("Requests within quota should be allowed and decrement remaining tokens")
    void testRateLimitWithinQuota() {
        String clientIp = "192.168.1.100";
        RateLimitResult result1 = rateLimitingService.checkLimit(clientIp, UserRole.INTERN);

        assertTrue(result1.isAllowed());
        assertEquals(20, result1.getLimit());
        assertEquals(19, result1.getRemaining());

        RateLimitResult result2 = rateLimitingService.checkLimit(clientIp, UserRole.INTERN);
        assertTrue(result2.isAllowed());
        assertEquals(18, result2.getRemaining());
    }

    @Test
    @DisplayName("Exceeding quota should reject request and report zero remaining")
    void testRateLimitExceeded() {
        String clientIp = "192.168.1.101";

        // Intern quota is 20
        for (int i = 0; i < 20; i++) {
            RateLimitResult res = rateLimitingService.checkLimit(clientIp, UserRole.INTERN);
            assertTrue(res.isAllowed(), "Request #" + (i + 1) + " should be allowed");
        }

        // 21st request should be rejected
        RateLimitResult blocked = rateLimitingService.checkLimit(clientIp, UserRole.INTERN);
        assertFalse(blocked.isAllowed(), "21st request should exceed 20 req/min limit");
        assertEquals(0, blocked.getRemaining());
        assertTrue(blocked.getResetSeconds() > 0);
    }
}

