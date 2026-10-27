package com.enterprise.ai.risk.service;

import com.enterprise.ai.common.dto.RiskAssessRequest;
import com.enterprise.ai.common.dto.RiskScore;
import com.enterprise.ai.common.dto.RiskScore.Action;
import com.enterprise.ai.common.dto.RiskScore.RiskLevel;
import com.enterprise.ai.risk.engine.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Risk Scoring Service Integration Tests")
class RiskScoringServiceTest {

    private RiskScoringService service;

    @BeforeEach
    void setUp() {
        List<FactorScorer> factorScorers = List.of(
                new DataSensitivityFactorScorer(),
                new UserRoleFactorScorer(),
                new PromptCategoryFactorScorer(),
                new TargetModelFactorScorer()
        );
        RiskCalculator calculator = new RiskCalculator(factorScorers);
        RiskRuleEngine ruleEngine = new RiskRuleEngine();
        service = new RiskScoringService(calculator, ruleEngine);
    }

    @Test
    @DisplayName("Should assess high-sensitivity request as CRITICAL and BLOCK")
    void testAssessCriticalRequest() {
        RiskAssessRequest request = RiskAssessRequest.builder()
                .requestId("req-assess-01")
                .dataSensitivity(95)
                .userRole("INTERN")
                .promptCategory("FINANCE")
                .model("gpt-4o")
                .build();

        RiskScore score = service.assess(request);

        assertNotNull(score);
        assertEquals("req-assess-01", score.getRequestId());
        assertEquals(RiskLevel.CRITICAL, score.getRiskLevel());
        assertEquals(Action.BLOCK, score.getAction());
        assertTrue(score.getTotalScore() >= 80);
    }

    @Test
    @DisplayName("Should assess low-sensitivity request as LOW and ALLOW")
    void testAssessLowRiskRequest() {
        RiskAssessRequest request = RiskAssessRequest.builder()
                .requestId("req-assess-02")
                .dataSensitivity(0)
                .userRole("SECURITY")
                .promptCategory("GENERAL")
                .model("gpt-4o")
                .build();

        RiskScore score = service.assess(request);

        assertNotNull(score);
        assertEquals(RiskLevel.LOW, score.getRiskLevel());
        assertEquals(Action.ALLOW, score.getAction());
        assertTrue(score.getTotalScore() < 30);
    }
}
