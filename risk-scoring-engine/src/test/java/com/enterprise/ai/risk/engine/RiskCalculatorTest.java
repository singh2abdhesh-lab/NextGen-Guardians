package com.enterprise.ai.risk.engine;

import com.enterprise.ai.common.dto.RiskAssessRequest;
import com.enterprise.ai.common.dto.RiskScore.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Risk Calculator Weighted Scoring Tests")
class RiskCalculatorTest {

    private RiskCalculator calculator;

    @BeforeEach
    void setUp() {
        List<FactorScorer> scorers = List.of(
                new DataSensitivityFactorScorer(),
                new UserRoleFactorScorer(),
                new PromptCategoryFactorScorer(),
                new TargetModelFactorScorer()
        );
        calculator = new RiskCalculator(scorers);
    }

    @Test
    @DisplayName("Should evaluate high data sensitivity as CRITICAL risk level")
    void testCriticalRiskCalculation() {
        RiskAssessRequest request = RiskAssessRequest.builder()
                .requestId("req-crit")
                .dataSensitivity(90)
                .userRole("INTERN")
                .promptCategory("CODE")
                .model("gpt-4o")
                .build();

        RiskCalculator.CalculatedRisk risk = calculator.compute(request);

        assertNotNull(risk);
        assertTrue(risk.totalScore() >= 80, "Expected score >= 80 but was " + risk.totalScore());
        assertEquals(RiskLevel.CRITICAL, risk.riskLevel());
        assertTrue(risk.factorScores().containsKey("dataSensitivity"));
        assertTrue(risk.factorScores().containsKey("userRole"));
    }

    @Test
    @DisplayName("Should evaluate compliant admin request as LOW risk level")
    void testLowRiskCalculation() {
        RiskAssessRequest request = RiskAssessRequest.builder()
                .requestId("req-low")
                .dataSensitivity(0)
                .userRole("ADMIN")
                .promptCategory("GENERAL")
                .model("gpt-4o")
                .build();

        RiskCalculator.CalculatedRisk risk = calculator.compute(request);

        assertNotNull(risk);
        assertTrue(risk.totalScore() < 30, "Expected score < 30 but was " + risk.totalScore());
        assertEquals(RiskLevel.LOW, risk.riskLevel());
    }
}
