package com.enterprise.ai.governance.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gov_approval_policies", indexes = {
        @Index(name = "idx_appr_key", columnList = "policy_key", unique = true),
        @Index(name = "idx_appr_trigger", columnList = "trigger_category")
})
public class ApprovalPolicyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_key", nullable = false, length = 64)
    private String policyKey;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "trigger_category", nullable = false, length = 50)
    private String triggerCategory;

    @Column(name = "workflow_type", nullable = false, length = 50)
    private String workflowType;

    @Column(name = "primary_approver_role", nullable = false, length = 60)
    private String primaryApproverRole;

    @Column(name = "escalation_approver_role", length = 60)
    private String escalationApproverRole;

    @Column(name = "auto_expiry_hours")
    private Integer autoExpiryHours;

    @Column(name = "delegated_to", length = 100)
    private String delegatedTo;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ApprovalPolicyEntity() {}

    public ApprovalPolicyEntity(Long id, String policyKey, String name, String triggerCategory,
                                String workflowType, String primaryApproverRole, String escalationApproverRole,
                                Integer autoExpiryHours, String delegatedTo, String description,
                                Boolean enabled, LocalDateTime updatedAt) {
        this.id = id;
        this.policyKey = policyKey;
        this.name = name;
        this.triggerCategory = triggerCategory;
        this.workflowType = workflowType;
        this.primaryApproverRole = primaryApproverRole;
        this.escalationApproverRole = escalationApproverRole;
        this.autoExpiryHours = autoExpiryHours;
        this.delegatedTo = delegatedTo;
        this.description = description;
        this.enabled = enabled;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void onPrePersist() {
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (enabled == null) enabled = true;
        if (autoExpiryHours == null) autoExpiryHours = 24;
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
    public String getTriggerCategory() { return triggerCategory; }
    public void setTriggerCategory(String triggerCategory) { this.triggerCategory = triggerCategory; }
    public String getWorkflowType() { return workflowType; }
    public void setWorkflowType(String workflowType) { this.workflowType = workflowType; }
    public String getPrimaryApproverRole() { return primaryApproverRole; }
    public void setPrimaryApproverRole(String primaryApproverRole) { this.primaryApproverRole = primaryApproverRole; }
    public String getEscalationApproverRole() { return escalationApproverRole; }
    public void setEscalationApproverRole(String escalationApproverRole) { this.escalationApproverRole = escalationApproverRole; }
    public Integer getAutoExpiryHours() { return autoExpiryHours; }
    public void setAutoExpiryHours(Integer autoExpiryHours) { this.autoExpiryHours = autoExpiryHours; }
    public String getDelegatedTo() { return delegatedTo; }
    public void setDelegatedTo(String delegatedTo) { this.delegatedTo = delegatedTo; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static ApprovalPolicyEntityBuilder builder() {
        return new ApprovalPolicyEntityBuilder();
    }

    public static class ApprovalPolicyEntityBuilder {
        private Long id;
        private String policyKey;
        private String name;
        private String triggerCategory;
        private String workflowType;
        private String primaryApproverRole;
        private String escalationApproverRole;
        private Integer autoExpiryHours;
        private String delegatedTo;
        private String description;
        private Boolean enabled;
        private LocalDateTime updatedAt;

        public ApprovalPolicyEntityBuilder id(Long id) { this.id = id; return this; }
        public ApprovalPolicyEntityBuilder policyKey(String policyKey) { this.policyKey = policyKey; return this; }
        public ApprovalPolicyEntityBuilder name(String name) { this.name = name; return this; }
        public ApprovalPolicyEntityBuilder triggerCategory(String triggerCategory) { this.triggerCategory = triggerCategory; return this; }
        public ApprovalPolicyEntityBuilder workflowType(String workflowType) { this.workflowType = workflowType; return this; }
        public ApprovalPolicyEntityBuilder primaryApproverRole(String primaryApproverRole) { this.primaryApproverRole = primaryApproverRole; return this; }
        public ApprovalPolicyEntityBuilder escalationApproverRole(String escalationApproverRole) { this.escalationApproverRole = escalationApproverRole; return this; }
        public ApprovalPolicyEntityBuilder autoExpiryHours(Integer autoExpiryHours) { this.autoExpiryHours = autoExpiryHours; return this; }
        public ApprovalPolicyEntityBuilder delegatedTo(String delegatedTo) { this.delegatedTo = delegatedTo; return this; }
        public ApprovalPolicyEntityBuilder description(String description) { this.description = description; return this; }
        public ApprovalPolicyEntityBuilder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        public ApprovalPolicyEntityBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public ApprovalPolicyEntity build() {
            return new ApprovalPolicyEntity(id, policyKey, name, triggerCategory, workflowType, primaryApproverRole,
                    escalationApproverRole, autoExpiryHours, delegatedTo, description, enabled, updatedAt);
        }
    }
}
