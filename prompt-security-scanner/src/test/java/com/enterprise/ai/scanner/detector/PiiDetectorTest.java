package com.enterprise.ai.scanner.detector;

import com.enterprise.ai.common.dto.Violation;
import com.enterprise.ai.common.dto.Violation.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PII Detector Unit Tests")
class PiiDetectorTest {

    private PiiDetector detector;

    @BeforeEach
    void setUp() {
        detector = new PiiDetector();
    }

    @Test
    @DisplayName("Should detect Social Security Number format")
    void testDetectSsn() {
        String prompt = "Client registration with SSN 123-45-6789 for tax filing.";
        List<Violation> violations = detector.detect(prompt);

        assertFalse(violations.isEmpty(), "Should detect SSN");
        assertTrue(violations.stream().anyMatch(v -> "PII_SSN".equals(v.getType())));
        assertTrue(violations.stream().anyMatch(v -> v.getSeverity() == Severity.HIGH));
    }

    @Test
    @DisplayName("Should detect Credit Card number with CRITICAL severity")
    void testDetectCreditCard() {
        String prompt = "Charge account 4111 2222 3333 4444 for annual subscription.";
        List<Violation> violations = detector.detect(prompt);

        assertFalse(violations.isEmpty(), "Should detect payment card");
        Violation ccViolation = violations.stream()
                .filter(v -> "PII_CREDIT_CARD".equals(v.getType()))
                .findFirst()
                .orElse(null);

        assertNotNull(ccViolation);
        assertEquals(Severity.CRITICAL, ccViolation.getSeverity());
    }

    @Test
    @DisplayName("Should detect email addresses with LOW severity")
    void testDetectEmail() {
        String prompt = "Send financial statement to CFO john.doe@enterprise.com immediately.";
        List<Violation> violations = detector.detect(prompt);

        assertTrue(violations.stream().anyMatch(v -> "PII_EMAIL".equals(v.getType())));
    }

    @Test
    @DisplayName("Should return empty violations for clean text")
    void testCleanPrompt() {
        String prompt = "Explain the difference between reactive programming and traditional MVC.";
        List<Violation> violations = detector.detect(prompt);

        assertTrue(violations.isEmpty(), "Clean text should not trigger PII detector");
    }
}
