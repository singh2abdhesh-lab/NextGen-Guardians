package com.enterprise.ai.risk.engine;

import com.enterprise.ai.common.constant.SecurityConstants;
import com.enterprise.ai.common.dto.RiskAssessRequest;
import com.enterprise.ai.common.enums.UserRole;
import org.springframework.stereotype.Component;

/**
 * Evaluates caller authorization tier and associated baseline risk.
 * Weight: 25%
 */
@Component
public class UserRoleFactorScorer implements FactorScorer {

    @Override
    public String getFactorName() {
        return "userRole";
    }

    @Override
    public double getWeight() {
        return SecurityConstants.WEIGHT_USER_ROLE;
    }

    @Override
    public int calculateRawScore(RiskAssessRequest request) {
        if (request == null || request.getUserRole() == null) {
            return 50;
        }

        String role = request.getUserRole().trim().toUpperCase();
        return switch (role) {
            case "ADMIN", "SECURITY" -> UserRole.ADMIN.getBaselineRiskScore();
            case "ENGINEER" -> UserRole.ENGINEER.getBaselineRiskScore();
            case "ANALYST" -> UserRole.ANALYST.getBaselineRiskScore();
            case "INTERN" -> UserRole.INTERN.getBaselineRiskScore();
            default -> 50;
        };
    }
}
