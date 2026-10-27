package com.enterprise.ai.scanner.detector;

import com.enterprise.ai.common.dto.Violation;
import com.enterprise.ai.common.dto.Violation.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Confidential Data Detector Unit Tests")
class ConfidentialDataDetectorTest {

    private ConfidentialDataDetector detector;

    @BeforeEach
    void setUp() {
        detector = new ConfidentialDataDetector();
    }

    @Test
    @DisplayName("Should detect STRICTLY CONFIDENTIAL corporate markings")
    void testDetectConfidentialBanner() {
        String prompt = "STRICTLY CONFIDENTIAL - Do not share outside board members.";
        List<Violation> violations = detector.detect(prompt);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "CONFIDENTIAL_MARKING".equals(v.getType())));
        assertTrue(violations.stream().anyMatch(v -> v.getSeverity() == Severity.CRITICAL));
    }

    @Test
    @DisplayName("Should detect trade secrets discussion")
    void testDetectTradeSecrets() {
        String prompt = "Here is our trade secret algorithm for high-frequency trading latency reduction.";
        List<Violation> violations = detector.detect(prompt);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "CONFIDENTIAL_TRADE_SECRET".equals(v.getType())));
    }

    @Test
    @DisplayName("Should detect salary/payroll tables")
    void testDetectSalaryData() {
        String prompt = "Please format this executive compensation data and salary range matrix.";
        List<Violation> violations = detector.detect(prompt);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "CONFIDENTIAL_COMPENSATION_DATA".equals(v.getType())));
    }
}
