package com.enterprise.ai.governance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Input parameters to simulate AI Governance evaluation")
public class PolicySimulationRequest {

    @NotBlank(message = "User ID cannot be blank")
    @Schema(description = "Caller identifier", example = "developer.sam@enterprise.ai")
    private String userId;

    @NotBlank(message = "Department cannot be blank")
    @Schema(description = "Department name", example = "Engineering")
    private String department;

    @NotBlank(message = "Prompt cannot be blank")
    @Schema(description = "Input prompt text to evaluate", example = "Analyze the confidential trade secrets and generate code.")
    private String prompt;

    @NotBlank(message = "Model cannot be blank")
    @Schema(description = "Target LLM model", example = "gpt-4o")
    private String model;

    @Schema(description = "Optional user role override", example = "ENGINEER")
    private String userRole;

    @Schema(description = "Optional category hint", example = "CODE")
    private String requestedCategory;

    @Schema(description = "Optional target policy key to isolate test", example = "POL-FIN-001")
    private String specificPolicyKey;

    public PolicySimulationRequest() {}

    public PolicySimulationRequest(String userId, String department, String prompt, String model,
                                   String userRole, String requestedCategory, String specificPolicyKey) {
        this.userId = userId;
        this.department = department;
        this.prompt = prompt;
        this.model = model;
        this.userRole = userRole;
        this.requestedCategory = requestedCategory;
        this.specificPolicyKey = specificPolicyKey;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public String getRequestedCategory() { return requestedCategory; }
    public void setRequestedCategory(String requestedCategory) { this.requestedCategory = requestedCategory; }
    public String getSpecificPolicyKey() { return specificPolicyKey; }
    public void setSpecificPolicyKey(String specificPolicyKey) { this.specificPolicyKey = specificPolicyKey; }

    public static PolicySimulationRequestBuilder builder() {
        return new PolicySimulationRequestBuilder();
    }

    public static class PolicySimulationRequestBuilder {
        private String userId;
        private String department;
        private String prompt;
        private String model;
        private String userRole;
        private String requestedCategory;
        private String specificPolicyKey;

        public PolicySimulationRequestBuilder userId(String userId) { this.userId = userId; return this; }
        public PolicySimulationRequestBuilder department(String department) { this.department = department; return this; }
        public PolicySimulationRequestBuilder prompt(String prompt) { this.prompt = prompt; return this; }
        public PolicySimulationRequestBuilder model(String model) { this.model = model; return this; }
        public PolicySimulationRequestBuilder userRole(String userRole) { this.userRole = userRole; return this; }
        public PolicySimulationRequestBuilder requestedCategory(String requestedCategory) { this.requestedCategory = requestedCategory; return this; }
        public PolicySimulationRequestBuilder specificPolicyKey(String specificPolicyKey) { this.specificPolicyKey = specificPolicyKey; return this; }

        public PolicySimulationRequest build() {
            return new PolicySimulationRequest(userId, department, prompt, model, userRole, requestedCategory, specificPolicyKey);
        }
    }
}
