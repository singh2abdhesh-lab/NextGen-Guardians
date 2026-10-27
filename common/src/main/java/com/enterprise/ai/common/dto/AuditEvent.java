package com.enterprise.ai.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Immutable forensic audit event capturing input, decisions, outputs, and financial spend")
public class AuditEvent {

    @Schema(description = "Internal unique event database ID", example = "1094")
    private Long id;

    @Schema(description = "Unique correlation request ID", example = "req-8894102-a")
    private String requestId;

    @Schema(description = "User identifier or email", example = "intern.alex@enterprise.ai")
    private String userId;

    @Schema(description = "Caller role", example = "INTERN")
    private String userRole;

    @Schema(description = "Target LLM model", example = "gpt-4o")
    private String model;

    @Schema(description = "Domain category of input prompt", example = "CODE")
    private String promptCategory;

    @Schema(description = "Enforced action: ALLOW, BLOCK, REVIEW", example = "BLOCK")
    private String action;

    @Schema(description = "Calculated risk score (0-100)", example = "88")
    private Integer riskScore;

    @Schema(description = "Risk level tier (LOW, MEDIUM, HIGH, CRITICAL)", example = "CRITICAL")
    private String riskLevel;

    @Schema(description = "Input prompt token count", example = "412")
    private Integer tokensIn;

    @Schema(description = "Model completion token count", example = "0")
    private Integer tokensOut;

    @Schema(description = "Financial cost in USD", example = "0.00103")
    private Double costUsd;

    @Schema(description = "Quality verdict rendered on model response", example = "FAIL")
    private String qualityVerdict;

    @Schema(description = "Hallucination probability score (0.0 to 1.0)", example = "0.85")
    private Double hallucinationScore;

    @Schema(description = "Whether human-in-the-loop review was mandated", example = "false")
    private Boolean approvalRequired;

    @Schema(description = "Summary of detected violations", example = "PROMPT_INJECTION (CRITICAL), SECRET_LEAK (CRITICAL)")
    private String violationsSummary;

    @Schema(description = "Justification reason if request was blocked", example = "Jailbreak instruction and AWS Access Key detected")
    private String blockReason;

    @Schema(description = "Timestamp when event occurred")
    private LocalDateTime timestamp;

    @Schema(description = "Approval lifecycle status: PENDING, APPROVED, REJECTED, NONE", example = "NONE")
    private String approvalStatus;

    @Schema(description = "Email of authorizing security auditor (if reviewed)", example = "secops.lead@enterprise.ai")
    private String approver;

    @Schema(description = "Timestamp when review decision was recorded")
    private LocalDateTime approvalTimestamp;

    @Schema(description = "Recorded prompt text")
    private String prompt;

    public AuditEvent() {
    }

    public AuditEvent(Long id, String requestId, String userId, String userRole, String model,
                      String promptCategory, String action, Integer riskScore, String riskLevel,
                      Integer tokensIn, Integer tokensOut, Double costUsd, String qualityVerdict,
                      Double hallucinationScore, Boolean approvalRequired, String violationsSummary,
                      String blockReason, LocalDateTime timestamp, String approvalStatus,
                      String approver, LocalDateTime approvalTimestamp, String prompt) {
        this.id = id;
        this.requestId = requestId;
        this.userId = userId;
        this.userRole = userRole;
        this.model = model;
        this.promptCategory = promptCategory;
        this.action = action;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.tokensIn = tokensIn;
        this.tokensOut = tokensOut;
        this.costUsd = costUsd;
        this.qualityVerdict = qualityVerdict;
        this.hallucinationScore = hallucinationScore;
        this.approvalRequired = approvalRequired;
        this.violationsSummary = violationsSummary;
        this.blockReason = blockReason;
        this.timestamp = timestamp;
        this.approvalStatus = approvalStatus;
        this.approver = approver;
        this.approvalTimestamp = approvalTimestamp;
        this.prompt = prompt;
    }

