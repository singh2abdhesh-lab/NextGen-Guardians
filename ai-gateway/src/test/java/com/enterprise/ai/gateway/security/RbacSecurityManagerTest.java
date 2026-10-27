package com.enterprise.ai.gateway.security;

import com.enterprise.ai.common.enums.UserRole;
import com.enterprise.ai.gateway.security.RbacSecurityManager.RbacAuthorizationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RBAC Security Manager Unit Tests")
class RbacSecurityManagerTest {

    private RbacSecurityManager rbacSecurityManager;

    @BeforeEach
    void setUp() {
        rbacSecurityManager = new RbacSecurityManager();
    }

    @Test
    @DisplayName("ADMIN role should have unrestricted access to all models and categories")
    void testAdminUnrestrictedAccess() {
        RbacAuthorizationResult result = rbacSecurityManager.authorizePromptExecution(
                UserRole.ADMIN,
                "FINANCE",
                "gpt-4o"
        );

        assertTrue(result.isAuthorized());
        assertTrue(rbacSecurityManager.canApproveReviewQueue(UserRole.ADMIN));
        assertTrue(rbacSecurityManager.canModifyPolicies(UserRole.ADMIN));
        assertTrue(rbacSecurityManager.canViewAuditLedger(UserRole.ADMIN));
    }

    @Test
    @DisplayName("INTERN role should be restricted from high-tier models with FINANCE category")
    void testInternFinanceRestriction() {
        RbacAuthorizationResult deniedResult = rbacSecurityManager.authorizePromptExecution(
                UserRole.INTERN,
                "FINANCE",
                "gpt-4o"
        );

        assertFalse(deniedResult.isAuthorized());
        assertTrue(deniedResult.getMessage().contains("INTERN role restricted"));

        // General non-finance query should be permitted
        RbacAuthorizationResult allowedResult = rbacSecurityManager.authorizePromptExecution(
                UserRole.INTERN,
                "GENERAL",
                "gpt-4o-mini"
        );
        assertTrue(allowedResult.isAuthorized());
    }

    @Test
    @DisplayName("ENGINEER and ANALYST cannot approve human review queue")
    void testNonAdminCannotApproveQueue() {
        assertFalse(rbacSecurityManager.canApproveReviewQueue(UserRole.ENGINEER));
        assertFalse(rbacSecurityManager.canApproveReviewQueue(UserRole.ANALYST));
        assertFalse(rbacSecurityManager.canApproveReviewQueue(UserRole.INTERN));
    }
}
