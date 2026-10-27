package com.enterprise.ai.risk.engine;

import com.enterprise.ai.common.dto.RiskAssessRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Data Sensitivity Factor Scorer Tests")
class DataSensitivityFactorScorerTest {

    private DataSensitivityFactorScorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new DataSensitivityFactorScorer();
    }

    @Test
    @DisplayName("Should apply 40% weight to raw sensitivity score")
    void testWeightedContribution() {
        RiskAssessRequest request = RiskAssessRequest.builder()
                .dataSensitivity(50)
                .build();

        assertEquals(0.40, scorer.getWeight());
        assertEquals(50, scorer.calculateRawScore(request));
        assertEquals(20, scorer.calculateWeightedPoints(request)); // 50 * 0.40 = 20
    }

    @Test
    @DisplayName("Should enforce minimum floor of 75 if critical category is detected")
    void testCriticalCategoryFloor() {
        RiskAssessRequest request = RiskAssessRequest.builder()
                .dataSensitivity(30)
                .detectedCategories(List.of("MALICIOUS_PROMPT"))
                .build();

        assertEquals(75, scorer.calculateRawScore(request));
        assertEquals(30, scorer.calculateWeightedPoints(request)); // 75 * 0.40 = 30
    }
}
