package com.enterprise.ai.risk.engine;

import com.enterprise.ai.common.constant.SecurityConstants;
import com.enterprise.ai.common.dto.RiskAssessRequest;
import com.enterprise.ai.common.dto.RiskScore.Action;
import com.enterprise.ai.common.dto.RiskScore.RiskLevel;
import org.springframework.stereotype.Component;

/**
 * Enterprise Rule Engine applying governance policies to determine enforcement actions.
 */
@Component
public class RiskRuleEngine {

    /**
     * Determines whether an evaluated request should be ALLOWED, BLOCKED, or routed to REVIEW.
     */
    public Action evaluateAction(RiskAssessRequest request, int totalScore, RiskLevel riskLevel) {
        // Policy 1: Critical Threat Hard Block
        if (riskLevel == RiskLevel.CRITICAL ||
                (request != null && request.getDataSensitivity() >= SecurityConstants.SENSITIVITY_BLOCK_THRESHOLD)) {
            return Action.BLOCK;
        }

        // Policy 2: Intern High-Risk Escalation
        if (request != null && "INTERN".equalsIgnoreCase(request.getUserRole())) {
            String category = request.getPromptCategory() != null ? request.getPromptCategory().toUpperCase() : "";
            if (("FINANCE".equals(category) || "CODE".equals(category)) && totalScore >= 50) {
                return Action.REVIEW;
            }
        }

        // Policy 3: High-Risk Threshold Review Queue
        if (riskLevel == RiskLevel.HIGH) {
            return Action.REVIEW;
        }

        // Policy 4: Standard Compliant Traffic
        return Action.ALLOW;
    }
}
