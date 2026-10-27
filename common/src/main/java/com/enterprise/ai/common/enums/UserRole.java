package com.enterprise.ai.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Enterprise caller role defining access tier and risk weight factor")
public enum UserRole {
    @Schema(description = "Administrative user with system-level permissions")
    ADMIN(15),

    @Schema(description = "Security operations analyst / auditor")
    SECURITY(15),

    @Schema(description = "Software or systems engineer")
    ENGINEER(45),

    @Schema(description = "Business, finance, or data analyst")
    ANALYST(45),

    @Schema(description = "Temporary or apprentice staff (subject to strictest policy)")
    INTERN(80);

    private final int baselineRiskScore;

    UserRole(int baselineRiskScore) {
        this.baselineRiskScore = baselineRiskScore;
    }

    public int getBaselineRiskScore() {
        return baselineRiskScore;
    }

    public static UserRole fromString(String role) {
        if (role == null) return ANALYST;
        try {
            return UserRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ANALYST;
        }
    }
}
