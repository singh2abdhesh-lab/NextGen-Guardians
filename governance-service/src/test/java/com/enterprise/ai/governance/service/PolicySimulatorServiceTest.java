package com.enterprise.ai.governance.service;

import com.enterprise.ai.governance.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Policy Simulator Service Unit Tests")
class PolicySimulatorServiceTest {

    @Mock
    private PolicyManagementService policyService;

    @Mock
    private ModelRegistryService modelService;

    @Mock
    private DepartmentGovernanceService departmentService;

    @Mock
    private UserGovernanceService userService;

    @Mock
    private ApprovalPolicyEngineService approvalPolicyService;

    private PolicySimulatorService simulatorService;

    @BeforeEach
    void setUp() {
        simulatorService = new PolicySimulatorService(
                policyService, modelService, departmentService, userService, approvalPolicyService);
    }

    @Test
    @DisplayName("Simulation should BLOCK if user account is restricted")
    void testRestrictedUserIsBlocked() {
        UserGovernanceDto restrictedUser = UserGovernanceDto.builder()
                .userId("blocked.user@enterprise.ai")
                .isRestricted(true)
                .restrictionReason("Security incident investigation")
                .build();

        when(userService.getByUserId("blocked.user@enterprise.ai")).thenReturn(restrictedUser);

        PolicySimulationRequest request = PolicySimulationRequest.builder()
                .userId("blocked.user@enterprise.ai")
                .department("Engineering")
                .model("gpt-4o")
                .prompt("Hello world")
                .build();

        PolicySimulationResponse response = simulatorService.simulate(request);

        assertNotNull(response);
        assertEquals("BLOCK", response.getExpectedDecision());
        assertTrue(response.getBlockedReasons().stream().anyMatch(r -> r.contains("Security incident investigation")));
        assertTrue(response.getRiskScore() >= 90);
    }

    @Test
    @DisplayName("Simulation should BLOCK if department blocks model")
    void testDepartmentBlockedModelIsBlocked() {
        when(userService.getByUserId(anyString())).thenReturn(null);

        DepartmentGovernanceDto dept = DepartmentGovernanceDto.builder()
                .departmentName("Finance")
                .blockedModels("uncensored-v1,llama-3-8b")
                .allowedModels("gpt-4o")
                .businessHoursOnly(false)
                .emergencyOverrideActive(false)
                .build();

        when(departmentService.getByName("Finance")).thenReturn(dept);

        PolicySimulationRequest request = PolicySimulationRequest.builder()
                .userId("analyst@enterprise.ai")
                .department("Finance")
                .model("uncensored-v1")
                .prompt("Analyze quarterly revenue")
                .build();

        PolicySimulationResponse response = simulatorService.simulate(request);

        assertNotNull(response);
        assertEquals("BLOCK", response.getExpectedDecision());
        assertEquals("BLOCKED_BY_DEPARTMENT", response.getModelAuthorizationStatus());
    }

    @Test
    @DisplayName("Simulation should trigger REVIEW and approval workflow when financial keywords match")
    void testFinancialPolicyTriggersReview() {
        when(userService.getByUserId(anyString())).thenReturn(null);

        DepartmentGovernanceDto dept = DepartmentGovernanceDto.builder()
                .departmentName("Finance")
                .allowedModels("gpt-4o")
                .businessHoursOnly(false)
                .emergencyOverrideActive(false)
                .build();
        when(departmentService.getByName("Finance")).thenReturn(dept);

        ModelRegistrationDto model = ModelRegistrationDto.builder()
                .modelName("gpt-4o")
                .status("APPROVED")
                .enabled(true)
                .build();
        when(modelService.getModelByName("gpt-4o")).thenReturn(model);

        AiPolicyDto financialPolicy = AiPolicyDto.builder()
                .policyKey("POL-FIN-001")
                .name("Earnings Firewall")
                .category("FINANCIAL")
                .rulesJson("{\"keywords\":[\"earnings\",\"forecast\"],\"action\":\"REVIEW\"}")
                .action("REVIEW")
                .approvalRequired(true)
                .priority(5)
                .enabled(true)
                .build();
        when(policyService.getActivePolicies()).thenReturn(List.of(financialPolicy));

        ApprovalPolicyDto approvalWorkflow = ApprovalPolicyDto.builder()
                .policyKey("APPR-FIN-01")
                .name("Financial Review")
                .workflowType("MULTI_LEVEL")
                .primaryApproverRole("FINANCE_DIRECTOR")
                .escalationApproverRole("CFO")
                .autoExpiryHours(24)
                .build();
        when(approvalPolicyService.findEffectivePolicy("FINANCE")).thenReturn(Optional.of(approvalWorkflow));

        PolicySimulationRequest request = PolicySimulationRequest.builder()
                .userId("analyst@enterprise.ai")
                .department("Finance")
                .model("gpt-4o")
                .prompt("Draft our unannounced Q3 earnings forecast summary")
                .build();

        PolicySimulationResponse response = simulatorService.simulate(request);

        assertNotNull(response);
        assertEquals("REVIEW", response.getExpectedDecision());
        assertTrue(response.getApprovalNeeded());
        assertTrue(response.getApprovalWorkflowDetails().contains("FINANCE_DIRECTOR"));
        assertFalse(response.getTriggeredPolicies().isEmpty());
    }
}
