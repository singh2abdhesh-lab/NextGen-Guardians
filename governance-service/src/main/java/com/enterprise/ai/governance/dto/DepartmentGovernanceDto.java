package com.enterprise.ai.governance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Schema(description = "Department AI Governance Quotas and Constraints")
public class DepartmentGovernanceDto {

    private Long id;

    @NotBlank(message = "Department name cannot be blank")
    @Schema(description = "Department name", example = "Engineering")
    private String departmentName;

    @NotBlank(message = "Department code cannot be blank")
    @Schema(description = "Short code", example = "ENG")
    private String departmentCode;

    @Schema(description = "Max total daily tokens for department", example = "10000000")
    private Long dailyTokenLimit;

    @Schema(description = "Monthly budget ceiling in USD", example = "5000.0")
    private Double monthlyCostLimitUsd;

    @Schema(description = "Accrued month-to-date dollar cost", example = "1420.50")
    private Double currentMonthlySpendUsd;

    @Schema(description = "Comma-delimited permitted models", example = "gpt-4o,claude-3-5-sonnet")
    private String allowedModels;

    @Schema(description = "Comma-delimited prohibited models", example = "uncensored-v1")
    private String blockedModels;

    @Schema(description = "Permitted prompt categories", example = "CODE,DOCUMENTATION,GENERAL")
    private String allowedPromptCategories;

    @Schema(description = "Blocked prompt categories", example = "PAYROLL,TRADE_SECRET")
    private String restrictedCategories;

    private Boolean businessHoursOnly;
    private String businessHoursStart; // "08:00"
    private String businessHoursEnd;   // "18:00"

    private Boolean emergencyOverrideActive;
    private String emergencyOverrideReason;
    private LocalDateTime emergencyOverrideUntil;
    private LocalDateTime updatedAt;

    public DepartmentGovernanceDto() {}

    public DepartmentGovernanceDto(Long id, String departmentName, String departmentCode, Long dailyTokenLimit,
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

    public static DepartmentGovernanceDtoBuilder builder() {
        return new DepartmentGovernanceDtoBuilder();
    }

    public static class DepartmentGovernanceDtoBuilder {
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

        public DepartmentGovernanceDtoBuilder id(Long id) { this.id = id; return this; }
        public DepartmentGovernanceDtoBuilder departmentName(String departmentName) { this.departmentName = departmentName; return this; }
        public DepartmentGovernanceDtoBuilder departmentCode(String departmentCode) { this.departmentCode = departmentCode; return this; }
        public DepartmentGovernanceDtoBuilder dailyTokenLimit(Long dailyTokenLimit) { this.dailyTokenLimit = dailyTokenLimit; return this; }
        public DepartmentGovernanceDtoBuilder monthlyCostLimitUsd(Double monthlyCostLimitUsd) { this.monthlyCostLimitUsd = monthlyCostLimitUsd; return this; }
        public DepartmentGovernanceDtoBuilder currentMonthlySpendUsd(Double currentMonthlySpendUsd) { this.currentMonthlySpendUsd = currentMonthlySpendUsd; return this; }
        public DepartmentGovernanceDtoBuilder allowedModels(String allowedModels) { this.allowedModels = allowedModels; return this; }
        public DepartmentGovernanceDtoBuilder blockedModels(String blockedModels) { this.blockedModels = blockedModels; return this; }
        public DepartmentGovernanceDtoBuilder allowedPromptCategories(String allowedPromptCategories) { this.allowedPromptCategories = allowedPromptCategories; return this; }
        public DepartmentGovernanceDtoBuilder restrictedCategories(String restrictedCategories) { this.restrictedCategories = restrictedCategories; return this; }
        public DepartmentGovernanceDtoBuilder businessHoursOnly(Boolean businessHoursOnly) { this.businessHoursOnly = businessHoursOnly; return this; }
        public DepartmentGovernanceDtoBuilder businessHoursStart(String businessHoursStart) { this.businessHoursStart = businessHoursStart; return this; }
        public DepartmentGovernanceDtoBuilder businessHoursEnd(String businessHoursEnd) { this.businessHoursEnd = businessHoursEnd; return this; }
        public DepartmentGovernanceDtoBuilder emergencyOverrideActive(Boolean emergencyOverrideActive) { this.emergencyOverrideActive = emergencyOverrideActive; return this; }
        public DepartmentGovernanceDtoBuilder emergencyOverrideReason(String emergencyOverrideReason) { this.emergencyOverrideReason = emergencyOverrideReason; return this; }
        public DepartmentGovernanceDtoBuilder emergencyOverrideUntil(LocalDateTime emergencyOverrideUntil) { this.emergencyOverrideUntil = emergencyOverrideUntil; return this; }
        public DepartmentGovernanceDtoBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public DepartmentGovernanceDto build() {
            return new DepartmentGovernanceDto(id, departmentName, departmentCode, dailyTokenLimit, monthlyCostLimitUsd,
                    currentMonthlySpendUsd, allowedModels, blockedModels, allowedPromptCategories, restrictedCategories,
                    businessHoursOnly, businessHoursStart, businessHoursEnd, emergencyOverrideActive, emergencyOverrideReason,
                    emergencyOverrideUntil, updatedAt);
        }
    }
}
