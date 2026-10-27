package com.enterprise.ai.quality.checker;

import com.enterprise.ai.common.dto.QualityResult;
import com.enterprise.ai.common.dto.QualityResult.Verdict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Synthesizes hallucination scores, policy violations, secret echoes,
 * and PII leaks into a unified quality score and final enforcement verdict.
 */
@Component
public class QualityScoreCalculator {

    private static final Logger log = LoggerFactory.getLogger(QualityScoreCalculator.class);

    /**
     * Aggregates verification vectors and produces a final QualityResult.
     *
     * @param requestId          unique correlation identifier
     * @param hallucinationScore computed score between 0.0 and 1.0
     * @param secretViolations   violations from SecretEchoDetector
     * @param piiViolations      violations from PiiLeakageDetector
     * @param policyViolations   violations from PolicyChecker
     * @return populated QualityResult entity
     */
    public QualityResult calculate(String requestId,
                                   double hallucinationScore,
                                   List<String> secretViolations,
                                   List<String> piiViolations,
                                   List<String> policyViolations) {

        List<String> allViolations = new ArrayList<>();
        if (secretViolations != null) {
            allViolations.addAll(secretViolations);
        }
        if (piiViolations != null) {
            allViolations.addAll(piiViolations);
        }
        if (policyViolations != null) {
            allViolations.addAll(policyViolations);
        }

        boolean hasDirectBreaches = !allViolations.isEmpty();
        boolean policyViolationFlag = hasDirectBreaches;

        Verdict verdict;
        if (hasDirectBreaches || hallucinationScore >= 0.70) {
            verdict = Verdict.FAIL;
        } else if (hallucinationScore >= 0.30) {
            verdict = Verdict.REVIEW;
        } else {
            verdict = Verdict.PASS;
        }

        boolean approvalRequired = (verdict == Verdict.REVIEW || verdict == Verdict.FAIL);

        log.info("Quality assessment concluded: requestId={}, verdict={}, hallucinationScore={}, violationsCount={}, approvalRequired={}",
                requestId, verdict, hallucinationScore, allViolations.size(), approvalRequired);

        return QualityResult.builder()
                .requestId(requestId)
                .verdict(verdict)
                .hallucinationScore(Math.round(hallucinationScore * 100.0) / 100.0)
                .policyViolation(policyViolationFlag)
                .policyViolations(Collections.unmodifiableList(allViolations))
                .approvalRequired(approvalRequired)
                .build();
    }
}
