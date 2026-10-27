package com.enterprise.ai.gateway.config;

import com.enterprise.ai.common.dto.ErrorResponse;
import com.enterprise.ai.common.enums.UserRole;
import com.enterprise.ai.gateway.security.RateLimitingService;
import com.enterprise.ai.gateway.security.RateLimitingService.RateLimitResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * High-performance reactive WebFilter enforcing OWASP Recommended Security Headers,
 * distributed X-Correlation-ID tracking, and rate limiting quota evaluation.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(SecurityWebFilter.class);
    private static final String CORRELATION_HEADER = "X-Correlation-Id";

    private final RateLimitingService rateLimitingService;
    private final ObjectMapper objectMapper;

    public SecurityWebFilter(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 1. Correlation ID management
        String correlationId = request.getHeaders().getFirst(CORRELATION_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        final String finalCorrelationId = correlationId;

        // 2. Attach OWASP Recommended Secure Headers on commit
        response.beforeCommit(() -> {
            HttpHeaders headers = response.getHeaders();
            headers.set(CORRELATION_HEADER, finalCorrelationId);
            headers.set("X-Content-Type-Options", "nosniff");
            headers.set("X-Frame-Options", "DENY");
            headers.set("X-XSS-Protection", "0");
            headers.set("Content-Security-Policy", "default-src 'self'; frame-ancestors 'none';");
            headers.set("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
            headers.set("Referrer-Policy", "strict-origin-when-cross-origin");
            headers.set("Permissions-Policy", "geolocation=(), camera=(), microphone=()");
            headers.set("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            headers.set("Pragma", "no-cache");
            return Mono.empty();
        });

        // 3. Enforce Rate Limiting for AI endpoints (/api/v1/ai/** and /api/v1/gateway/**)
        String path = request.getURI().getPath();
        if (path.startsWith("/api/v1/ai/") || path.startsWith("/api/v1/gateway/")) {
            String clientIp = extractClientIp(request);
            String roleHeader = request.getHeaders().getFirst("X-User-Role");
            UserRole role = UserRole.fromString(roleHeader != null ? roleHeader : "ENGINEER");

            RateLimitResult rateLimit = rateLimitingService.checkLimit(clientIp, role);

            // Add rate limit headers to response
            response.beforeCommit(() -> {
                HttpHeaders headers = response.getHeaders();
                headers.set("X-RateLimit-Limit", String.valueOf(rateLimit.getLimit()));
                headers.set("X-RateLimit-Remaining", String.valueOf(rateLimit.getRemaining()));
                headers.set("X-RateLimit-Reset", String.valueOf(rateLimit.getResetSeconds()));
                return Mono.empty();
            });

            if (!rateLimit.isAllowed()) {
                log.warn("Rate limit triggered for client='{}' path='{}'", clientIp, path);
                return renderRateLimitError(exchange, rateLimit.getResetSeconds(), path);
            }
        }

        return chain.filter(exchange);
    }

    private Mono<Void> renderRateLimitError(ServerWebExchange exchange, long resetSeconds, String path) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set("Retry-After", String.valueOf(resetSeconds));

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .error("Too Many Requests")
                .message("API request quota exceeded. Rate limit reset in " + resetSeconds + " seconds.")
                .errorCode("RATE_LIMIT_EXCEEDED")
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            byte[] fallback = "{\"error\":\"Too Many Requests\"}".getBytes(StandardCharsets.UTF_8);
            return response.writeWith(Mono.just(response.bufferFactory().wrap(fallback)));
        }
    }

    private String extractClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        if (request.getRemoteAddress() != null && request.getRemoteAddress().getAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "127.0.0.1";
    }
}

