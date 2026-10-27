package com.enterprise.ai.quality.checker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Secret Echo Detector Unit Tests")
class SecretEchoDetectorTest {

    private SecretEchoDetector detector;

    @BeforeEach
    void setUp() {
        detector = new SecretEchoDetector();
    }

    @Test
    @DisplayName("Clean response without secret echo returns empty violations")
    void testCleanEcho() {
        String prompt = "How do I securely store secrets in AWS Secrets Manager?";
        String response = "Use AWS Secrets Manager client with IAM roles instead of hardcoding API keys.";

        List<String> violations = detector.detectSecretEchoes(prompt, response);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Response echoing OpenAI API key from prompt should be detected")
    void testOpenAiKeyEcho() {
        String prompt = "Debug this call using key sk-abcdef12345678901234567890";
        String response = "Your key sk-abcdef12345678901234567890 seems to have expired.";

        List<String> violations = detector.detectSecretEchoes(prompt, response);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("OpenAI API key")));
    }

    @Test
    @DisplayName("Response echoing AWS access key from prompt should be detected")
    void testAwsKeyEcho() {
        String prompt = "Connect using AKIAIOSFODNN7EXAMPLE";
        String response = "Received key AKIAIOSFODNN7EXAMPLE. Proceeding with connection.";

        List<String> violations = detector.detectSecretEchoes(prompt, response);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("AWS Access Key ID")));
    }

    @Test
    @DisplayName("Response echoing password assignment from prompt should be detected")
    void testPasswordEcho() {
        String prompt = "My database password = 'SuperSecretDbPassword123' does not work";
        String response = "Checking with SuperSecretDbPassword123 failed.";

        List<String> violations = detector.detectSecretEchoes(prompt, response);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("credential/password")));
    }

    @Test
    @DisplayName("Response emitting raw private key header triggers violation")
    void testRawPrivateKeyEmission() {
        String prompt = "Generate a keypair";
        String response = "-----BEGIN RSA PRIVATE KEY-----\nMIIEowIBAAKCAQEA0...\n-----END RSA PRIVATE KEY-----";

        List<String> violations = detector.detectSecretEchoes(prompt, response);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.contains("private cryptographic key")));
    }
}
