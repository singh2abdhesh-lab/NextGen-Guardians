package com.enterprise.ai.governance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Schema(description = "AI Policy Definition Payload")
public class AiPolicyDto {

    private Long id;

    @NotBlank(message = "Policy key cannot be blank")
    @Schema(description = "Unique policy identifier", example = "POL-FIN-001")
    private String policyKey;

    @NotBlank(message = "Policy name cannot be blank")
    @Schema(description = "Human-readable policy title", example = "Confidential Financial Data Firewall")
    private String name;

    @NotBlank(message = "Category cannot be blank")
    @Schema(description = "Policy domain category", example = "FINANCIAL")
    private String category;

    @Schema(description = "Policy rationale and context")
    private String description;

    @NotNull
    @Min(1)
    @Max(100)
    @Schema(description = "Execution priority (1-100, 1 being highest)", example = "10")
    private Integer priority;

    @Schema(description = "Policy status: DRAFT, ACTIVE, SUSPENDED, DEPRECATED", example = "ACTIVE")
    private String status;

    private Integer version;

    @Schema(description = "JSON-based rule conditions, regex, or keyword matches", example = "{\"keywords\":[\"earnings\",\"forecast\"],\"action\":\"REVIEW\"}")
    private String rulesJson;

    @NotBlank(message = "Action cannot be blank")
    @Schema(description = "Enforcement action: ALLOW, BLOCK, REVIEW, REDACT, NOTIFY", example = "REVIEW")
    private String action;

    @Schema(description = "Requires human signoff before execution", example = "true")
    private Boolean approvalRequired;

    private LocalDateTime effectiveDate;
    private LocalDateTime expiryDate;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public AiPolicyDto() {}

    public AiPolicyDto(Long id, String policyKey, String name, String category, String description,
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

    public static AiPolicyDtoBuilder builder() {
        return new AiPolicyDtoBuilder();
    }

    public static class AiPolicyDtoBuilder {
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

        public AiPolicyDtoBuilder id(Long id) { this.id = id; return this; }
        public AiPolicyDtoBuilder policyKey(String policyKey) { this.policyKey = policyKey; return this; }
        public AiPolicyDtoBuilder name(String name) { this.name = name; return this; }
        public AiPolicyDtoBuilder category(String category) { this.category = category; return this; }
        public AiPolicyDtoBuilder description(String description) { this.description = description; return this; }
        public AiPolicyDtoBuilder priority(Integer priority) { this.priority = priority; return this; }
        public AiPolicyDtoBuilder status(String status) { this.status = status; return this; }
        public AiPolicyDtoBuilder version(Integer version) { this.version = version; return this; }
        public AiPolicyDtoBuilder rulesJson(String rulesJson) { this.rulesJson = rulesJson; return this; }
        public AiPolicyDtoBuilder action(String action) { this.action = action; return this; }
        public AiPolicyDtoBuilder approvalRequired(Boolean approvalRequired) { this.approvalRequired = approvalRequired; return this; }
        public AiPolicyDtoBuilder effectiveDate(LocalDateTime effectiveDate) { this.effectiveDate = effectiveDate; return this; }
        public AiPolicyDtoBuilder expiryDate(LocalDateTime expiryDate) { this.expiryDate = expiryDate; return this; }
        public AiPolicyDtoBuilder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        public AiPolicyDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AiPolicyDtoBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public AiPolicyDtoBuilder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public AiPolicyDtoBuilder updatedBy(String updatedBy) { this.updatedBy = updatedBy; return this; }

        public AiPolicyDto build() {
            return new AiPolicyDto(id, policyKey, name, category, description, priority, status, version, rulesJson, action, approvalRequired, effectiveDate, expiryDate, enabled, createdAt, updatedAt, createdBy, updatedBy);
        }
    }
}
