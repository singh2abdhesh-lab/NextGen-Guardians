package com.enterprise.ai.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Verification results rendered by the Response Quality Checker")
public class QualityResult {

    @Schema(description = "Correlation tracking request ID", example = "req-10293")
    private String requestId;

    @Schema(description = "Quality verdict rendered on model response")
    private Verdict verdict;

    @Schema(description = "Computed probability of hallucination (0.0 to 1.0)", example = "0.04")
    private double hallucinationScore;

    @Schema(description = "Indicates whether model response breached enterprise policy", example = "false")
    private boolean policyViolation;

    @Schema(description = "List of specific policy violations detected in model completion")
    private List<String> policyViolations;

    @Schema(description = "Indicates whether human review is mandated before returning completion", example = "false")
    private boolean approvalRequired;

    public QualityResult() {
    }

    public QualityResult(String requestId, Verdict verdict, double hallucinationScore,
                         boolean policyViolation, List<String> policyViolations, boolean approvalRequired) {
        this.requestId = requestId;
        this.verdict = verdict;
        this.hallucinationScore = hallucinationScore;
        this.policyViolation = policyViolation;
        this.policyViolations = policyViolations;
        this.approvalRequired = approvalRequired;
    }

    public static QualityResultBuilder builder() {
        return new QualityResultBuilder();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Verdict getVerdict() {
        return verdict;
    }

    public void setVerdict(Verdict verdict) {
        this.verdict = verdict;
    }

    public double getHallucinationScore() {
        return hallucinationScore;
    }

    public void setHallucinationScore(double hallucinationScore) {
        this.hallucinationScore = hallucinationScore;
    }

    public boolean isPolicyViolation() {
        return policyViolation;
    }

    public void setPolicyViolation(boolean policyViolation) {
        this.policyViolation = policyViolation;
    }

    public List<String> getPolicyViolations() {
        return policyViolations;
    }

    public void setPolicyViolations(List<String> policyViolations) {
        this.policyViolations = policyViolations;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public void setApprovalRequired(boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public enum Verdict {
        PASS, REVIEW, FAIL, WARN
    }

    private double toxicityScore;
    private List<?> violations;

    public double getToxicityScore() { return toxicityScore; }
    public void setToxicityScore(double toxicityScore) { this.toxicityScore = toxicityScore; }
    public List<?> getViolations() { return violations; }
    public void setViolations(List<?> violations) { this.violations = violations; }

    public static class QualityResultBuilder {
        private String requestId;
        private Verdict verdict;
        private double hallucinationScore;
        private boolean policyViolation;
        private List<String> policyViolations;
        private boolean approvalRequired;
        private double toxicityScore;
        private List<?> violations;

        public QualityResultBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public QualityResultBuilder verdict(Verdict verdict) {
            this.verdict = verdict;
            return this;
        }

        public QualityResultBuilder verdict(com.enterprise.ai.common.enums.QualityVerdict qualityVerdict) {
            if (qualityVerdict != null) {
                try {
                    this.verdict = Verdict.valueOf(qualityVerdict.name());
                } catch (IllegalArgumentException e) {
                    this.verdict = Verdict.PASS;
                }
            }
            return this;
        }

        public QualityResultBuilder hallucinationScore(double hallucinationScore) {
            this.hallucinationScore = hallucinationScore;
            return this;
        }

        public QualityResultBuilder toxicityScore(double toxicityScore) {
            this.toxicityScore = toxicityScore;
            return this;
        }

        public QualityResultBuilder policyViolation(boolean policyViolation) {
            this.policyViolation = policyViolation;
            return this;
        }

        public QualityResultBuilder policyViolations(List<String> policyViolations) {
            this.policyViolations = policyViolations;
            return this;
        }

        public QualityResultBuilder violations(List<?> violations) {
            this.violations = violations;
            return this;
        }

        public QualityResultBuilder approvalRequired(boolean approvalRequired) {
            this.approvalRequired = approvalRequired;
            return this;
        }

        public QualityResult build() {
            QualityResult qr = new QualityResult(requestId, verdict, hallucinationScore, policyViolation, policyViolations, approvalRequired);
            qr.setToxicityScore(toxicityScore);
            qr.setViolations(violations);
            return qr;
        }
    }
}
