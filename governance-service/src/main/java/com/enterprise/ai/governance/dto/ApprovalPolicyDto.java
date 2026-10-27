package com.enterprise.ai.governance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Schema(description = "Approval Policy Configuration")
public class ApprovalPolicyDto {

    private Long id;

    @NotBlank(message = "Policy key cannot be blank")
    @Schema(description = "Unique approval policy key", example = "APPR-FIN-01")
    private String policyKey;

    @NotBlank(message = "Policy name cannot be blank")
    @Schema(description = "Workflow title", example = "Executive Financial Sign-off")
    private String name;

    @NotBlank(message = "Trigger category cannot be blank")
    @Schema(description = "Trigger category (HIGH_RISK, FINANCE, LEGAL, SOURCE_CODE, CUSTOMER_DATA, CRITICAL_SECRETS)", example = "FINANCE")
    private String triggerCategory;

    @NotBlank(message = "Workflow type cannot be blank")
    @Schema(description = "Workflow mechanics (SINGLE_APPROVAL, MULTI_LEVEL, PARALLEL, ESCALATION, AUTO_EXPIRY, DELEGATE)", example = "MULTI_LEVEL")
    private String workflowType;

    @NotBlank(message = "Primary approver role cannot be blank")
    @Schema(description = "Primary reviewer role", example = "FINANCE_DIRECTOR")
    private String primaryApproverRole;

    @Schema(description = "Escalation reviewer role", example = "CISO")
    private String escalationApproverRole;

    @Schema(description = "Hours before auto-expiration or escalation", example = "24")
    private Integer autoExpiryHours;

    @Schema(description = "Optional delegated user ID", example = "deputy.ciso@enterprise.ai")
    private String delegatedTo;

    private String description;
    private Boolean enabled;
    private LocalDateTime updatedAt;

    public ApprovalPolicyDto() {}

    public ApprovalPolicyDto(Long id, String policyKey, String name, String triggerCategory,
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

    public static ApprovalPolicyDtoBuilder builder() {
        return new ApprovalPolicyDtoBuilder();
    }

    public static class ApprovalPolicyDtoBuilder {
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

        public ApprovalPolicyDtoBuilder id(Long id) { this.id = id; return this; }
        public ApprovalPolicyDtoBuilder policyKey(String policyKey) { this.policyKey = policyKey; return this; }
        public ApprovalPolicyDtoBuilder name(String name) { this.name = name; return this; }
        public ApprovalPolicyDtoBuilder triggerCategory(String triggerCategory) { this.triggerCategory = triggerCategory; return this; }
        public ApprovalPolicyDtoBuilder workflowType(String workflowType) { this.workflowType = workflowType; return this; }
        public ApprovalPolicyDtoBuilder primaryApproverRole(String primaryApproverRole) { this.primaryApproverRole = primaryApproverRole; return this; }
        public ApprovalPolicyDtoBuilder escalationApproverRole(String escalationApproverRole) { this.escalationApproverRole = escalationApproverRole; return this; }
        public ApprovalPolicyDtoBuilder autoExpiryHours(Integer autoExpiryHours) { this.autoExpiryHours = autoExpiryHours; return this; }
        public ApprovalPolicyDtoBuilder delegatedTo(String delegatedTo) { this.delegatedTo = delegatedTo; return this; }
        public ApprovalPolicyDtoBuilder description(String description) { this.description = description; return this; }
        public ApprovalPolicyDtoBuilder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        public ApprovalPolicyDtoBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public ApprovalPolicyDto build() {
            return new ApprovalPolicyDto(id, policyKey, name, triggerCategory, workflowType, primaryApproverRole,
                    escalationApproverRole, autoExpiryHours, delegatedTo, description, enabled, updatedAt);
        }
    }
}
