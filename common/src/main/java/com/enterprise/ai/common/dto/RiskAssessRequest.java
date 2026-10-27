package com.enterprise.ai.common.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request payload for multi-factor weighted risk assessment")
public class RiskAssessRequest {

    @Schema(description = "Correlation tracking request ID", example = "req-10293")
    private String requestId;

    @Schema(description = "Caller identifier", example = "analyst.mark@enterprise.ai")
    private String userId;

    @Schema(description = "Caller role defining risk factor", example = "ANALYST")
    private String userRole;

    @Schema(description = "Domain category of input prompt", example = "FINANCE")
    private String promptCategory;

    @JsonAlias({"targetModel", "modelName"})
    @Schema(description = "Target LLM model", example = "gpt-4o")
    private String model;

    @Min(value = 0, message = "Data sensitivity score cannot be negative")
    @Max(value = 100, message = "Data sensitivity score cannot exceed 100")
    @Schema(description = "Raw data sensitivity score derived from scanner violations", example = "45")
    private int dataSensitivity;

    @Schema(description = "List of threat categories detected in prompt")
    private List<String> detectedCategories;

    public RiskAssessRequest() {
    }

    public RiskAssessRequest(String requestId, String userId, String userRole, String promptCategory,
                             String model, int dataSensitivity, List<String> detectedCategories) {
        this.requestId = requestId;
        this.userId = userId;
        this.userRole = userRole;
        this.promptCategory = promptCategory;
        this.model = model;
        this.dataSensitivity = dataSensitivity;
        this.detectedCategories = detectedCategories;
    }

    public static RiskAssessRequestBuilder builder() {
        return new RiskAssessRequestBuilder();
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

    public String getPromptCategory() {
        return promptCategory;
    }

    public void setPromptCategory(String promptCategory) {
        this.promptCategory = promptCategory;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getDataSensitivity() {
        return dataSensitivity;
    }

    public void setDataSensitivity(int dataSensitivity) {
        this.dataSensitivity = dataSensitivity;
    }

    public List<String> getDetectedCategories() {
        return detectedCategories;
    }

    public void setDetectedCategories(List<String> detectedCategories) {
        this.detectedCategories = detectedCategories;
    }

    public static class RiskAssessRequestBuilder {
        private String requestId;
        private String userId;
        private String userRole;
        private String promptCategory;
        private String model;
        private int dataSensitivity;
        private List<String> detectedCategories;

        public RiskAssessRequestBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public RiskAssessRequestBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public RiskAssessRequestBuilder userRole(String userRole) {
            this.userRole = userRole;
            return this;
        }

        public RiskAssessRequestBuilder promptCategory(String promptCategory) {
            this.promptCategory = promptCategory;
            return this;
        }

        public RiskAssessRequestBuilder model(String model) {
            this.model = model;
            return this;
        }

        public RiskAssessRequestBuilder dataSensitivity(int dataSensitivity) {
            this.dataSensitivity = dataSensitivity;
            return this;
        }

        public RiskAssessRequestBuilder detectedCategories(List<String> detectedCategories) {
            this.detectedCategories = detectedCategories;
            return this;
        }

        public RiskAssessRequest build() {
            return new RiskAssessRequest(requestId, userId, userRole, promptCategory, model, dataSensitivity, detectedCategories);
        }
    }
}
