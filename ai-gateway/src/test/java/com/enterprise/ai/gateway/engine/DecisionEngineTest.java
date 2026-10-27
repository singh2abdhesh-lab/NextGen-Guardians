package com.enterprise.ai.gateway.engine;

import com.enterprise.ai.common.dto.QualityResult;
import com.enterprise.ai.common.dto.QualityResult.Verdict;
import com.enterprise.ai.common.dto.RiskScore;
import com.enterprise.ai.common.dto.RiskScore.Action;
import com.enterprise.ai.common.dto.RiskScore.RiskLevel;
import com.enterprise.ai.common.dto.ScanResult;
import com.enterprise.ai.common.dto.Violation;
import com.enterprise.ai.common.enums.ViolationType;
import com.enterprise.ai.gateway.engine.DecisionEngine.GateDecision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Decision Engine Unit Tests")
class DecisionEngineTest {

    private DecisionEngine decisionEngine;

    @BeforeEach
    void setUp() {
        decisionEngine = new DecisionEngine();
    }

    @Test
    @DisplayName("Clean scan and low risk score should produce ALLOW gate decision")
    void testCleanPromptGate() {
        ScanResult scan = ScanResult.builder()
                .blocked(false)
                .sensitivityScore(10)
                .violations(Collections.emptyList())
                .build();

        RiskScore risk = RiskScore.builder()
                .totalScore(15)
                .riskLevel(RiskLevel.LOW)
                .action(Action.ALLOW)
                .build();

        GateDecision decision = decisionEngine.evaluatePromptGate(scan, risk);

        assertTrue(decision.isAllowed());
        assertEquals("ALLOWED", decision.getAction());
        assertFalse(decision.isApprovalRequired());
    }

    @Test
    @DisplayName("Blocked scan result triggers BLOCKED gate decision")
    void testScanBlockedGate() {
        ScanResult scan = ScanResult.builder()
                .blocked(true)
                .sensitivityScore(90)
                .violations(List.of(Violation.builder()
                        .type(ViolationType.PROMPT_INJECTION)
                        .severity(Violation.Severity.CRITICAL)
                        .description("Jailbreak pattern")
                        .build()))
                .build();

        RiskScore risk = RiskScore.builder()
                .totalScore(85)
                .riskLevel(RiskLevel.CRITICAL)
                .action(Action.BLOCK)
                .build();

        GateDecision decision = decisionEngine.evaluatePromptGate(scan, risk);

        assertFalse(decision.isAllowed());
        assertEquals("BLOCKED", decision.getAction());
        assertTrue(decision.getReason().contains("Security scan violations"));
    }

    @Test
    @DisplayName("Risk score requiring REVIEW marks gate decision as REVIEW")
    void testRiskReviewGate() {
        ScanResult scan = ScanResult.builder()
                .blocked(false)
                .sensitivityScore(40)
                .violations(Collections.emptyList())
                .build();

        RiskScore risk = RiskScore.builder()
                .totalScore(65)
                .riskLevel(RiskLevel.HIGH)
                .action(Action.REVIEW)
                .build();

        GateDecision decision = decisionEngine.evaluatePromptGate(scan, risk);

        assertTrue(decision.isAllowed());
        assertEquals("REVIEW", decision.getAction());
        assertTrue(decision.isApprovalRequired());
    }

    @Test
    @DisplayName("Quality failure triggers post-completion review")
    void testPostCompletionQualityFailure() {
        GateDecision preGate = GateDecision.allowed();

        QualityResult quality = QualityResult.builder()
                .verdict(Verdict.FAIL)
                .hallucinationScore(0.85)
                .policyViolation(true)
                .policyViolations(List.of("Response echoes API key"))
                .approvalRequired(true)
                .build();

        GateDecision postGate = decisionEngine.evaluatePostCompletionGate(preGate, quality);

        assertTrue(postGate.isApprovalRequired());
        assertEquals("REVIEW", postGate.getAction());
        assertTrue(postGate.getReason().contains("Response echoes API key"));
    }
}
