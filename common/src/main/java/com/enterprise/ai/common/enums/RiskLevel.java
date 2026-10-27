package com.enterprise.ai.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categorized risk severity calculated by the multi-factor risk scoring engine")
public enum RiskLevel {
    @Schema(description = "Low risk (score < 30)")
    LOW(0, 29),

    @Schema(description = "Medium risk (score 30-59)")
    MEDIUM(30, 59),

    @Schema(description = "High risk (score 60-79) - requires elevated scrutiny or review")
    HIGH(60, 79),

    @Schema(description = "Critical risk (score >= 80) - immediate termination")
    CRITICAL(80, 100);

    private final int minScore;
    private final int maxScore;

    RiskLevel(int minScore, int maxScore) {
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public static RiskLevel fromScore(int score) {
        if (score >= 80) return CRITICAL;
        if (score >= 60) return HIGH;
        if (score >= 30) return MEDIUM;
        return LOW;
    }
}
