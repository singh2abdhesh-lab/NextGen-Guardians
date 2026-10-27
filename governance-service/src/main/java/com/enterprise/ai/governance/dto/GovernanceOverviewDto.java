package com.enterprise.ai.governance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(description = "Executive Governance Summary Metrics")
public class GovernanceOverviewDto {

    private Long totalPolicies;
    private Long activePolicies;
    private Long totalModels;
    private Long approvedModels;
    private Long totalPromptTemplates;
    private Long totalDepartments;
    private Long totalGovernedUsers;
    private Long totalApprovalWorkflows;
    private Double totalMonthlyBudgetCeilingUsd;
    private Double currentMonthlySpendUsd;
    private Double budgetUtilizationPercent;
    private List<Map<String, Object>> recentAuditHistory;
    private Map<String, Long> policiesByCategory;
    private Map<String, Long> modelsByVendor;

    public GovernanceOverviewDto() {}

    public GovernanceOverviewDto(Long totalPolicies, Long activePolicies, Long totalModels, Long approvedModels,
                                 Long totalPromptTemplates, Long totalDepartments, Long totalGovernedUsers,
                                 Long totalApprovalWorkflows, Double totalMonthlyBudgetCeilingUsd,
                                 Double currentMonthlySpendUsd, Double budgetUtilizationPercent,
                                 List<Map<String, Object>> recentAuditHistory,
                                 Map<String, Long> policiesByCategory, Map<String, Long> modelsByVendor) {
        this.totalPolicies = totalPolicies;
        this.activePolicies = activePolicies;
        this.totalModels = totalModels;
        this.approvedModels = approvedModels;
        this.totalPromptTemplates = totalPromptTemplates;
        this.totalDepartments = totalDepartments;
        this.totalGovernedUsers = totalGovernedUsers;
        this.totalApprovalWorkflows = totalApprovalWorkflows;
        this.totalMonthlyBudgetCeilingUsd = totalMonthlyBudgetCeilingUsd;
        this.currentMonthlySpendUsd = currentMonthlySpendUsd;
        this.budgetUtilizationPercent = budgetUtilizationPercent;
        this.recentAuditHistory = recentAuditHistory;
        this.policiesByCategory = policiesByCategory;
        this.modelsByVendor = modelsByVendor;
    }

    // Getters and Setters
    public Long getTotalPolicies() { return totalPolicies; }
    public void setTotalPolicies(Long totalPolicies) { this.totalPolicies = totalPolicies; }
    public Long getActivePolicies() { return activePolicies; }
    public void setActivePolicies(Long activePolicies) { this.activePolicies = activePolicies; }
    public Long getTotalModels() { return totalModels; }
    public void setTotalModels(Long totalModels) { this.totalModels = totalModels; }
    public Long getApprovedModels() { return approvedModels; }
    public void setApprovedModels(Long approvedModels) { this.approvedModels = approvedModels; }
    public Long getTotalPromptTemplates() { return totalPromptTemplates; }
    public void setTotalPromptTemplates(Long totalPromptTemplates) { this.totalPromptTemplates = totalPromptTemplates; }
    public Long getTotalDepartments() { return totalDepartments; }
    public void setTotalDepartments(Long totalDepartments) { this.totalDepartments = totalDepartments; }
    public Long getTotalGovernedUsers() { return totalGovernedUsers; }
    public void setTotalGovernedUsers(Long totalGovernedUsers) { this.totalGovernedUsers = totalGovernedUsers; }
    public Long getTotalApprovalWorkflows() { return totalApprovalWorkflows; }
    public void setTotalApprovalWorkflows(Long totalApprovalWorkflows) { this.totalApprovalWorkflows = totalApprovalWorkflows; }
    public Double getTotalMonthlyBudgetCeilingUsd() { return totalMonthlyBudgetCeilingUsd; }
    public void setTotalMonthlyBudgetCeilingUsd(Double totalMonthlyBudgetCeilingUsd) { this.totalMonthlyBudgetCeilingUsd = totalMonthlyBudgetCeilingUsd; }
    public Double getCurrentMonthlySpendUsd() { return currentMonthlySpendUsd; }
    public void setCurrentMonthlySpendUsd(Double currentMonthlySpendUsd) { this.currentMonthlySpendUsd = currentMonthlySpendUsd; }
    public Double getBudgetUtilizationPercent() { return budgetUtilizationPercent; }
    public void setBudgetUtilizationPercent(Double budgetUtilizationPercent) { this.budgetUtilizationPercent = budgetUtilizationPercent; }
    public List<Map<String, Object>> getRecentAuditHistory() { return recentAuditHistory; }
    public void setRecentAuditHistory(List<Map<String, Object>> recentAuditHistory) { this.recentAuditHistory = recentAuditHistory; }
    public Map<String, Long> getPoliciesByCategory() { return policiesByCategory; }
    public void setPoliciesByCategory(Map<String, Long> policiesByCategory) { this.policiesByCategory = policiesByCategory; }
    public Map<String, Long> getModelsByVendor() { return modelsByVendor; }
    public void setModelsByVendor(Map<String, Long> modelsByVendor) { this.modelsByVendor = modelsByVendor; }

