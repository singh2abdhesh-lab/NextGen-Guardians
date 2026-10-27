package com.enterprise.ai.governance.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gov_policy_audit_history", indexes = {
        @Index(name = "idx_hist_pol_id", columnList = "policy_id"),
        @Index(name = "idx_hist_pol_key", columnList = "policy_key"),
        @Index(name = "idx_hist_changed_at", columnList = "changed_at")
})
public class PolicyAuditHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @Column(name = "policy_key", nullable = false, length = 64)
    private String policyKey;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "action_type", nullable = false, length = 40)
    private String actionType;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "change_summary", length = 500)
    private String changeSummary;

    @Lob
    @Column(name = "old_value_json", columnDefinition = "TEXT")
    private String oldValueJson;

    @Lob
    @Column(name = "new_value_json", columnDefinition = "TEXT")
    private String newValueJson;

    public PolicyAuditHistoryEntity() {}

    public PolicyAuditHistoryEntity(Long id, Long policyId, String policyKey, Integer version,
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

    @PrePersist
    public void onPrePersist() {
        if (changedAt == null) changedAt = LocalDateTime.now();
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

    public static PolicyAuditHistoryEntityBuilder builder() {
        return new PolicyAuditHistoryEntityBuilder();
    }

    public static class PolicyAuditHistoryEntityBuilder {
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

        public PolicyAuditHistoryEntityBuilder id(Long id) { this.id = id; return this; }
        public PolicyAuditHistoryEntityBuilder policyId(Long policyId) { this.policyId = policyId; return this; }
        public PolicyAuditHistoryEntityBuilder policyKey(String policyKey) { this.policyKey = policyKey; return this; }
        public PolicyAuditHistoryEntityBuilder version(Integer version) { this.version = version; return this; }
        public PolicyAuditHistoryEntityBuilder actionType(String actionType) { this.actionType = actionType; return this; }
        public PolicyAuditHistoryEntityBuilder changedBy(String changedBy) { this.changedBy = changedBy; return this; }
        public PolicyAuditHistoryEntityBuilder changedAt(LocalDateTime changedAt) { this.changedAt = changedAt; return this; }
        public PolicyAuditHistoryEntityBuilder changeSummary(String changeSummary) { this.changeSummary = changeSummary; return this; }
        public PolicyAuditHistoryEntityBuilder oldValueJson(String oldValueJson) { this.oldValueJson = oldValueJson; return this; }
        public PolicyAuditHistoryEntityBuilder newValueJson(String newValueJson) { this.newValueJson = newValueJson; return this; }

        public PolicyAuditHistoryEntity build() {
            return new PolicyAuditHistoryEntity(id, policyId, policyKey, version, actionType, changedBy, changedAt, changeSummary, oldValueJson, newValueJson);
        }
    }
}
