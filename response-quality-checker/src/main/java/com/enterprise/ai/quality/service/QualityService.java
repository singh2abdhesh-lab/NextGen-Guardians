package com.enterprise.ai.quality.service;

import com.enterprise.ai.common.dto.QualityCheckRequest;
import com.enterprise.ai.common.dto.QualityResult;
import com.enterprise.ai.common.exception.AiGuardianException;
import com.enterprise.ai.quality.checker.HallucinationDetector;
import com.enterprise.ai.quality.checker.PiiLeakageDetector;
import com.enterprise.ai.quality.checker.PolicyChecker;
import com.enterprise.ai.quality.checker.QualityScoreCalculator;
import com.enterprise.ai.quality.checker.SecretEchoDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Core domain service orchestrating response quality checks, hallucination scoring,
 * echo detection, PII leakage detection, and enterprise policy compliance.
 */
@Service
public class QualityService {

    private static final Logger log = LoggerFactory.getLogger(QualityService.class);

    private final HallucinationDetector hallucinationDetector;
    private final SecretEchoDetector secretEchoDetector;
    private final PiiLeakageDetector piiLeakageDetector;
    private final PolicyChecker policyChecker;
    private final QualityScoreCalculator qualityScoreCalculator;

    public QualityService(HallucinationDetector hallucinationDetector,
                          SecretEchoDetector secretEchoDetector,
                          PiiLeakageDetector piiLeakageDetector,
                          PolicyChecker policyChecker,
                          QualityScoreCalculator qualityScoreCalculator) {
        this.hallucinationDetector = hallucinationDetector;
        this.secretEchoDetector = secretEchoDetector;
        this.piiLeakageDetector = piiLeakageDetector;
        this.policyChecker = policyChecker;
        this.qualityScoreCalculator = qualityScoreCalculator;
    }

    /**
     * Executes end-to-end quality and compliance verification against prompt and model response.
     *
     * @param request the quality check request payload
     * @return evaluated QualityResult
     */
    public QualityResult check(QualityCheckRequest request) {
        if (request == null) {
            throw new AiGuardianException("Quality check request cannot be null");
        }

        String prompt = request.getPrompt() != null ? request.getPrompt() : "";
        String response = request.getResponse() != null ? request.getResponse() : "";
        String requestId = request.getRequestId();

        log.debug("Initiating response quality assessment for requestId={}", requestId);

        // 1. Hallucination analysis
        double hallucinationScore = hallucinationDetector.calculateHallucinationScore(response);

        // 2. Secret echo analysis
        List<String> secretViolations = secretEchoDetector.detectSecretEchoes(prompt, response);

        // 3. PII leakage analysis
        List<String> piiViolations = piiLeakageDetector.detectPiiLeakage(prompt, response);

        // 4. Policy compliance analysis
        List<String> policyViolations = policyChecker.checkPolicy(response);

        // 5. Synthesis & verdict generation
        return qualityScoreCalculator.calculate(
                requestId,
                hallucinationScore,
                secretViolations,
                piiViolations,
                policyViolations
        );
    }
}
