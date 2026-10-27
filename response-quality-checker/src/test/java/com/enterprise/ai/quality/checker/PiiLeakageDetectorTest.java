package com.enterprise.ai.quality.checker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PII Leakage Detector Unit Tests")
class PiiLeakageDetectorTest {

    private PiiLeakageDetector detector;

    @BeforeEach
    void setUp() {
        detector = new PiiLeakageDetector();
    }

    @Test
    @DisplayName("Clean response without PII echo returns no violations")
    void testCleanResponse() {
        String prompt = "What are the compliance requirements for storing patient records?";
        String response = "HIPAA mandates encryption at rest and in transit for Protected Health Information.";

        List<String> violations = detector.detectPiiLeakage(prompt, response);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Response echoing SSN from prompt triggers violation")
    void testSsnEcho() {
        String prompt = "Verify customer with SSN 123-45-6789";
        String response = "Found record matching SSN 123-45-6789 in the system.";

        List<String> violations = detector.detectPiiLeakage(prompt, response);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("Social Security Number (SSN)")));
    }

    @Test
    @DisplayName("Response echoing email address from prompt triggers violation")
    void testEmailEcho() {
        String prompt = "Send an invite to alice.smith@enterprise.corp";
        String response = "Invitation successfully dispatched to alice.smith@enterprise.corp";

        List<String> violations = detector.detectPiiLeakage(prompt, response);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("email address")));
    }

    @Test
    @DisplayName("Unsolicited credit card number in response triggers violation")
    void testUnsolicitedCreditCard() {
        String prompt = "Give me dummy test data for an e-commerce checkout";
        String response = "You can test with Visa card 4111111111111111.";

        List<String> violations = detector.detectPiiLeakage(prompt, response);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("payment card")));
    }
}
