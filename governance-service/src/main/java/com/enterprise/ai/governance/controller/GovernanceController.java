package com.enterprise.ai.governance.controller;

import com.enterprise.ai.governance.dto.GovernanceOverviewDto;
import com.enterprise.ai.governance.dto.ModelRegistrationDto;
import com.enterprise.ai.governance.repository.*;
import com.enterprise.ai.governance.service.ModelRegistryService;
import com.enterprise.ai.governance.service.NotificationService;
import com.enterprise.ai.governance.service.PolicyManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/governance")
@CrossOrigin(origins = "*")
@Tag(name = "Governance Dashboard", description = "Endpoints for executive governance metrics, notifications, and overall health")
public class GovernanceController {

    private final AiPolicyRepository policyRepository;
    private final ModelRegistrationRepository modelRepository;
    private final PromptTemplateRepository templateRepository;
    private final DepartmentGovernanceRepository departmentRepository;
    private final UserGovernanceRepository userRepository;
    private final ApprovalPolicyRepository approvalPolicyRepository;
    private final PolicyManagementService policyService;
    private final NotificationService notificationService;

    public GovernanceController(
            AiPolicyRepository policyRepository,
            ModelRegistrationRepository modelRepository,
            PromptTemplateRepository templateRepository,
            DepartmentGovernanceRepository departmentRepository,
            UserGovernanceRepository userRepository,
            ApprovalPolicyRepository approvalPolicyRepository,
            PolicyManagementService policyService,
            NotificationService notificationService) {
        this.policyRepository = policyRepository;
        this.modelRepository = modelRepository;
        this.templateRepository = templateRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.approvalPolicyRepository = approvalPolicyRepository;
        this.policyService = policyService;
        this.notificationService = notificationService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get Governance Control Center Overview", description = "Returns aggregated platform metrics, budget utilization, active policies, and recent audit activity.")
    public ResponseEntity<GovernanceOverviewDto> getOverview() {
        long totalPolicies = policyRepository.count();
        long activePolicies = policyRepository.findByEnabledTrueOrderByPriorityAsc().size();
        long totalModels = modelRepository.count();
        long approvedModels = modelRepository.findByEnabledTrue().size();
        long totalTemplates = templateRepository.count();
        long totalDepartments = departmentRepository.count();
        long totalUsers = userRepository.count();
        long totalApprovals = approvalPolicyRepository.count();

        double totalBudget = departmentRepository.findAll().stream()
                .mapToDouble(d -> d.getMonthlyCostLimitUsd() != null ? d.getMonthlyCostLimitUsd() : 0.0)
                .sum();

        double currentSpend = departmentRepository.findAll().stream()
                .mapToDouble(d -> d.getCurrentMonthlySpendUsd() != null ? d.getCurrentMonthlySpendUsd() : 0.0)
                .sum();

        double utilization = totalBudget > 0 ? Math.round((currentSpend / totalBudget) * 1000.0) / 10.0 : 0.0;

        Map<String, Long> policiesByCategory = policyRepository.findAll().stream()
                .collect(Collectors.groupingBy(p -> p.getCategory(), Collectors.counting()));

        Map<String, Long> modelsByVendor = modelRepository.findAll().stream()
                .collect(Collectors.groupingBy(m -> m.getVendor(), Collectors.counting()));

        List<Map<String, Object>> recentAudit = policyService.getRecentAuditHistory().stream()
                .limit(10)
                .map(a -> Map.<String, Object>of(
                        "id", a.getId(),
                        "policyKey", a.getPolicyKey(),
                        "version", a.getVersion(),
                        "actionType", a.getActionType(),
                        "changedBy", a.getChangedBy(),
                        "changedAt", a.getChangedAt().toString(),
                        "summary", a.getChangeSummary() != null ? a.getChangeSummary() : ""
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(GovernanceOverviewDto.builder()
                .totalPolicies(totalPolicies)
                .activePolicies(activePolicies)
                .totalModels(totalModels)
                .approvedModels(approvedModels)
                .totalPromptTemplates(totalTemplates)
                .totalDepartments(totalDepartments)
                .totalGovernedUsers(totalUsers)
                .totalApprovalWorkflows(totalApprovals)
                .totalMonthlyBudgetCeilingUsd(totalBudget)
                .currentMonthlySpendUsd(currentSpend)
                .budgetUtilizationPercent(utilization)
                .recentAuditHistory(recentAudit)
                .policiesByCategory(policiesByCategory)
                .modelsByVendor(modelsByVendor)
                .build());
    }

    @GetMapping("/notifications")
    @Operation(summary = "Get recent governance notifications")
    public ResponseEntity<List<Map<String, Object>>> getNotifications() {
        return ResponseEntity.ok(notificationService.getRecentNotifications());
    }

    @GetMapping("/health")
    @Operation(summary = "Service health probe")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "governance-service",
                "version", "1.0.0"
        ));
    }
}
