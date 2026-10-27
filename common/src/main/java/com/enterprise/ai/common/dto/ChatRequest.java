package com.enterprise.ai.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Ingress chat payload submitted to Enterprise AI Gateway")
public class ChatRequest {

    @Schema(description = "Unique correlation request ID", example = "req-10293-alpha")
    private String requestId;

    @Schema(description = "Enterprise user identifier or email", example = "dev.marcus@enterprise.ai")
    private String userId;

    @Schema(description = "Caller role defining authorization matrix", example = "ENGINEER")
    private String userRole;

    @Schema(description = "Organizational division or department", example = "Platform Engineering")
    private String department;

    @Schema(description = "Target LLM model", example = "gpt-4o")
    private String model;

    @Schema(description = "Prompt domain category", example = "CODE")
    private String promptCategory;

    @NotBlank(message = "Prompt content cannot be blank")
    @Size(max = 32000, message = "Prompt content exceeds maximum permitted token window (32,000 chars)")
    @Schema(description = "Raw prompt submitted by caller", requiredMode = Schema.RequiredMode.REQUIRED, example = "Refactor this Spring Boot reactive pipeline")
    private String prompt;

    @JsonProperty("autoRedact")
    @Schema(description = "Whether to auto-redact detected PII before sending upstream", example = "true")
    private boolean autoRedact = false;

    public ChatRequest() {
    }

    public ChatRequest(String requestId, String userId, String userRole, String department,
                       String model, String promptCategory, String prompt, boolean autoRedact) {
        this.requestId = requestId;
        this.userId = userId;
        this.userRole = userRole;
        this.department = department;
        this.model = model;
        this.promptCategory = promptCategory;
        this.prompt = prompt;
        this.autoRedact = autoRedact;
    }

    public static ChatRequestBuilder builder() {
        return new ChatRequestBuilder();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPromptCategory() {
        return promptCategory;
    }

    public void setPromptCategory(String promptCategory) {
        this.promptCategory = promptCategory;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public boolean isAutoRedact() {
        return autoRedact;
    }

    public void setAutoRedact(boolean autoRedact) {
        this.autoRedact = autoRedact;
    }

    public static class ChatRequestBuilder {
        private String requestId;
        private String userId;
        private String userRole;
        private String department;
        private String model;
        private String promptCategory;
        private String prompt;
        private boolean autoRedact = false;

        public ChatRequestBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public ChatRequestBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public ChatRequestBuilder userRole(String userRole) {
            this.userRole = userRole;
            return this;
        }

        public ChatRequestBuilder department(String department) {
            this.department = department;
            return this;
        }

        public ChatRequestBuilder model(String model) {
            this.model = model;
            return this;
        }

        public ChatRequestBuilder promptCategory(String promptCategory) {
            this.promptCategory = promptCategory;
            return this;
        }

        public ChatRequestBuilder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public ChatRequestBuilder autoRedact(boolean autoRedact) {
            this.autoRedact = autoRedact;
            return this;
        }

        public ChatRequest build() {
            return new ChatRequest(requestId, userId, userRole, department, model, promptCategory, prompt, autoRedact);
        }
    }
}
