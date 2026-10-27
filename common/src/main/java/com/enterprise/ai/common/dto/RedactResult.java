package com.enterprise.ai.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Result of PII and credential prompt redaction")
public class RedactResult {

    @Schema(description = "Prompt text with identified sensitive strings masked", example = "Customer SSN is [REDACTED_SSN]")
    private String redactedPrompt;

    @Schema(description = "Correlation request identifier", example = "req-10293")
    private String requestId;

    @Schema(description = "List of violations detected and masked")
    private List<Violation> violations;

    public RedactResult() {
    }

    public RedactResult(String redactedPrompt, List<Violation> violations) {
        this.redactedPrompt = redactedPrompt;
        this.violations = violations;
    }

    public RedactResult(String requestId, String redactedPrompt, List<Violation> violations) {
        this.requestId = requestId;
        this.redactedPrompt = redactedPrompt;
        this.violations = violations;
    }

    public static RedactResultBuilder builder() {
        return new RedactResultBuilder();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRedactedPrompt() {
        return redactedPrompt;
    }

    public void setRedactedPrompt(String redactedPrompt) {
        this.redactedPrompt = redactedPrompt;
    }

    public List<Violation> getViolations() {
        return violations;
    }

    public void setViolations(List<Violation> violations) {
        this.violations = violations;
    }

    public static class RedactResultBuilder {
        private String requestId;
        private String redactedPrompt;
        private List<Violation> violations;

        public RedactResultBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public RedactResultBuilder redactedPrompt(String redactedPrompt) {
            this.redactedPrompt = redactedPrompt;
            return this;
        }

        public RedactResultBuilder violations(List<Violation> violations) {
            this.violations = violations;
            return this;
        }

        public RedactResult build() {
            RedactResult rr = new RedactResult(redactedPrompt, violations);
            rr.setRequestId(requestId);
            return rr;
        }
    }
}