    public static AuditEventBuilder builder() {
        return new AuditEventBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPromptCategory() {
        return promptCategory;
    }

    public void setPromptCategory(String promptCategory) {
        this.promptCategory = promptCategory;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Integer getTokensIn() {
        return tokensIn;
    }

    public void setTokensIn(Integer tokensIn) {
        this.tokensIn = tokensIn;
    }

    public Integer getTokensOut() {
        return tokensOut;
    }

    public void setTokensOut(Integer tokensOut) {
        this.tokensOut = tokensOut;
    }

    public Double getCostUsd() {
        return costUsd;
    }

    public void setCostUsd(Double costUsd) {
        this.costUsd = costUsd;
    }

    public String getQualityVerdict() {
        return qualityVerdict;
    }

    public void setQualityVerdict(String qualityVerdict) {
        this.qualityVerdict = qualityVerdict;
    }

    public Double getHallucinationScore() {
        return hallucinationScore;
    }

    public void setHallucinationScore(Double hallucinationScore) {
        this.hallucinationScore = hallucinationScore;
    }

    public Boolean getApprovalRequired() {
        return approvalRequired;
    }

    public void setApprovalRequired(Boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public Boolean isApprovalRequired() {
        return approvalRequired != null && approvalRequired;
    }

    public String getViolationsSummary() {
        return violationsSummary;
    }

    public void setViolationsSummary(String violationsSummary) {
        this.violationsSummary = violationsSummary;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public LocalDateTime getApprovalTimestamp() {
        return approvalTimestamp;
    }

    public void setApprovalTimestamp(LocalDateTime approvalTimestamp) {
        this.approvalTimestamp = approvalTimestamp;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public static class AuditEventBuilder {
        private Long id;
        private String requestId;
        private String userId;
        private String userRole;
        private String model;
        private String promptCategory;
        private String action;
        private Integer riskScore;
        private String riskLevel;
        private Integer tokensIn;
        private Integer tokensOut;
        private Double costUsd;
        private String qualityVerdict;
        private Double hallucinationScore;
        private Boolean approvalRequired;
        private String violationsSummary;
        private String blockReason;
        private LocalDateTime timestamp;
        private String approvalStatus;
        private String approver;
        private LocalDateTime approvalTimestamp;
        private String prompt;

        public AuditEventBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AuditEventBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public AuditEventBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public AuditEventBuilder userRole(String userRole) {
            this.userRole = userRole;
            return this;
        }

        public AuditEventBuilder model(String model) {
            this.model = model;
            return this;
        }

        public AuditEventBuilder promptCategory(String promptCategory) {
            this.promptCategory = promptCategory;
            return this;
        }

        public AuditEventBuilder action(String action) {
            this.action = action;
            return this;
        }

        public AuditEventBuilder riskScore(Integer riskScore) {
            this.riskScore = riskScore;
            return this;
        }

        public AuditEventBuilder riskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }

        public AuditEventBuilder tokensIn(Integer tokensIn) {
            this.tokensIn = tokensIn;
            return this;
        }

        public AuditEventBuilder tokensOut(Integer tokensOut) {
            this.tokensOut = tokensOut;
            return this;
        }

        public AuditEventBuilder costUsd(Double costUsd) {
            this.costUsd = costUsd;
            return this;
        }

        public AuditEventBuilder qualityVerdict(String qualityVerdict) {
            this.qualityVerdict = qualityVerdict;
            return this;
        }

        public AuditEventBuilder hallucinationScore(Double hallucinationScore) {
            this.hallucinationScore = hallucinationScore;
            return this;
        }

        public AuditEventBuilder approvalRequired(Boolean approvalRequired) {
            this.approvalRequired = approvalRequired;
            return this;
        }

        public AuditEventBuilder violationsSummary(String violationsSummary) {
            this.violationsSummary = violationsSummary;
            return this;
        }

        public AuditEventBuilder blockReason(String blockReason) {
            this.blockReason = blockReason;
            return this;
        }

        public AuditEventBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AuditEventBuilder approvalStatus(String approvalStatus) {
            this.approvalStatus = approvalStatus;
            return this;
        }

        public AuditEventBuilder approver(String approver) {
            this.approver = approver;
            return this;
        }

        public AuditEventBuilder approvalTimestamp(LocalDateTime approvalTimestamp) {
            this.approvalTimestamp = approvalTimestamp;
            return this;
        }

        public AuditEventBuilder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public AuditEvent build() {
            return new AuditEvent(id, requestId, userId, userRole, model, promptCategory, action,
                    riskScore, riskLevel, tokensIn, tokensOut, costUsd, qualityVerdict,
                    hallucinationScore, approvalRequired, violationsSummary, blockReason,
                    timestamp, approvalStatus, approver, approvalTimestamp, prompt);
        }
    }
}
