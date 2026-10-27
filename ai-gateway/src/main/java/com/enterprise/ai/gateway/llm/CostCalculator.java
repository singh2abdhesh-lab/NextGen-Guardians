package com.enterprise.ai.gateway.llm;

import com.enterprise.ai.gateway.config.LlmProperties;
import com.enterprise.ai.gateway.config.LlmProperties.ModelPricing;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Calculates financial costs in USD for model prompts and completions
 * based on tiered model pricing schedules.
 */
@Component
public class CostCalculator {

    private final LlmProperties properties;

    public CostCalculator(LlmProperties properties) {
        this.properties = properties;
    }

    /**
     * Computes the financial cost in USD.
     *
     * @param model     target LLM model name
     * @param tokensIn  prompt/input token count
     * @param tokensOut completion/output token count
     * @return calculated USD cost rounded to 6 decimal places
     */
    public double calculateCost(String model, int tokensIn, int tokensOut) {
        double costPer1kIn = properties.getCostPer1kInput();
        double costPer1kOut = properties.getCostPer1kOutput();

        String m = model != null ? model.toLowerCase() : properties.getDefaultModel().toLowerCase();

        // Check if explicit model override exists in properties
        Map<String, ModelPricing> customModels = properties.getModels();
        if (customModels != null && customModels.containsKey(m)) {
            ModelPricing pricing = customModels.get(m);
            costPer1kIn = pricing.getCostPer1kInput();
            costPer1kOut = pricing.getCostPer1kOutput();
        } else if (m.contains("gpt-4o-mini") || m.contains("gpt-3.5")) {
            costPer1kIn = 0.00015;
            costPer1kOut = 0.00060;
        } else if (m.contains("claude-3-5") || m.contains("sonnet")) {
            costPer1kIn = 0.00300;
            costPer1kOut = 0.01500;
        } else if (m.contains("claude-3-haiku")) {
            costPer1kIn = 0.00025;
            costPer1kOut = 0.00125;
        } else if (m.contains("gemini-1.5-pro")) {
            costPer1kIn = 0.00125;
            costPer1kOut = 0.00500;
        } else if (m.contains("gemini-1.5-flash")) {
            costPer1kIn = 0.000075;
            costPer1kOut = 0.00030;
        }

        double total = (tokensIn * (costPer1kIn / 1000.0)) + (tokensOut * (costPer1kOut / 1000.0));
        return BigDecimal.valueOf(total).setScale(6, RoundingMode.HALF_UP).doubleValue();
    }
}
