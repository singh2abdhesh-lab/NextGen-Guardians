package com.enterprise.ai.risk.engine;

import com.enterprise.ai.common.dto.RiskAssessRequest;
import com.enterprise.ai.common.dto.RiskScore.Action;
import com.enterprise.ai.common.dto.RiskScore.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Risk Rule Engine Policy Action Tests")
class RiskRuleEngineTest {

    private RiskRuleEngine ruleEngine;

    @BeforeEach
    void setUp() {
        ruleEngine = new RiskRuleEngine();
    }

    @Test
    @DisplayName("Should assign BLOCK action when RiskLevel is CRITICAL")
    void testCriticalRiskBlocks() {
        RiskAssessRequest request = RiskAssessRequest.builder().build();
        Action action = ruleEngine.evaluateAction(request, 85, RiskLevel.CRITICAL);
        assertEquals(Action.BLOCK, action);
    }

    @Test
    @DisplayName("Should assign BLOCK action when data sensitivity reaches 70")
    void testSensitivityThresholdBlocks() {
        RiskAssessRequest request = RiskAssessRequest.builder().dataSensitivity(75).build();
        Action action = ruleEngine.evaluateAction(request, 55, RiskLevel.MEDIUM);
        assertEquals(Action.BLOCK, action);
    }

    @Test
    @DisplayName("Should assign REVIEW action for HIGH risk requests")
    void testHighRiskReviewQueue() {
        RiskAssessRequest request = RiskAssessRequest.builder().dataSensitivity(30).build();
        Action action = ruleEngine.evaluateAction(request, 68, RiskLevel.HIGH);
        assertEquals(Action.REVIEW, action);
    }

    @Test
    @DisplayName("Should escalate intern accessing financial code to REVIEW queue")
    void testInternFinanceEscalation() {
        RiskAssessRequest request = RiskAssessRequest.builder()
                .userRole("INTERN")
                .promptCategory("FINANCE")
                .dataSensitivity(20)
                .build();

        Action action = ruleEngine.evaluateAction(request, 52, RiskLevel.MEDIUM);
        assertEquals(Action.REVIEW, action);
    }

    @Test
    @DisplayName("Should assign ALLOW action for standard compliant requests")
    void testStandardAllow() {
        RiskAssessRequest request = RiskAssessRequest.builder()
                .userRole("ENGINEER")
                .promptCategory("GENERAL")
                .dataSensitivity(0)
                .build();

        Action action = ruleEngine.evaluateAction(request, 22, RiskLevel.LOW);
        assertEquals(Action.ALLOW, action);
    }
}
