package com.enterprise.ai.governance.service;

import com.enterprise.ai.governance.dto.*;
import com.enterprise.ai.governance.repository.DepartmentGovernanceRepository;
import com.enterprise.ai.governance.repository.ModelRegistrationRepository;
import com.enterprise.ai.governance.repository.UserGovernanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class PolicySimulatorService {

    private static final Logger log = LoggerFactory.getLogger(PolicySimulatorService.class);

    private final PolicyManagementService policyService;
    private final ModelRegistryService modelService;
    private final DepartmentGovernanceService departmentService;
    private final UserGovernanceService userService;
    private final ApprovalPolicyEngineService approvalPolicyService;

    public PolicySimulatorService(
            PolicyManagementService policyService,
            ModelRegistryService modelService,
            DepartmentGovernanceService departmentService,
            UserGovernanceService userService,
            ApprovalPolicyEngineService approvalPolicyService) {
        this.policyService = policyService;
        this.modelService = modelService;
        this.departmentService = departmentService;
        this.userService = userService;
        this.approvalPolicyService = approvalPolicyService;
    }

    public PolicySimulationResponse simulate(PolicySimulationRequest request) {
        long start = System.currentTimeMillis();

        List<String> triggeredPolicies = new ArrayList<>();
        List<String> blockedReasons = new ArrayList<>();
        List<String> suggestedRemediations = new ArrayList<>();
        int computedRiskScore = 15; // baseline
        String decision = "ALLOW";
        boolean approvalNeeded = false;
        String workflowDetails = "Standard Non-Gated Fastpath";
        String deptQuotaStatus = "PASSED";
        String modelAuthStatus = "PERMITTED";

        String promptLower = request.getPrompt().toLowerCase();

        // 1. Check User Level Governance
        UserGovernanceDto user = userService.getByUserId(request.getUserId());
        if (user != null && Boolean.TRUE.equals(user.getIsRestricted())) {
            decision = "BLOCK";
            blockedReasons.add("User account is restricted: " + (user.getRestrictionReason() != null ? user.getRestrictionReason() : "Governance lock"));
            suggestedRemediations.add("Contact SecOps compliance officer to request user account reinstatement.");
            computedRiskScore = Math.max(computedRiskScore, 90);
        }

        // 2. Check Department Constraints
        DepartmentGovernanceDto dept = null;
        try {
            dept = departmentService.getByName(request.getDepartment());
        } catch (Exception ignored) {}

        if (dept != null) {
            // Check Emergency Override
            boolean hasOverride = Boolean.TRUE.equals(dept.getEmergencyOverrideActive());

            if (!hasOverride) {
                // Check Blocked Models
                if (dept.getBlockedModels() != null && containsIgnoreCase(dept.getBlockedModels(), request.getModel())) {
                    decision = "BLOCK";
                    modelAuthStatus = "BLOCKED_BY_DEPARTMENT";
                    blockedReasons.add("Model '" + request.getModel() + "' is explicitly prohibited for " + dept.getDepartmentName());
                    suggestedRemediations.add("Select an authorized department model: " + dept.getAllowedModels());
                    computedRiskScore = Math.max(computedRiskScore, 80);
                }

                // Check Allowed Models
                if (dept.getAllowedModels() != null && !dept.getAllowedModels().isBlank() &&
                        !containsIgnoreCase(dept.getAllowedModels(), request.getModel())) {
                    decision = "BLOCK";
                    modelAuthStatus = "NOT_IN_DEPARTMENT_ALLOWLIST";
                    blockedReasons.add("Model '" + request.getModel() + "' is not in allowed models list for " + dept.getDepartmentName());
                    suggestedRemediations.add("Switch to one of: " + dept.getAllowedModels());
                    computedRiskScore = Math.max(computedRiskScore, 70);
                }

                // Check Business Hours
                if (!departmentService.isWithinBusinessHours(dept)) {
                    decision = "REVIEW";
                    approvalNeeded = true;
                    blockedReasons.add("Department enforces business hours (" + dept.getBusinessHoursStart() + " - " + dept.getBusinessHoursEnd() + "). Off-hours query requires escalation.");
                    suggestedRemediations.add("Submit during operational business hours or request an Emergency Override.");
                    computedRiskScore = Math.max(computedRiskScore, 50);
                }

                // Check Restricted Categories
                if (dept.getRestrictedCategories() != null && request.getRequestedCategory() != null) {
                    if (containsIgnoreCase(dept.getRestrictedCategories(), request.getRequestedCategory())) {
                        decision = "BLOCK";
                        blockedReasons.add("Category '" + request.getRequestedCategory() + "' is restricted for " + dept.getDepartmentName());
                        computedRiskScore = Math.max(computedRiskScore, 85);
                    }
                }
            } else {
                deptQuotaStatus = "EMERGENCY_OVERRIDE_ACTIVE";
            }
        }

        // 3. Check Model Status in Registry
        try {
            ModelRegistrationDto modelReg = modelService.getModelByName(request.getModel());
            if ("BLOCKED".equalsIgnoreCase(modelReg.getStatus())) {
                decision = "BLOCK";
                modelAuthStatus = "MODEL_GLOBALLY_BLOCKED";
                blockedReasons.add("Target model '" + request.getModel() + "' is globally disabled in Enterprise Model Registry");
                computedRiskScore = Math.max(computedRiskScore, 95);
            } else if ("RESTRICTED".equalsIgnoreCase(modelReg.getStatus())) {
                if (!"BLOCK".equals(decision)) decision = "REVIEW";
                approvalNeeded = true;
                modelAuthStatus = "RESTRICTED_ACCESS";
                blockedReasons.add("Target model is in restricted evaluation status");
                computedRiskScore = Math.max(computedRiskScore, 60);
            }
        } catch (Exception e) {
            // Model not in registry
            if (!"BLOCK".equals(decision)) decision = "REVIEW";
            approvalNeeded = true;
            modelAuthStatus = "UNREGISTERED_MODEL";
            blockedReasons.add("Model '" + request.getModel() + "' is not registered in Enterprise Model Registry");
            computedRiskScore = Math.max(computedRiskScore, 65);
        }

        // 4. Evaluate Active AI Policies
        List<AiPolicyDto> policies = policyService.getActivePolicies();
        if (request.getSpecificPolicyKey() != null && !request.getSpecificPolicyKey().isBlank()) {
            policies = policies.stream()
                    .filter(p -> p.getPolicyKey().equalsIgnoreCase(request.getSpecificPolicyKey()))
                    .toList();
        }

        for (AiPolicyDto policy : policies) {
            boolean triggered = evaluatePolicyRules(policy, promptLower, request);
            if (triggered) {
                triggeredPolicies.add(policy.getName() + " [" + policy.getPolicyKey() + "] (" + policy.getAction() + ")");

                if (Boolean.TRUE.equals(policy.getApprovalRequired())) {
                    approvalNeeded = true;
                }

                if ("BLOCK".equalsIgnoreCase(policy.getAction())) {
                    decision = "BLOCK";
                    blockedReasons.add("Triggered blocking policy: " + policy.getName());
                    computedRiskScore = Math.max(computedRiskScore, 80 + (100 - policy.getPriority()) / 5);
                } else if ("REVIEW".equalsIgnoreCase(policy.getAction())) {
                    if (!"BLOCK".equals(decision)) {
                        decision = "REVIEW";
                        approvalNeeded = true;
                    }
                    computedRiskScore = Math.max(computedRiskScore, 50 + (100 - policy.getPriority()) / 4);
                } else if ("REDACT".equalsIgnoreCase(policy.getAction())) {
                    suggestedRemediations.add("Automatic token redaction will be enforced on sensitive entities matching: " + policy.getName());
                }
            }
        }

        // 5. Check Approval Workflows
        if (approvalNeeded || "REVIEW".equals(decision)) {
            String triggerCat = "HIGH_RISK";
            if (promptLower.contains("earnings") || promptLower.contains("financial") || promptLower.contains("revenue")) {
                triggerCat = "FINANCE";
            } else if (promptLower.contains("nda") || promptLower.contains("contract") || promptLower.contains("legal")) {
                triggerCat = "LEGAL";
            } else if (promptLower.contains("class ") || promptLower.contains("def ") || promptLower.contains("function")) {
                triggerCat = "SOURCE_CODE";
            } else if (promptLower.contains("ssn") || promptLower.contains("customer")) {
                triggerCat = "CUSTOMER_DATA";
            }

            Optional<ApprovalPolicyDto> approvalPolicy = approvalPolicyService.findEffectivePolicy(triggerCat);
            if (approvalPolicy.isPresent()) {
                ApprovalPolicyDto ap = approvalPolicy.get();
                workflowDetails = ap.getWorkflowType() + " (Approver Role: " + ap.getPrimaryApproverRole() +
                        (ap.getEscalationApproverRole() != null ? ", Escalation: " + ap.getEscalationApproverRole() : "") +
                        ", Auto-Expiry: " + ap.getAutoExpiryHours() + "h)";
            } else {
                workflowDetails = "SINGLE_APPROVAL (Role: SECURITY_LEAD, Auto-Expiry: 24h)";
            }
        }

        long latency = System.currentTimeMillis() - start;

        return PolicySimulationResponse.builder()
                .expectedDecision(decision)
                .riskScore(Math.min(100, computedRiskScore))
                .approvalNeeded(approvalNeeded || "REVIEW".equals(decision))
                .approvalWorkflowDetails(workflowDetails)
                .triggeredPolicies(triggeredPolicies)
                .blockedReasons(blockedReasons)
                .suggestedRemediations(suggestedRemediations)
                .departmentQuotaStatus(deptQuotaStatus)
                .modelAuthorizationStatus(modelAuthStatus)
                .latencyMs(latency)
                .build();
    }

    private boolean evaluatePolicyRules(AiPolicyDto policy, String promptLower, PolicySimulationRequest req) {
        String rulesJson = policy.getRulesJson();
        if (rulesJson == null || rulesJson.isBlank()) {
            return false;
        }

        // 1. Keyword check
        if (rulesJson.contains("keywords")) {
            if (policy.getCategory().equalsIgnoreCase("FINANCIAL") &&
                    (promptLower.contains("earnings") || promptLower.contains("quarterly") || promptLower.contains("forecast") || promptLower.contains("revenue") || promptLower.contains("salary"))) {
                return true;
            }
            if (policy.getCategory().equalsIgnoreCase("DATA_PRIVACY") &&
                    (promptLower.contains("ssn") || promptLower.contains("social security") || promptLower.contains("credit card") || promptLower.contains("passport"))) {
                return true;
            }
            if (policy.getCategory().equalsIgnoreCase("SECURITY") &&
                    (promptLower.contains("ignore previous") || promptLower.contains("system prompt") || promptLower.contains("jailbreak") || promptLower.contains("bypass"))) {
                return true;
            }
            if (policy.getCategory().equalsIgnoreCase("CODE_INTEGRITY") &&
                    (promptLower.contains("private_key") || promptLower.contains("api_key") || promptLower.contains("aws_secret") || promptLower.contains("password"))) {
                return true;
            }
        }

        // 2. Direct string containment
        if (rulesJson.contains("trade secret") && promptLower.contains("trade secret")) return true;
        if (rulesJson.contains("unreleased") && promptLower.contains("unreleased")) return true;

        return false;
    }

    private boolean containsIgnoreCase(String csv, String target) {
        if (csv == null || target == null) return false;
        String[] parts = csv.split(",");
        for (String p : parts) {
            if (p.trim().equalsIgnoreCase(target.trim())) {
                return true;
            }
        }
        return false;
    }
}
