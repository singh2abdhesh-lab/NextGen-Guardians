package com.enterprise.ai.risk.engine;

import com.enterprise.ai.common.constant.SecurityConstants;
import com.enterprise.ai.common.dto.RiskAssessRequest;
import org.springframework.stereotype.Component;

/**
 * Evaluates the sensitivity of detected data (PII, credentials, proprietary secrets).
 * Weight: 50%
 */
@Component
public class DataSensitivityFactorScorer implements FactorScorer {

    @Override
    public String getFactorName() {
        return "dataSensitivity";
    }

    @Override
    public double getWeight() {
        return SecurityConstants.WEIGHT_DATA_SENSITIVITY;
    }

    @Override
    public int calculateRawScore(RiskAssessRequest request) {
        if (request == null) {
            return 0;
        }

        int score = request.getDataSensitivity();

        // If request includes detected categories containing critical threats, enforce high floor
        if (request.getDetectedCategories() != null && !request.getDetectedCategories().isEmpty()) {
            boolean hasCritical = request.getDetectedCategories().stream()
                    .anyMatch(c -> c.toUpperCase().contains("MALICIOUS") ||
                                   c.toUpperCase().contains("SECRET") ||
                                   c.toUpperCase().contains("INJECTION"));
            if (hasCritical && score < 75) {
                score = 75;
            }
        }

        return Math.max(0, Math.min(100, score));
    }
}
