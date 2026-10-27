package com.enterprise.ai.governance.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Outcome of AI Policy Simulation")
public class PolicySimulationResponse {

    @Schema(description = "Expected final verdict: ALLOW, BLOCK, REVIEW", example = "REVIEW")
    private String expectedDecision;

    @Schema(description = "Composite contextual risk score (0-100)", example = "65")
    private Integer riskScore;

    @Schema(description = "Indicates whether human-in-the-loop review is triggered", example = "true")
    private Boolean approvalNeeded;

    @Schema(description = "Approval workflow tier details if approval needed")
    private String approvalWorkflowDetails;

    @Schema(description = "List of policy titles and keys triggered during evaluation")
    private List<String> triggeredPolicies;

    @Schema(description = "Explanations for blocking or reviewing")
    private List<String> blockedReasons;

    @Schema(description = "Remediation recommendations for user or compliance")
    private List<String> suggestedRemediations;

    @Schema(description = "Department quota check status", example = "PASSED")
    private String departmentQuotaStatus;

    @Schema(description = "Model authorization check status", example = "PERMITTED")
    private String modelAuthorizationStatus;

    @Schema(description = "Processing simulation execution latency in milliseconds", example = "12")
    private Long latencyMs;

    public PolicySimulationResponse() {}

    public PolicySimulationResponse(String expectedDecision, Integer riskScore, Boolean approvalNeeded,
                                    String approvalWorkflowDetails, List<String> triggeredPolicies,
                                    List<String> blockedReasons, List<String> suggestedRemediations,
                                    String departmentQuotaStatus, String modelAuthorizationStatus, Long latencyMs) {
        this.expectedDecision = expectedDecision;
        this.riskScore = riskScore;
        this.approvalNeeded = approvalNeeded;
        this.approvalWorkflowDetails = approvalWorkflowDetails;
        this.triggeredPolicies = triggeredPolicies;
        this.blockedReasons = blockedReasons;
        this.suggestedRemediations = suggestedRemediations;
        this.departmentQuotaStatus = departmentQuotaStatus;
        this.modelAuthorizationStatus = modelAuthorizationStatus;
        this.latencyMs = latencyMs;
    }

    // Getters and Setters
    public String getExpectedDecision() { return expectedDecision; }
    public void setExpectedDecision(String expectedDecision) { this.expectedDecision = expectedDecision; }
    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }
    public Boolean getApprovalNeeded() { return approvalNeeded; }
    public void setApprovalNeeded(Boolean approvalNeeded) { this.approvalNeeded = approvalNeeded; }
    public String getApprovalWorkflowDetails() { return approvalWorkflowDetails; }
    public void setApprovalWorkflowDetails(String approvalWorkflowDetails) { this.approvalWorkflowDetails = approvalWorkflowDetails; }
    public List<String> getTriggeredPolicies() { return triggeredPolicies; }
    public void setTriggeredPolicies(List<String> triggeredPolicies) { this.triggeredPolicies = triggeredPolicies; }
    public List<String> getBlockedReasons() { return blockedReasons; }
    public void setBlockedReasons(List<String> blockedReasons) { this.blockedReasons = blockedReasons; }
    public List<String> getSuggestedRemediations() { return suggestedRemediations; }
    public void setSuggestedRemediations(List<String> suggestedRemediations) { this.suggestedRemediations = suggestedRemediations; }
    public String getDepartmentQuotaStatus() { return departmentQuotaStatus; }
    public void setDepartmentQuotaStatus(String departmentQuotaStatus) { this.departmentQuotaStatus = departmentQuotaStatus; }
    public String getModelAuthorizationStatus() { return modelAuthorizationStatus; }
    public void setModelAuthorizationStatus(String modelAuthorizationStatus) { this.modelAuthorizationStatus = modelAuthorizationStatus; }
    public Long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }

    public static PolicySimulationResponseBuilder builder() {
        return new PolicySimulationResponseBuilder();
    }

    public static class PolicySimulationResponseBuilder {
        private String expectedDecision;
        private Integer riskScore;
        private Boolean approvalNeeded;
        private String approvalWorkflowDetails;
        private List<String> triggeredPolicies;
        private List<String> blockedReasons;
        private List<String> suggestedRemediations;
        private String departmentQuotaStatus;
        private String modelAuthorizationStatus;
        private Long latencyMs;

        public PolicySimulationResponseBuilder expectedDecision(String expectedDecision) { this.expectedDecision = expectedDecision; return this; }
        public PolicySimulationResponseBuilder riskScore(Integer riskScore) { this.riskScore = riskScore; return this; }
        public PolicySimulationResponseBuilder approvalNeeded(Boolean approvalNeeded) { this.approvalNeeded = approvalNeeded; return this; }
        public PolicySimulationResponseBuilder approvalWorkflowDetails(String approvalWorkflowDetails) { this.approvalWorkflowDetails = approvalWorkflowDetails; return this; }
        public PolicySimulationResponseBuilder triggeredPolicies(List<String> triggeredPolicies) { this.triggeredPolicies = triggeredPolicies; return this; }
        public PolicySimulationResponseBuilder blockedReasons(List<String> blockedReasons) { this.blockedReasons = blockedReasons; return this; }
        public PolicySimulationResponseBuilder suggestedRemediations(List<String> suggestedRemediations) { this.suggestedRemediations = suggestedRemediations; return this; }
        public PolicySimulationResponseBuilder departmentQuotaStatus(String departmentQuotaStatus) { this.departmentQuotaStatus = departmentQuotaStatus; return this; }
        public PolicySimulationResponseBuilder modelAuthorizationStatus(String modelAuthorizationStatus) { this.modelAuthorizationStatus = modelAuthorizationStatus; return this; }
        public PolicySimulationResponseBuilder latencyMs(Long latencyMs) { this.latencyMs = latencyMs; return this; }

        public PolicySimulationResponse build() {
            return new PolicySimulationResponse(expectedDecision, riskScore, approvalNeeded, approvalWorkflowDetails,
                    triggeredPolicies, blockedReasons, suggestedRemediations, departmentQuotaStatus, modelAuthorizationStatus, latencyMs);
        }
    }
}
