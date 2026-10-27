package com.enterprise.ai.quality.checker;

import com.enterprise.ai.common.dto.QualityResult;
import com.enterprise.ai.common.dto.QualityResult.Verdict;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Quality Score Calculator Unit Tests")
class QualityScoreCalculatorTest {

    private QualityScoreCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new QualityScoreCalculator();
    }

    @Test
    @DisplayName("Clean response with low hallucination score renders PASS verdict")
    void testPassVerdict() {
        QualityResult result = calculator.calculate(
                "req-pass-1",
                0.10,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertNotNull(result);
        assertEquals("req-pass-1", result.getRequestId());
        assertEquals(Verdict.PASS, result.getVerdict());
        assertEquals(0.10, result.getHallucinationScore());
        assertFalse(result.isPolicyViolation());
        assertTrue(result.getPolicyViolations().isEmpty());
        assertFalse(result.isApprovalRequired());
    }

    @Test
    @DisplayName("Moderate hallucination score renders REVIEW verdict requiring approval")
    void testReviewVerdict() {
        QualityResult result = calculator.calculate(
                "req-rev-1",
                0.45,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertEquals(Verdict.REVIEW, result.getVerdict());
        assertEquals(0.45, result.getHallucinationScore());
        assertFalse(result.isPolicyViolation());
        assertTrue(result.isApprovalRequired());
    }

    @Test
    @DisplayName("High hallucination score renders FAIL verdict")
    void testFailVerdictDueToHallucination() {
        QualityResult result = calculator.calculate(
                "req-fail-1",
                0.85,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );

        assertEquals(Verdict.FAIL, result.getVerdict());
        assertEquals(0.85, result.getHallucinationScore());
        assertTrue(result.isApprovalRequired());
    }

    @Test
    @DisplayName("Secret or PII violations immediately render FAIL verdict and flag policyViolation")
    void testFailVerdictDueToViolations() {
        QualityResult result = calculator.calculate(
                "req-breach-1",
                0.05,
                List.of("Response echoes secret token from prompt"),
                List.of("Response echoes SSN from prompt"),
                Collections.emptyList()
        );

        assertEquals(Verdict.FAIL, result.getVerdict());
        assertTrue(result.isPolicyViolation());
        assertEquals(2, result.getPolicyViolations().size());
        assertTrue(result.isApprovalRequired());
    }
}
