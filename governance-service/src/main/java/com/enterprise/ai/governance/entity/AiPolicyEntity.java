package com.enterprise.ai.governance.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gov_ai_policies", indexes = {
        @Index(name = "idx_pol_key", columnList = "policy_key", unique = true),
        @Index(name = "idx_pol_cat", columnList = "category"),
        @Index(name = "idx_pol_status", columnList = "status"),
        @Index(name = "idx_pol_priority", columnList = "priority")
})
public class AiPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_key", nullable = false, length = 64)
    private String policyKey;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Integer priority; // 1 (Highest) to 100

    @Column(nullable = false, length = 30)
    private String status; // DRAFT, ACTIVE, SUSPENDED, DEPRECATED

    @Column(nullable = false)
    private Integer version;

    @Lob
    @Column(name = "rules_json", columnDefinition = "TEXT")
    private String rulesJson;

    @Column(nullable = false, length = 30)
    private String action; // ALLOW, BLOCK, REVIEW, REDACT, NOTIFY

    @Column(name = "approval_required")
    private Boolean approvalRequired;

    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public AiPolicyEntity() {}

    public AiPolicyEntity(Long id, String policyKey, String name, String category, String description,
                          Integer priority, String status, Integer version, String rulesJson, String action,
                          Boolean approvalRequired, LocalDateTime effectiveDate, LocalDateTime expiryDate,
                          Boolean enabled, LocalDateTime createdAt, LocalDateTime updatedAt,
                          String createdBy, String updatedBy) {
        this.id = id;
        this.policyKey = policyKey;
        this.name = name;
        this.category = category;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.version = version;
        this.rulesJson = rulesJson;
        this.action = action;
        this.approvalRequired = approvalRequired;
        this.effectiveDate = effectiveDate;
        this.expiryDate = expiryDate;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (version == null) version = 1;
        if (enabled == null) enabled = true;
        if (status == null) status = "ACTIVE";
        if (approvalRequired == null) approvalRequired = false;
        if (priority == null) priority = 50;
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPolicyKey() { return policyKey; }
    public void setPolicyKey(String policyKey) { this.policyKey = policyKey; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getRulesJson() { return rulesJson; }
    public void setRulesJson(String rulesJson) { this.rulesJson = rulesJson; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Boolean getApprovalRequired() { return approvalRequired; }
    public void setApprovalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; }
    public LocalDateTime getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDateTime effectiveDate) { this.effectiveDate = effectiveDate; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public static AiPolicyEntityBuilder builder() {
        return new AiPolicyEntityBuilder();
    }

    public static class AiPolicyEntityBuilder {
        private Long id;
        private String policyKey;
        private String name;
        private String category;
        private String description;
        private Integer priority;
        private String status;
        private Integer version;
        private String rulesJson;
        private String action;
        private Boolean approvalRequired;
        private LocalDateTime effectiveDate;
        private LocalDateTime expiryDate;
        private Boolean enabled;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;

        public AiPolicyEntityBuilder id(Long id) { this.id = id; return this; }
        public AiPolicyEntityBuilder policyKey(String policyKey) { this.policyKey = policyKey; return this; }
        public AiPolicyEntityBuilder name(String name) { this.name = name; return this; }
        public AiPolicyEntityBuilder category(String category) { this.category = category; return this; }
        public AiPolicyEntityBuilder description(String description) { this.description = description; return this; }
        public AiPolicyEntityBuilder priority(Integer priority) { this.priority = priority; return this; }
        public AiPolicyEntityBuilder status(String status) { this.status = status; return this; }
        public AiPolicyEntityBuilder version(Integer version) { this.version = version; return this; }
        public AiPolicyEntityBuilder rulesJson(String rulesJson) { this.rulesJson = rulesJson; return this; }
        public AiPolicyEntityBuilder action(String action) { this.action = action; return this; }
        public AiPolicyEntityBuilder approvalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; return this; }
        public AiPolicyEntityBuilder effectiveDate(LocalDateTime effectiveDate) { this.effectiveDate = effectiveDate; return this; }
        public AiPolicyEntityBuilder expiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; return this; }
        public AiPolicyEntityBuilder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        public AiPolicyEntityBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AiPolicyEntityBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public AiPolicyEntityBuilder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public AiPolicyEntityBuilder updatedBy(String updatedBy) { this.updatedBy = updatedBy; return this; }

        public AiPolicyEntity build() {
            return new AiPolicyEntity(id, policyKey, name, category, description, priority, status, version, rulesJson, action, approvalRequired, effectiveDate, expiryDate, enabled, createdAt, updatedAt, createdBy, updatedBy);
        }
    }
}
