package com.enterprise.ai.risk.engine;

import com.enterprise.ai.common.constant.SecurityConstants;
import com.enterprise.ai.common.dto.RiskAssessRequest;
import org.springframework.stereotype.Component;

/**
 * Evaluates the target LLM model infrastructure risk.
 * Weight: 15%
 */
@Component
public class TargetModelFactorScorer implements FactorScorer {

    @Override
    public String getFactorName() {
        return "modelUsed";
    }

    @Override
    public double getWeight() {
        return SecurityConstants.WEIGHT_TARGET_MODEL;
    }

    @Override
    public int calculateRawScore(RiskAssessRequest request) {
        if (request == null || request.getModel() == null) {
            return 40;
        }

        String model = request.getModel().trim().toLowerCase();
        if (model.contains("gpt-4o") || model.contains("gpt-4") || model.contains("claude-3-5")) {
            return 30;
        } else if (model.contains("gpt-4o-mini") || model.contains("gpt-3.5")) {
            return 40;
        } else {
            return 50;
        }
    }
}
