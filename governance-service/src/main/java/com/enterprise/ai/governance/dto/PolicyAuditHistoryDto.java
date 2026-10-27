package com.enterprise.ai.governance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Policy Audit Trail Record")
public class PolicyAuditHistoryDto {

    private Long id;
    private Long policyId;
    private String policyKey;
    private Integer version;
    private String actionType; // CREATE, UPDATE, ROLLBACK, ENABLE, DISABLE
    private String changedBy;
    private LocalDateTime changedAt;
    private String changeSummary;
    private String oldValueJson;
    private String newValueJson;

    public PolicyAuditHistoryDto() {}

    public PolicyAuditHistoryDto(Long id, Long policyId, String policyKey, Integer version,
                                 String actionType, String changedBy, LocalDateTime changedAt,
                                 String changeSummary, String oldValueJson, String newValueJson) {
        this.id = id;
        this.policyId = policyId;
        this.policyKey = policyKey;
        this.version = version;
        this.actionType = actionType;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
        this.changeSummary = changeSummary;
        this.oldValueJson = oldValueJson;
        this.newValueJson = newValueJson;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public String getPolicyKey() { return policyKey; }
    public void setPolicyKey(String policyKey) { this.policyKey = policyKey; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
    public String getChangeSummary() { return changeSummary; }
    public void setChangeSummary(String changeSummary) { this.changeSummary = changeSummary; }
    public String getOldValueJson() { return oldValueJson; }
    public void setOldValueJson(String oldValueJson) { this.oldValueJson = oldValueJson; }
    public String getNewValueJson() { return newValueJson; }
    public void setNewValueJson(String newValueJson) { this.newValueJson = newValueJson; }

    public static PolicyAuditHistoryDtoBuilder builder() {
        return new PolicyAuditHistoryDtoBuilder();
    }

    public static class PolicyAuditHistoryDtoBuilder {
        private Long id;
        private Long policyId;
        private String policyKey;
        private Integer version;
        private String actionType;
        private String changedBy;
        private LocalDateTime changedAt;
        private String changeSummary;
        private String oldValueJson;
        private String newValueJson;

        public PolicyAuditHistoryDtoBuilder id(Long id) { this.id = id; return this; }
        public PolicyAuditHistoryDtoBuilder policyId(Long policyId) { this.policyId = policyId; return this; }
        public PolicyAuditHistoryDtoBuilder policyKey(String policyKey) { this.policyKey = policyKey; return this; }
        public PolicyAuditHistoryDtoBuilder version(Integer version) { this.version = version; return this; }
        public PolicyAuditHistoryDtoBuilder actionType(String actionType) { this.actionType = actionType; return this; }
        public PolicyAuditHistoryDtoBuilder changedBy(String changedBy) { this.changedBy = changedBy; return this; }
        public PolicyAuditHistoryDtoBuilder changedAt(LocalDateTime changedAt) { this.changedAt = changedAt; return this; }
        public PolicyAuditHistoryDtoBuilder changeSummary(String changeSummary) { this.changeSummary = changeSummary; return this; }
        public PolicyAuditHistoryDtoBuilder oldValueJson(String oldValueJson) { this.oldValueJson = oldValueJson; return this; }
        public PolicyAuditHistoryDtoBuilder newValueJson(String newValueJson) { this.newValueJson = newValueJson; return this; }

        public PolicyAuditHistoryDto build() {
            return new PolicyAuditHistoryDto(id, policyId, policyKey, version, actionType, changedBy, changedAt, changeSummary, oldValueJson, newValueJson);
        }
    }
}