    public static GovernanceOverviewDtoBuilder builder() {
        return new GovernanceOverviewDtoBuilder();
    }

    public static class GovernanceOverviewDtoBuilder {
        private Long totalPolicies;
        private Long activePolicies;
        private Long totalModels;
        private Long approvedModels;
        private Long totalPromptTemplates;
        private Long totalDepartments;
        private Long totalGovernedUsers;
        private Long totalApprovalWorkflows;
        private Double totalMonthlyBudgetCeilingUsd;
        private Double currentMonthlySpendUsd;
        private Double budgetUtilizationPercent;
        private List<Map<String, Object>> recentAuditHistory;
        private Map<String, Long> policiesByCategory;
        private Map<String, Long> modelsByVendor;

        public GovernanceOverviewDtoBuilder totalPolicies(Long totalPolicies) { this.totalPolicies = totalPolicies; return this; }
        public GovernanceOverviewDtoBuilder activePolicies(Long activePolicies) { this.activePolicies = activePolicies; return this; }
        public GovernanceOverviewDtoBuilder totalModels(Long totalModels) { this.totalModels = totalModels; return this; }
        public GovernanceOverviewDtoBuilder approvedModels(Long approvedModels) { this.approvedModels = approvedModels; return this; }
        public GovernanceOverviewDtoBuilder totalPromptTemplates(Long totalPromptTemplates) { this.totalPromptTemplates = totalPromptTemplates; return this; }
        public GovernanceOverviewDtoBuilder totalDepartments(Long totalDepartments) { this.totalDepartments = totalDepartments; return this; }
        public GovernanceOverviewDtoBuilder totalGovernedUsers(Long totalGovernedUsers) { this.totalGovernedUsers = totalGovernedUsers; return this; }
        public GovernanceOverviewDtoBuilder totalApprovalWorkflows(Long totalApprovalWorkflows) { this.totalApprovalWorkflows = totalApprovalWorkflows; return this; }
        public GovernanceOverviewDtoBuilder totalMonthlyBudgetCeilingUsd(Double totalMonthlyBudgetCeilingUsd) { this.totalMonthlyBudgetCeilingUsd = totalMonthlyBudgetCeilingUsd; return this; }
        public GovernanceOverviewDtoBuilder currentMonthlySpendUsd(Double currentMonthlySpendUsd) { this.currentMonthlySpendUsd = currentMonthlySpendUsd; return this; }
        public GovernanceOverviewDtoBuilder budgetUtilizationPercent(Double budgetUtilizationPercent) { this.budgetUtilizationPercent = budgetUtilizationPercent; return this; }
        public GovernanceOverviewDtoBuilder recentAuditHistory(List<Map<String, Object>> recentAuditHistory) { this.recentAuditHistory = recentAuditHistory; return this; }
        public GovernanceOverviewDtoBuilder policiesByCategory(Map<String, Long> policiesByCategory) { this.policiesByCategory = policiesByCategory; return this; }
        public GovernanceOverviewDtoBuilder modelsByVendor(Map<String, Long> modelsByVendor) { this.modelsByVendor = modelsByVendor; return this; }

        public GovernanceOverviewDto build() {
            return new GovernanceOverviewDto(totalPolicies, activePolicies, totalModels, approvedModels,
                    totalPromptTemplates, totalDepartments, totalGovernedUsers, totalApprovalWorkflows,
                    totalMonthlyBudgetCeilingUsd, currentMonthlySpendUsd, budgetUtilizationPercent,
                    recentAuditHistory, policiesByCategory, modelsByVendor);
        }
    }
}
