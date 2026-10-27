package com.enterprise.ai.risk.engine;

import com.enterprise.ai.common.dto.RiskAssessRequest;

/**
 * Strategy interface for individual risk evaluation factors.
 */
public interface FactorScorer {

    /**
     * Name identifier of the factor (e.g. dataSensitivity, userRole).
     */
    String getFactorName();

    /**
     * Weight coefficient in the multi-factor calculation (between 0.0 and 1.0).
     */
    double getWeight();

    /**
     * Computes raw risk score for this dimension (0 to 100).
     */
    int calculateRawScore(RiskAssessRequest request);

    /**
     * Calculates the weighted contribution points toward total composite risk.
     */
    default int calculateWeightedPoints(RiskAssessRequest request) {
        int raw = calculateRawScore(request);
        return (int) Math.round(raw * getWeight());
    }
}
