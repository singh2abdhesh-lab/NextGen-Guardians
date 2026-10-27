package com.enterprise.ai.quality.service;

import com.enterprise.ai.common.dto.QualityCheckRequest;
import com.enterprise.ai.common.dto.QualityResult;
import com.enterprise.ai.common.dto.QualityResult.Verdict;
import com.enterprise.ai.common.exception.AiGuardianException;
import com.enterprise.ai.quality.checker.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Quality Service Unit Tests")
class QualityServiceTest {

    private QualityService qualityService;

    @BeforeEach
    void setUp() {
        HallucinationDetector hallucinationDetector = new HallucinationDetector();
        SecretEchoDetector secretEchoDetector = new SecretEchoDetector();
        PiiLeakageDetector piiLeakageDetector = new PiiLeakageDetector();
        PolicyChecker policyChecker = new PolicyChecker();
        QualityScoreCalculator qualityScoreCalculator = new QualityScoreCalculator();

        qualityService = new QualityService(
                hallucinationDetector,
                secretEchoDetector,
                piiLeakageDetector,
                policyChecker,
                qualityScoreCalculator
        );
    }

    @Test
    @DisplayName("Clean prompt and response should evaluate to PASS")
    void testCheckClean() {
        QualityCheckRequest request = QualityCheckRequest.builder()
                .requestId("req-clean-1")
                .prompt("Explain how DNS resolution works.")
                .response("DNS maps domain names to IP addresses via root nameservers, TLD servers, and authoritative nameservers.")
                .build();

        QualityResult result = qualityService.check(request);

        assertNotNull(result);
        assertEquals("req-clean-1", result.getRequestId());
        assertEquals(Verdict.PASS, result.getVerdict());
        assertEquals(0.0, result.getHallucinationScore());
        assertFalse(result.isPolicyViolation());
        assertFalse(result.isApprovalRequired());
    }

    @Test
    @DisplayName("Prompt secret echoed in response should evaluate to FAIL")
    void testCheckSecretEcho() {
        QualityCheckRequest request = QualityCheckRequest.builder()
                .requestId("req-echo-1")
                .prompt("Using key sk-1234567890abcdef12345678 to query DB")
                .response("Error: Token sk-1234567890abcdef12345678 lacks access.")
                .build();

        QualityResult result = qualityService.check(request);

        assertEquals(Verdict.FAIL, result.getVerdict());
        assertTrue(result.isPolicyViolation());
        assertTrue(result.isApprovalRequired());
        assertFalse(result.getPolicyViolations().isEmpty());
    }

    @Test
    @DisplayName("Response with fabricated URL and overconfidence should elevate hallucination score")
    void testCheckHallucination() {
        QualityCheckRequest request = QualityCheckRequest.builder()
                .requestId("req-halluc-1")
                .prompt("Where can I find the theorem?")
                .response("The proof is 100% accurate and available at https://fake-academic-portal.edu/papers/complete_proof_download_archive_2024_verifiable")
                .build();

        QualityResult result = qualityService.check(request);

        assertTrue(result.getHallucinationScore() >= 0.60);
        assertTrue(result.getVerdict() == Verdict.REVIEW || result.getVerdict() == Verdict.FAIL);
    }

    @Test
    @DisplayName("Null request should throw AiGuardianException")
    void testNullRequestThrows() {
        assertThrows(AiGuardianException.class, () -> qualityService.check(null));
    }
}
