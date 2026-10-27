package com.enterprise.ai.gateway.llm;

import com.enterprise.ai.gateway.config.LlmProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Cost Calculator Unit Tests")
class CostCalculatorTest {

    private CostCalculator costCalculator;

    @BeforeEach
    void setUp() {
        LlmProperties properties = new LlmProperties();
        costCalculator = new CostCalculator(properties);
    }

    @Test
    @DisplayName("GPT-4o calculation matches expected rates")
    void testGpt4oCost() {
        // gpt-4o: $0.0025 per 1k in, $0.01 per 1k out
        // 1000 in = 0.0025, 1000 out = 0.0100 -> total = 0.0125
        double cost = costCalculator.calculateCost("gpt-4o", 1000, 1000);
        assertEquals(0.0125, cost);
    }

    @Test
    @DisplayName("GPT-4o-mini calculation reflects economy pricing")
    void testGpt4oMiniCost() {
        // gpt-4o-mini: $0.00015 per 1k in, $0.00060 per 1k out
        // 1000 in = 0.00015, 1000 out = 0.00060 -> total = 0.00075
        double cost = costCalculator.calculateCost("gpt-4o-mini", 1000, 1000);
        assertEquals(0.00075, cost);
    }

    @Test
    @DisplayName("Zero tokens incurs zero cost")
    void testZeroTokensCost() {
        double cost = costCalculator.calculateCost("gpt-4o", 0, 0);
        assertEquals(0.0, cost);
    }
}
