package com.enterprise.ai.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Security scan results detailing identified threats and data sensitivity")
public class ScanResult {

    @Schema(description = "Correlation tracking request ID", example = "req-10293")
    private String requestId;

    @Schema(description = "Indicates whether the prompt was blocked by scanner guardrails", example = "false")
    private boolean blocked;

    @Schema(description = "Aggregated data sensitivity score (0-100)", example = "35")
    private int sensitivityScore;

    @Schema(description = "List of threat categories triggered")
    private List<String> detectedCategories;

    @Schema(description = "Detailed list of detected security violations")
    private List<Violation> violations;

    public ScanResult() {
    }

    public ScanResult(String requestId, boolean blocked, int sensitivityScore,
                      List<String> detectedCategories, List<Violation> violations) {
        this.requestId = requestId;
        this.blocked = blocked;
        this.sensitivityScore = sensitivityScore;
        this.detectedCategories = detectedCategories;
        this.violations = violations;
    }

    public static ScanResultBuilder builder() {
        return new ScanResultBuilder();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public int getSensitivityScore() {
        return sensitivityScore;
    }

    public void setSensitivityScore(int sensitivityScore) {
        this.sensitivityScore = sensitivityScore;
    }

    public List<String> getDetectedCategories() {
        return detectedCategories;
    }

    public void setDetectedCategories(List<String> detectedCategories) {
        this.detectedCategories = detectedCategories;
    }

    public List<Violation> getViolations() {
        return violations;
    }

    public void setViolations(List<Violation> violations) {
        this.violations = violations;
    }

    public static class ScanResultBuilder {
        private String requestId;
        private boolean blocked;
        private int sensitivityScore;
        private List<String> detectedCategories;
        private List<Violation> violations;

        public ScanResultBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public ScanResultBuilder blocked(boolean blocked) {
            this.blocked = blocked;
            return this;
        }

        public ScanResultBuilder sensitivityScore(int sensitivityScore) {
            this.sensitivityScore = sensitivityScore;
            return this;
        }

        public ScanResultBuilder detectedCategories(List<String> detectedCategories) {
            this.detectedCategories = detectedCategories;
            return this;
        }

        public ScanResultBuilder violations(List<Violation> violations) {
            this.violations = violations;
            return this;
        }

        public ScanResult build() {
            return new ScanResult(requestId, blocked, sensitivityScore, detectedCategories, violations);
        }
    }
}
