package com.enterprise.ai.gateway.security;

import com.enterprise.ai.common.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe sliding window token bucket rate limiter delivering role-tiered quota enforcement
 * and standard RFC 6585 headers (X-RateLimit-Limit, X-RateLimit-Remaining, X-RateLimit-Reset).
 */
@Component
public class RateLimitingService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingService.class);
    private static final long WINDOW_SECONDS = 60L;

    // Concurrent in-memory bucket store: Key -> WindowBucket
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    /**
     * Determines whether a client request is permitted under role-specific rate limits.
     *
     * @param clientIdentifier IP address or userId
     * @param role             caller UserRole
     * @return RateLimitResult containing allowance, remaining tokens, and reset epoch
     */
    public RateLimitResult checkLimit(String clientIdentifier, UserRole role) {
        if (clientIdentifier == null || clientIdentifier.isBlank()) {
            clientIdentifier = "anonymous-client";
        }

        long maxQuota = getRoleQuotaPerMinute(role);
        long now = Instant.now().getEpochSecond();

        // The allow decision is made inside compute() so decrement, window reset, and rejection
        // stay atomic per key under concurrent access.
        boolean[] allowedHolder = new boolean[1];
        TokenBucket bucket = buckets.compute(clientIdentifier, (key, existing) -> {
            if (existing == null || now - existing.windowStartEpoch >= WINDOW_SECONDS) {
                allowedHolder[0] = true;
                return new TokenBucket(now, maxQuota - 1);
            }
            if (existing.remainingTokens > 0) {
                existing.remainingTokens--;
                allowedHolder[0] = true;
                return existing;
            }
            allowedHolder[0] = false;
            return existing;
        });

        long resetSeconds = Math.max(1, (bucket.windowStartEpoch + WINDOW_SECONDS) - now);
        boolean allowed = allowedHolder[0];

        if (!allowed) {
            log.warn("Rate limit exceeded for client='{}' role='{}' (limit={} req/min)", clientIdentifier, role, maxQuota);
        }

        return new RateLimitResult(allowed, maxQuota, Math.max(0, bucket.remainingTokens), resetSeconds);
    }

    private long getRoleQuotaPerMinute(UserRole role) {
        if (role == null) return 30L;
        return switch (role) {
            case ADMIN, SECURITY -> 300L;
            case ENGINEER -> 100L;
            case ANALYST -> 60L;
            case INTERN -> 20L;
        };
    }

    private static class TokenBucket {
        final long windowStartEpoch;
        long remainingTokens;

        TokenBucket(long windowStartEpoch, long remainingTokens) {
            this.windowStartEpoch = windowStartEpoch;
            this.remainingTokens = remainingTokens;
        }
    }

    public static class RateLimitResult {
        private final boolean allowed;
        private final long limit;
        private final long remaining;
        private final long resetSeconds;

        public RateLimitResult(boolean allowed, long limit, long remaining, long resetSeconds) {
            this.allowed = allowed;
            this.limit = limit;
            this.remaining = remaining;
            this.resetSeconds = resetSeconds;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public long getLimit() {
            return limit;
        }

        public long getRemaining() {
            return remaining;
        }

        public long getResetSeconds() {
            return resetSeconds;
        }
    }
}

