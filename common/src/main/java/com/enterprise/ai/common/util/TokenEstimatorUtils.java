package com.enterprise.ai.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility for estimating token counts and calculating financial model spend.
 */
public final class TokenEstimatorUtils {

    private TokenEstimatorUtils() {
        // Prevent instantiation
    }

    /**
     * Approximate token count based on standard English 4 characters-per-token heuristic.
     */
    public static int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(text.length() / 4.0));
    }

    /**
     * Calculates estimated USD cost based on token counts and model pricing schedules.
     */
    public static double calculateCost(String model, int tokensIn, int tokensOut) {
        double costPer1kIn;
        double costPer1kOut;

        String m = model != null ? model.toLowerCase() : "gpt-4o";
        if (m.contains("gpt-4o-mini") || m.contains("gpt-3.5")) {
            costPer1kIn = 0.00015;
            costPer1kOut = 0.00060;
        } else if (m.contains("claude-3-5")) {
            costPer1kIn = 0.00300;
            costPer1kOut = 0.01500;
        } else {
            // Default gpt-4o schedule
            costPer1kIn = 0.00250;
            costPer1kOut = 0.01000;
        }

        double total = (tokensIn * (costPer1kIn / 1000.0)) + (tokensOut * (costPer1kOut / 1000.0));
        return BigDecimal.valueOf(total).setScale(5, RoundingMode.HALF_UP).doubleValue();
    }
}
