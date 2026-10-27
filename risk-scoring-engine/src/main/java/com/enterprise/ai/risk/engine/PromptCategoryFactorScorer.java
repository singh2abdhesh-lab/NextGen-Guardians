package com.enterprise.ai.risk.engine;

import com.enterprise.ai.common.constant.SecurityConstants;
import com.enterprise.ai.common.dto.RiskAssessRequest;
import com.enterprise.ai.common.enums.PromptCategory;
import org.springframework.stereotype.Component;

/**
 * Evaluates the domain sensitivity of the prompt category.
 * Weight: 20%
 */
@Component
public class PromptCategoryFactorScorer implements FactorScorer {

    @Override
    public String getFactorName() {
        return "promptCategory";
    }

    @Override
    public double getWeight() {
        return SecurityConstants.WEIGHT_PROMPT_CATEGORY;
    }

    @Override
    public int calculateRawScore(RiskAssessRequest request) {
        if (request == null || request.getPromptCategory() == null) {
            return 30;
        }

        String cat = request.getPromptCategory().trim().toUpperCase();
        return switch (cat) {
            case "CODE" -> PromptCategory.CODE.getBaselineRiskScore();
            case "FINANCE" -> PromptCategory.FINANCE.getBaselineRiskScore();
            case "HR" -> PromptCategory.HR.getBaselineRiskScore();
            case "LEGAL" -> PromptCategory.LEGAL.getBaselineRiskScore();
            case "HEALTHCARE" -> PromptCategory.HEALTHCARE.getBaselineRiskScore();
            case "GENERAL" -> PromptCategory.GENERAL.getBaselineRiskScore();
            default -> 40;
        };
    }
}
