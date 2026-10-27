package com.enterprise.ai.common.exception;

import com.enterprise.ai.common.enums.RiskLevel;

/**
 * Thrown when the multi-factor weighted risk score exceeds policy thresholds.
 */
public class RiskThresholdExceededException extends AiGuardianException {

    private final int calculatedScore;
    private final RiskLevel riskLevel;

    public RiskThresholdExceededException(String message, int calculatedScore, RiskLevel riskLevel) {
        super(message, "RISK_THRESHOLD_EXCEEDED", 403);
        this.calculatedScore = calculatedScore;
        this.riskLevel = riskLevel;
    }

    public int getCalculatedScore() {
        return calculatedScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }
}
