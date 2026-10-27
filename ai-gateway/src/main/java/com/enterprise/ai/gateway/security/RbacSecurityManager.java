package com.enterprise.ai.gateway.security;

import com.enterprise.ai.common.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Role-Based Access Control (RBAC) security manager enforcing enterprise authorization matrices,
 * model access tiers, and policy boundaries.
 */
@Component
public class RbacSecurityManager {

    private static final Logger log = LoggerFactory.getLogger(RbacSecurityManager.class);

    // Elevated models requiring senior authorization
    private static final Set<String> HIGH_TIER_MODELS = Set.of(
            "gpt-4o",
            "claude-3-5-sonnet",
            "gemini-1.5-pro"
    );

    /**
     * Assesses whether a user role is permitted to execute queries against a target model and category.
     *
     * @param role           caller UserRole
     * @param promptCategory category of prompt
     * @param model          target LLM model name
     * @return RbacAuthorizationResult
     */
    public RbacAuthorizationResult authorizePromptExecution(UserRole role, String promptCategory, String model) {
        if (role == null) {
            return RbacAuthorizationResult.denied("User role must be defined for RBAC evaluation");
        }

        // 1. ADMIN & SECURITY roles have unconstrained access
        if (role == UserRole.ADMIN || role == UserRole.SECURITY) {
            return RbacAuthorizationResult.granted("Unrestricted enterprise access granted for role: " + role.name());
        }

        // 2. ENGINEER access rules
        if (role == UserRole.ENGINEER) {
            return RbacAuthorizationResult.granted("Engineering role permitted for model: " + model);
        }

        // 3. ANALYST access rules
        if (role == UserRole.ANALYST) {
            if ("CODE".equalsIgnoreCase(promptCategory)) {
                log.info("RBAC note: Analyst invoking code evaluation category");
            }
            return RbacAuthorizationResult.granted("Analyst role authorized for standard workflows");
        }

        // 4. INTERN access restrictions (Strictest tier per enterprise policy)
        if (role == UserRole.INTERN) {
            // Interns cannot directly invoke high tier unmonitored models with finance categories
            if ("FINANCE".equalsIgnoreCase(promptCategory) && model != null && HIGH_TIER_MODELS.contains(model.toLowerCase())) {
                log.warn("RBAC violation: INTERN role denied direct access to high-tier model '{}' with category FINANCE", model);
                return RbacAuthorizationResult.denied("INTERN role restricted from executing FINANCE inquiries on frontier model '" + model + "' without supervisor approval");
            }
            return RbacAuthorizationResult.granted("Intern role permitted under supervised threshold");
        }

        return RbacAuthorizationResult.denied("Unrecognized authorization policy for role: " + role.name());
    }

    /**
     * Determines whether role has authority to approve or reject items in the human review queue.
     */
    public boolean canApproveReviewQueue(UserRole role) {
        return role == UserRole.ADMIN || role == UserRole.SECURITY;
    }

    /**
     * Determines whether role can inspect full forensic audit ledgers and PII payloads.
     */
    public boolean canViewAuditLedger(UserRole role) {
        return role == UserRole.ADMIN || role == UserRole.SECURITY || role == UserRole.ENGINEER || role == UserRole.ANALYST;
    }

    /**
     * Determines whether role can alter runtime guardrail thresholds and configuration policies.
     */
    public boolean canModifyPolicies(UserRole role) {
        return role == UserRole.ADMIN || role == UserRole.SECURITY;
    }

    public static class RbacAuthorizationResult {
        private final boolean authorized;
        private final String message;

        private RbacAuthorizationResult(boolean authorized, String message) {
            this.authorized = authorized;
            this.message = message;
        }

        public static RbacAuthorizationResult granted(String message) {
            return new RbacAuthorizationResult(true, message);
        }

        public static RbacAuthorizationResult denied(String message) {
            return new RbacAuthorizationResult(false, message);
        }

        public boolean isAuthorized() {
            return authorized;
        }

        public String getMessage() {
            return message;
        }
    }
}

