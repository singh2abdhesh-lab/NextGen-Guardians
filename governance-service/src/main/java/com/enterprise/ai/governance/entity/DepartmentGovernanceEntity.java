package com.enterprise.ai.governance.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gov_department_governance", indexes = {
        @Index(name = "idx_dept_name", columnList = "department_name", unique = true),
        @Index(name = "idx_dept_code", columnList = "department_code", unique = true)
})
public class DepartmentGovernanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_name", nullable = false, length = 100)
    private String departmentName;

    @Column(name = "department_code", nullable = false, length = 20)
    private String departmentCode;

    @Column(name = "daily_token_limit")
    private Long dailyTokenLimit;

    @Column(name = "monthly_cost_limit_usd")
    private Double monthlyCostLimitUsd;

    @Column(name = "current_monthly_spend_usd")
    private Double currentMonthlySpendUsd;

    @Column(name = "allowed_models", length = 1000)
    private String allowedModels;

    @Column(name = "blocked_models", length = 1000)
    private String blockedModels;

    @Column(name = "allowed_prompt_categories", length = 500)
    private String allowedPromptCategories;

    @Column(name = "restricted_categories", length = 500)
    private String restrictedCategories;

    @Column(name = "business_hours_only")
    private Boolean businessHoursOnly;

    @Column(name = "business_hours_start", length = 10)
    private String businessHoursStart;

    @Column(name = "business_hours_end", length = 10)
    private String businessHoursEnd;

    @Column(name = "emergency_override_active")
    private Boolean emergencyOverrideActive;

    @Column(name = "emergency_override_reason", length = 500)
    private String emergencyOverrideReason;

    @Column(name = "emergency_override_until")
    private LocalDateTime emergencyOverrideUntil;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DepartmentGovernanceEntity() {}

    public DepartmentGovernanceEntity(Long id, String departmentName, String departmentCode, Long dailyTokenLimit,
                                      Double monthlyCostLimitUsd, Double currentMonthlySpendUsd, String allowedModels,
                                      String blockedModels, String allowedPromptCategories, String restrictedCategories,
                                      Boolean businessHoursOnly, String businessHoursStart, String businessHoursEnd,
                                      Boolean emergencyOverrideActive, String emergencyOverrideReason,
                                      LocalDateTime emergencyOverrideUntil, LocalDateTime updatedAt) {
        this.id = id;
        this.departmentName = departmentName;
        this.departmentCode = departmentCode;
        this.dailyTokenLimit = dailyTokenLimit;
        this.monthlyCostLimitUsd = monthlyCostLimitUsd;
        this.currentMonthlySpendUsd = currentMonthlySpendUsd;
        this.allowedModels = allowedModels;
        this.blockedModels = blockedModels;
        this.allowedPromptCategories = allowedPromptCategories;
        this.restrictedCategories = restrictedCategories;
        this.businessHoursOnly = businessHoursOnly;
        this.businessHoursStart = businessHoursStart;
        this.businessHoursEnd = businessHoursEnd;
        this.emergencyOverrideActive = emergencyOverrideActive;
        this.emergencyOverrideReason = emergencyOverrideReason;
        this.emergencyOverrideUntil = emergencyOverrideUntil;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void onPrePersist() {
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (dailyTokenLimit == null) dailyTokenLimit = 5_000_000L;
        if (monthlyCostLimitUsd == null) monthlyCostLimitUsd = 2500.0;
        if (currentMonthlySpendUsd == null) currentMonthlySpendUsd = 0.0;
        if (businessHoursOnly == null) businessHoursOnly = false;
        if (businessHoursStart == null) businessHoursStart = "08:00";
        if (businessHoursEnd == null) businessHoursEnd = "18:00";
        if (emergencyOverrideActive == null) emergencyOverrideActive = false;
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public Long getDailyTokenLimit() { return dailyTokenLimit; }
    public void setDailyTokenLimit(Long dailyTokenLimit) { this.dailyTokenLimit = dailyTokenLimit; }
    public Double getMonthlyCostLimitUsd() { return monthlyCostLimitUsd; }
    public void setMonthlyCostLimitUsd(Double monthlyCostLimitUsd) { this.monthlyCostLimitUsd = monthlyCostLimitUsd; }
    public Double getCurrentMonthlySpendUsd() { return currentMonthlySpendUsd; }
    public void setCurrentMonthlySpendUsd(Double currentMonthlySpendUsd) { this.currentMonthlySpendUsd = currentMonthlySpendUsd; }
    public String getAllowedModels() { return allowedModels; }
    public void setAllowedModels(String allowedModels) { this.allowedModels = allowedModels; }
    public String getBlockedModels() { return blockedModels; }
    public void setBlockedModels(String blockedModels) { this.blockedModels = blockedModels; }
    public String getAllowedPromptCategories() { return allowedPromptCategories; }
    public void setAllowedPromptCategories(String allowedPromptCategories) { this.allowedPromptCategories = allowedPromptCategories; }
    public String getRestrictedCategories() { return restrictedCategories; }
    public void setRestrictedCategories(String restrictedCategories) { this.restrictedCategories = restrictedCategories; }
    public Boolean getBusinessHoursOnly() { return businessHoursOnly; }
    public void setBusinessHoursOnly(Boolean businessHoursOnly) { this.businessHoursOnly = businessHoursOnly; }
    public String getBusinessHoursStart() { return businessHoursStart; }
    public void setBusinessHoursStart(String businessHoursStart) { this.businessHoursStart = businessHoursStart; }
    public String getBusinessHoursEnd() { return businessHoursEnd; }
    public void setBusinessHoursEnd(String businessHoursEnd) { this.businessHoursEnd = businessHoursEnd; }
    public Boolean getEmergencyOverrideActive() { return emergencyOverrideActive; }
    public void setEmergencyOverrideActive(Boolean emergencyOverrideActive) { this.emergencyOverrideActive = emergencyOverrideActive; }
    public String getEmergencyOverrideReason() { return emergencyOverrideReason; }
    public void setEmergencyOverrideReason(String emergencyOverrideReason) { this.emergencyOverrideReason = emergencyOverrideReason; }
    public LocalDateTime getEmergencyOverrideUntil() { return emergencyOverrideUntil; }
    public void setEmergencyOverrideUntil(LocalDateTime emergencyOverrideUntil) { this.emergencyOverrideUntil = emergencyOverrideUntil; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static DepartmentGovernanceEntityBuilder builder() {
        return new DepartmentGovernanceEntityBuilder();
    }

    public static class DepartmentGovernanceEntityBuilder {
        private Long id;
        private String departmentName;
        private String departmentCode;
        private Long dailyTokenLimit;
        private Double monthlyCostLimitUsd;
        private Double currentMonthlySpendUsd;
        private String allowedModels;
        private String blockedModels;
        private String allowedPromptCategories;
        private String restrictedCategories;
        private Boolean businessHoursOnly;
        private String businessHoursStart;
        private String businessHoursEnd;
        private Boolean emergencyOverrideActive;
        private String emergencyOverrideReason;
        private LocalDateTime emergencyOverrideUntil;
        private LocalDateTime updatedAt;

        public DepartmentGovernanceEntityBuilder id(Long id) { this.id = id; return this; }
        public DepartmentGovernanceEntityBuilder departmentName(String departmentName) { this.departmentName = departmentName; return this; }
        public DepartmentGovernanceEntityBuilder departmentCode(String departmentCode) { this.departmentCode = departmentCode; return this; }
        public DepartmentGovernanceEntityBuilder dailyTokenLimit(Long dailyTokenLimit) { this.dailyTokenLimit = dailyTokenLimit; return this; }
        public DepartmentGovernanceEntityBuilder monthlyCostLimitUsd(Double monthlyCostLimitUsd) { this.monthlyCostLimitUsd = monthlyCostLimitUsd; return this; }
        public DepartmentGovernanceEntityBuilder currentMonthlySpendUsd(Double currentMonthlySpendUsd) { this.currentMonthlySpendUsd = currentMonthlySpendUsd; return this; }
        public DepartmentGovernanceEntityBuilder allowedModels(String allowedModels) { this.allowedModels = allowedModels; return this; }
        public DepartmentGovernanceEntityBuilder blockedModels(String blockedModels) { this.blockedModels = blockedModels; return this; }
        public DepartmentGovernanceEntityBuilder allowedPromptCategories(String allowedPromptCategories) { this.allowedPromptCategories = allowedPromptCategories; return this; }
        public DepartmentGovernanceEntityBuilder restrictedCategories(String restrictedCategories) { this.restrictedCategories = restrictedCategories; return this; }
        public DepartmentGovernanceEntityBuilder businessHoursOnly(Boolean businessHoursOnly) { this.businessHoursOnly = businessHoursOnly; return this; }
        public DepartmentGovernanceEntityBuilder businessHoursStart(String businessHoursStart) { this.businessHoursStart = businessHoursStart; return this; }
        public DepartmentGovernanceEntityBuilder businessHoursEnd(String businessHoursEnd) { this.businessHoursEnd = businessHoursEnd; return this; }
        public DepartmentGovernanceEntityBuilder emergencyOverrideActive(Boolean emergencyOverrideActive) { this.emergencyOverrideActive = emergencyOverrideActive; return this; }
        public DepartmentGovernanceEntityBuilder emergencyOverrideReason(String emergencyOverrideReason) { this.emergencyOverrideReason = emergencyOverrideReason; return this; }
        public DepartmentGovernanceEntityBuilder emergencyOverrideUntil(LocalDateTime emergencyOverrideUntil) { this.emergencyOverrideUntil = emergencyOverrideUntil; return this; }
        public DepartmentGovernanceEntityBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public DepartmentGovernanceEntity build() {
            return new DepartmentGovernanceEntity(id, departmentName, departmentCode, dailyTokenLimit, monthlyCostLimitUsd,
                    currentMonthlySpendUsd, allowedModels, blockedModels, allowedPromptCategories, restrictedCategories,
                    businessHoursOnly, businessHoursStart, businessHoursEnd, emergencyOverrideActive, emergencyOverrideReason,
                    emergencyOverrideUntil, updatedAt);
        }
    }
}
