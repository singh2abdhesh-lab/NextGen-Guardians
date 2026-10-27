package com.enterprise.ai.scanner.detector;

import com.enterprise.ai.common.dto.Violation;
import com.enterprise.ai.common.dto.Violation.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Secret Detector Unit Tests")
class SecretDetectorTest {

    private SecretDetector detector;

    @BeforeEach
    void setUp() {
        detector = new SecretDetector();
    }

    @Test
    @DisplayName("Should detect OpenAI API keys as CRITICAL")
    void testDetectOpenAiKey() {
        String prompt = "Use key sk-abcdef1234567890abcdef1234567890 to call GPT-4.";
        List<Violation> violations = detector.detect(prompt);

        assertFalse(violations.isEmpty());
        Violation v = violations.get(0);
        assertEquals("SECRET_OPENAI_KEY", v.getType());
        assertEquals(Severity.CRITICAL, v.getSeverity());
    }

    @Test
    @DisplayName("Should detect AWS Access Key ID")
    void testDetectAwsKey() {
        String prompt = "Deploy to S3 bucket using AKIAIOSFODNN7EXAMPLE credentials.";
        List<Violation> violations = detector.detect(prompt);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "SECRET_AWS_ACCESS_KEY".equals(v.getType())));
    }

    @Test
    @DisplayName("Should detect Private Key headers")
    void testDetectPrivateKey() {
        String prompt = "-----BEGIN RSA PRIVATE KEY-----\nMIIEowIBAAKCAQEA0...\n-----END RSA PRIVATE KEY-----";
        List<Violation> violations = detector.detect(prompt);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "SECRET_PRIVATE_KEY".equals(v.getType())));
    }
}
