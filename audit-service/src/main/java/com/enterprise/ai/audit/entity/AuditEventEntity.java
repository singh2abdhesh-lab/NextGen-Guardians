package com.enterprise.ai.audit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Primary JPA entity representing an immutable forensic audit event.
 * Maps one-to-many relationship with ViolationEntity and one-to-one with ApprovalRecordEntity.
 */
@Entity
@Table(name = "audit_events", indexes = {
        @Index(name = "idx_audit_user", columnList = "user_id"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_risk", columnList = "risk_level"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_approval", columnList = "approval_status"),
        @Index(name = "idx_audit_request", columnList = "request_id")
})
public class AuditEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private String requestId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_role", nullable = false)
    private String userRole;

    private String model;

    @Column(name = "prompt_category")
    private String promptCategory;

    @Column(nullable = false)
    private String action;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "tokens_in")
    private Integer tokensIn;

    @Column(name = "tokens_out")
    private Integer tokensOut;

    @Column(name = "cost_usd")
    private Double costUsd;

    @Column(name = "quality_verdict")
    private String qualityVerdict;

    @Column(name = "hallucination_score")
    private Double hallucinationScore;

    @Column(name = "approval_required")
    private Boolean approvalRequired;

    @Column(name = "violations_summary", columnDefinition = "TEXT")
    private String violationsSummary;

    @Column(name = "block_reason", columnDefinition = "TEXT")
    private String blockReason;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    private LocalDateTime timestamp;

    @Column(name = "approval_status")
    private String approvalStatus;

    private String approver;

    @Column(name = "approval_timestamp")
    private LocalDateTime approvalTimestamp;

    @Column(name = "approval_reason", columnDefinition = "TEXT")
    private String approvalReason;

    // --- Relational Mappings ---

    @JsonIgnore
    @OneToMany(mappedBy = "auditEvent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ViolationEntity> violations = new ArrayList<>();

    @JsonIgnore
    @OneToOne(mappedBy = "auditEvent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private ApprovalRecordEntity approvalRecord;

    public AuditEventEntity() {}

    public AuditEventEntity(Long id, String requestId, String userId, String userRole, String model,
                            String promptCategory, String action, Integer riskScore, String riskLevel,
                            Integer tokensIn, Integer tokensOut, Double costUsd, String qualityVerdict,
                            Double hallucinationScore, Boolean approvalRequired, String violationsSummary,
                            String blockReason, String prompt, LocalDateTime timestamp, String approvalStatus,
                            String approver, LocalDateTime approvalTimestamp, String approvalReason,
                            List<ViolationEntity> violations, ApprovalRecordEntity approvalRecord) {
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
        this.prompt = prompt;
        this.timestamp = timestamp;
        this.approvalStatus = approvalStatus;
        this.approver = approver;
        this.approvalTimestamp = approvalTimestamp;
        this.approvalReason = approvalReason;
        this.violations = violations != null ? violations : new ArrayList<>();
        this.approvalRecord = approvalRecord;
    }

    public void addViolation(ViolationEntity violation) {
        if (violations == null) {
            violations = new ArrayList<>();
        }
        violations.add(violation);
        violation.setAuditEvent(this);
    }

    public void removeViolation(ViolationEntity violation) {
        if (violations != null) {
            violations.remove(violation);
            violation.setAuditEvent(null);
        }
    }

    public void setApprovalRecord(ApprovalRecordEntity record) {
        this.approvalRecord = record;
        if (record != null) {
            record.setAuditEvent(this);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getPromptCategory() { return promptCategory; }
    public void setPromptCategory(String promptCategory) { this.promptCategory = promptCategory; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Integer getTokensIn() { return tokensIn; }
    public void setTokensIn(Integer tokensIn) { this.tokensIn = tokensIn; }
    public Integer getTokensOut() { return tokensOut; }
    public void setTokensOut(Integer tokensOut) { this.tokensOut = tokensOut; }
    public Double getCostUsd() { return costUsd; }
    public void setCostUsd(Double costUsd) { this.costUsd = costUsd; }
    public String getQualityVerdict() { return qualityVerdict; }
    public void setQualityVerdict(String qualityVerdict) { this.qualityVerdict = qualityVerdict; }
    public Double getHallucinationScore() { return hallucinationScore; }
    public void setHallucinationScore(Double hallucinationScore) { this.hallucinationScore = hallucinationScore; }
    public Boolean getApprovalRequired() { return approvalRequired; }
    public void setApprovalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; }
    public String getViolationsSummary() { return violationsSummary; }
    public void setViolationsSummary(String violationsSummary) { this.violationsSummary = violationsSummary; }
    public String getBlockReason() { return blockReason; }
    public void setBlockReason(String blockReason) { this.blockReason = blockReason; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public String getApprover() { return approver; }
    public void setApprover(String approver) { this.approver = approver; }
    public LocalDateTime getApprovalTimestamp() { return approvalTimestamp; }
    public void setApprovalTimestamp(LocalDateTime approvalTimestamp) { this.approvalTimestamp = approvalTimestamp; }
    public String getApprovalReason() { return approvalReason; }
    public void setApprovalReason(String approvalReason) { this.approvalReason = approvalReason; }
    public List<ViolationEntity> getViolations() { return violations; }
    public void setViolations(List<ViolationEntity> violations) { this.violations = violations; }
    public ApprovalRecordEntity getApprovalRecord() { return approvalRecord; }

    public static AuditEventEntityBuilder builder() {
        return new AuditEventEntityBuilder();
    }

    public static class AuditEventEntityBuilder {
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
        private String prompt;
        private LocalDateTime timestamp;
        private String approvalStatus;
        private String approver;
        private LocalDateTime approvalTimestamp;
        private String approvalReason;
        private List<ViolationEntity> violations = new ArrayList<>();
        private ApprovalRecordEntity approvalRecord;

        public AuditEventEntityBuilder id(Long id) { this.id = id; return this; }
        public AuditEventEntityBuilder requestId(String requestId) { this.requestId = requestId; return this; }
        public AuditEventEntityBuilder userId(String userId) { this.userId = userId; return this; }
        public AuditEventEntityBuilder userRole(String userRole) { this.userRole = userRole; return this; }
        public AuditEventEntityBuilder model(String model) { this.model = model; return this; }
        public AuditEventEntityBuilder promptCategory(String promptCategory) { this.promptCategory = promptCategory; return this; }
        public AuditEventEntityBuilder action(String action) { this.action = action; return this; }
        public AuditEventEntityBuilder riskScore(Integer riskScore) { this.riskScore = riskScore; return this; }
        public AuditEventEntityBuilder riskLevel(String riskLevel) { this.riskLevel = riskLevel; return this; }
        public AuditEventEntityBuilder tokensIn(Integer tokensIn) { this.tokensIn = tokensIn; return this; }
        public AuditEventEntityBuilder tokensOut(Integer tokensOut) { this.tokensOut = tokensOut; return this; }
        public AuditEventEntityBuilder costUsd(Double costUsd) { this.costUsd = costUsd; return this; }
        public AuditEventEntityBuilder qualityVerdict(String qualityVerdict) { this.qualityVerdict = qualityVerdict; return this; }
        public AuditEventEntityBuilder hallucinationScore(Double hallucinationScore) { this.hallucinationScore = hallucinationScore; return this; }
        public AuditEventEntityBuilder approvalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; return this; }
        public AuditEventEntityBuilder violationsSummary(String violationsSummary) { this.violationsSummary = violationsSummary; return this; }
        public AuditEventEntityBuilder blockReason(String blockReason) { this.blockReason = blockReason; return this; }
        public AuditEventEntityBuilder prompt(String prompt) { this.prompt = prompt; return this; }
        public AuditEventEntityBuilder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public AuditEventEntityBuilder approvalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; return this; }
        public AuditEventEntityBuilder approver(String approver) { this.approver = approver; return this; }
        public AuditEventEntityBuilder approvalTimestamp(LocalDateTime approvalTimestamp) { this.approvalTimestamp = approvalTimestamp; return this; }
        public AuditEventEntityBuilder approvalReason(String approvalReason) { this.approvalReason = approvalReason; return this; }
        public AuditEventEntityBuilder violations(List<ViolationEntity> violations) { this.violations = violations; return this; }
        public AuditEventEntityBuilder approvalRecord(ApprovalRecordEntity approvalRecord) { this.approvalRecord = approvalRecord; return this; }

        public AuditEventEntity build() {
            return new AuditEventEntity(id, requestId, userId, userRole, model, promptCategory, action,
                    riskScore, riskLevel, tokensIn, tokensOut, costUsd, qualityVerdict, hallucinationScore,
                    approvalRequired, violationsSummary, blockReason, prompt, timestamp, approvalStatus,
                    approver, approvalTimestamp, approvalReason, violations, approvalRecord);
        }
    }
}
