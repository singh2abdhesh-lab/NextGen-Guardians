package com.enterprise.ai.gateway.security;

import com.enterprise.ai.common.dto.ChatRequest;
import com.enterprise.ai.common.enums.UserRole;
import com.enterprise.ai.gateway.security.JwtTokenService.JwtClaims;
import com.enterprise.ai.gateway.security.RbacSecurityManager.RbacAuthorizationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Validates caller identity, verifies cryptographic JWT signatures, and enforces RBAC privileges
 * before admitting requests into the AI Guardian governance pipeline.
 */
@Component
public class IdentityValidator {

    private static final Logger log = LoggerFactory.getLogger(IdentityValidator.class);

    private final JwtTokenService jwtTokenService;
    private final RbacSecurityManager rbacSecurityManager;

    public IdentityValidator() {
        this(new JwtTokenService(), new RbacSecurityManager());
    }

    public IdentityValidator(JwtTokenService jwtTokenService, RbacSecurityManager rbacSecurityManager) {
        this.jwtTokenService = jwtTokenService;
        this.rbacSecurityManager = rbacSecurityManager;
    }

    /**
     * Validates whether the incoming ChatRequest has an authorized caller identity (via JWT or validated credentials)
     * and evaluates RBAC permissions against target model and category.
     *
     * @param request the chat request payload
     * @return IdentityValidationResult detailing status, claims, and RBAC justification
     */
    public IdentityValidationResult validate(ChatRequest request) {
        if (request == null) {
            return IdentityValidationResult.rejected("Request payload cannot be null");
        }

        String rawToken = extractBearerToken(request.getUserId());

        // 1. Check if token is a signed JWT — only when the userId carries an explicit Bearer prefix,
        //    so plain credential-style identifiers (including email addresses) take the fallback path.
        if (rawToken != null && request.getUserId() != null && request.getUserId().startsWith("Bearer ")) {
            if (!jwtTokenService.validateToken(rawToken)) {
                log.warn("Identity validation rejected: invalid or expired JWT token");
                return IdentityValidationResult.rejected("UNAUTHORIZED: Invalid, expired, or tampered JWT token");
            }

            try {
                JwtClaims claims = jwtTokenService.parseClaims(rawToken);
                RbacAuthorizationResult rbacResult = rbacSecurityManager.authorizePromptExecution(
                        claims.getRole(),
                        request.getPromptCategory(),
                        request.getModel()
                );

                if (!rbacResult.isAuthorized()) {
                    log.warn("RBAC check failed for JWT user '{}' role '{}': {}", claims.getSub(), claims.getRole(), rbacResult.getMessage());
                    return IdentityValidationResult.rejected("FORBIDDEN: " + rbacResult.getMessage());
                }

                log.debug("JWT identity and RBAC authorized for user={} role={}", claims.getSub(), claims.getRole());
                return IdentityValidationResult.authorized(claims.getSub(), claims.getRole().name());
            } catch (Exception e) {
                return IdentityValidationResult.rejected("UNAUTHORIZED: Failed to decode JWT claims: " + e.getMessage());
            }
        }

        // 2. Fall back to credential-based validation
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            log.warn("Identity validation rejected: missing userId");
            return IdentityValidationResult.rejected("UNAUTHORIZED: userId is required");
        }

        if (request.getUserRole() == null || request.getUserRole().isBlank()) {
            log.warn("Identity validation rejected: missing userRole for user={}", request.getUserId());
            return IdentityValidationResult.rejected("UNAUTHORIZED: userRole is required");
        }

        String normalizedRole = request.getUserRole().trim().toUpperCase();
        boolean validRole = Arrays.stream(UserRole.values())
                .anyMatch(r -> r.name().equalsIgnoreCase(normalizedRole));

        if (!validRole) {
            log.warn("Identity validation rejected: unrecognized userRole='{}' for user={}", request.getUserRole(), request.getUserId());
            return IdentityValidationResult.rejected("FORBIDDEN: Invalid userRole '" + request.getUserRole() + "'");
        }

        UserRole parsedRole = UserRole.fromString(normalizedRole);
        RbacAuthorizationResult rbacResult = rbacSecurityManager.authorizePromptExecution(
                parsedRole,
                request.getPromptCategory(),
                request.getModel()
        );

        if (!rbacResult.isAuthorized()) {
            log.warn("RBAC authorization failed for user '{}' role '{}': {}", request.getUserId(), parsedRole, rbacResult.getMessage());
            return IdentityValidationResult.rejected("FORBIDDEN: " + rbacResult.getMessage());
        }

        log.debug("Identity validated successfully for user={} role={}", request.getUserId(), normalizedRole);
        return IdentityValidationResult.authorized(request.getUserId(), normalizedRole);
    }

    private String extractBearerToken(String candidate) {
        if (candidate == null) return null;
        if (candidate.startsWith("Bearer ")) {
            return candidate.substring(7).trim();
        }
        return candidate.trim();
    }

    public static class IdentityValidationResult {
        private final boolean authorized;
        private final String userId;
        private final String userRole;
        private final String reason;

        private IdentityValidationResult(boolean authorized, String userId, String userRole, String reason) {
            this.authorized = authorized;
            this.userId = userId;
            this.userRole = userRole;
            this.reason = reason;
        }

        public static IdentityValidationResult authorized(String userId, String userRole) {
            return new IdentityValidationResult(true, userId, userRole, "Identity authenticated and role authorized");
        }

        public static IdentityValidationResult rejected(String reason) {
            return new IdentityValidationResult(false, null, null, reason);
        }

        public boolean isAuthorized() {
            return authorized;
        }

        public String getUserId() {
            return userId;
        }

        public String getUserRole() {
            return userRole;
        }

        public String getReason() {
            return reason;
        }
    }
}

