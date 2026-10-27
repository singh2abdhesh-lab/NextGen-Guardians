package com.enterprise.ai.common.exception;

import com.enterprise.ai.common.enums.QualityVerdict;

/**
 * Thrown when model output fails factual consistency or safety quality heuristics.
 */
public class QualityValidationException extends AiGuardianException {

    private final QualityVerdict verdict;
    private final double hallucinationScore;
    private final double toxicityScore;

    public QualityValidationException(String message, QualityVerdict verdict, double hallucinationScore, double toxicityScore) {
        super(message, "QUALITY_VALIDATION_FAILED", 502);
        this.verdict = verdict;
        this.hallucinationScore = hallucinationScore;
        this.toxicityScore = toxicityScore;
    }

    public QualityVerdict getVerdict() {
        return verdict;
    }

    public double getHallucinationScore() {
        return hallucinationScore;
    }

    public double getToxicityScore() {
        return toxicityScore;
    }
}
