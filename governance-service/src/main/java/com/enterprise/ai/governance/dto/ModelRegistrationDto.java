package com.enterprise.ai.governance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Schema(description = "Registered Enterprise AI Model Metadata")
public class ModelRegistrationDto {

    private Long id;

    @NotBlank(message = "Model name cannot be blank")
    @Schema(description = "Canonical model identifier", example = "gpt-4o")
    private String modelName;

    @NotBlank(message = "Vendor cannot be blank")
    @Schema(description = "Model provider/vendor", example = "OpenAI")
    private String vendor;

    @Schema(description = "Specific provider model version", example = "2024-08-06")
    private String version;

    @Schema(description = "Provider API endpoint URL", example = "https://api.openai.com/v1/chat/completions")
    private String endpoint;

    @Schema(description = "Protocol standard", example = "OPENAI_COMPATIBLE")
    private String apiType;

    @Schema(description = "Supported capabilities", example = "STREAMING,FUNCTION_CALLING,VISION,JSON_MODE")
    private String supportedFeatures;

    @Schema(description = "Cost per 1,000 input tokens in USD", example = "0.0025")
    private Double costPer1kInput;

    @Schema(description = "Cost per 1,000 output tokens in USD", example = "0.0100")
    private Double costPer1kOutput;

    @Schema(description = "Total context token window", example = "128000")
    private Integer contextWindow;

    private Boolean streamingSupport;

    @Schema(description = "Status: APPROVED, EVALUATION, RESTRICTED, DEPRECATED, BLOCKED", example = "APPROVED")
    private String status;

    private String owner;

    @Schema(description = "Live health: HEALTHY, DEGRADED, UNREACHABLE", example = "HEALTHY")
    private String healthStatus;

    private Boolean enabled;
    private LocalDateTime lastHealthCheck;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ModelRegistrationDto() {}

    public ModelRegistrationDto(Long id, String modelName, String vendor, String version, String endpoint,
                                String apiType, String supportedFeatures, Double costPer1kInput,
                                Double costPer1kOutput, Integer contextWindow, Boolean streamingSupport,
                                String status, String owner, String healthStatus, Boolean enabled,
                                LocalDateTime lastHealthCheck, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.modelName = modelName;
        this.vendor = vendor;
        this.version = version;
        this.endpoint = endpoint;
        this.apiType = apiType;
        this.supportedFeatures = supportedFeatures;
        this.costPer1kInput = costPer1kInput;
        this.costPer1kOutput = costPer1kOutput;
        this.contextWindow = contextWindow;
        this.streamingSupport = streamingSupport;
        this.status = status;
        this.owner = owner;
        this.healthStatus = healthStatus;
        this.enabled = enabled;
        this.lastHealthCheck = lastHealthCheck;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getApiType() { return apiType; }
    public void setApiType(String apiType) { this.apiType = apiType; }
    public String getSupportedFeatures() { return supportedFeatures; }
    public void setSupportedFeatures(String supportedFeatures) { this.supportedFeatures = supportedFeatures; }
    public Double getCostPer1kInput() { return costPer1kInput; }
    public void setCostPer1kInput(Double costPer1kInput) { this.costPer1kInput = costPer1kInput; }
    public Double getCostPer1kOutput() { return costPer1kOutput; }
    public void setCostPer1kOutput(Double costPer1kOutput) { this.costPer1kOutput = costPer1kOutput; }
    public Integer getContextWindow() { return contextWindow; }
    public void setContextWindow(Integer contextWindow) { this.contextWindow = contextWindow; }
    public Boolean getStreamingSupport() { return streamingSupport; }
    public void setStreamingSupport(Boolean streamingSupport) { this.streamingSupport = streamingSupport; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getLastHealthCheck() { return lastHealthCheck; }
    public void setLastHealthCheck(LocalDateTime lastHealthCheck) { this.lastHealthCheck = lastHealthCheck; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static ModelRegistrationDtoBuilder builder() {
        return new ModelRegistrationDtoBuilder();
    }

    public static class ModelRegistrationDtoBuilder {
        private Long id;
        private String modelName;
        private String vendor;
        private String version;
        private String endpoint;
        private String apiType;
        private String supportedFeatures;
        private Double costPer1kInput;
        private Double costPer1kOutput;
        private Integer contextWindow;
        private Boolean streamingSupport;
        private String status;
        private String owner;
        private String healthStatus;
        private Boolean enabled;
        private LocalDateTime lastHealthCheck;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public ModelRegistrationDtoBuilder id(Long id) { this.id = id; return this; }
        public ModelRegistrationDtoBuilder modelName(String modelName) { this.modelName = modelName; return this; }
        public ModelRegistrationDtoBuilder vendor(String vendor) { this.vendor = vendor; return this; }
        public ModelRegistrationDtoBuilder version(String version) { this.version = version; return this; }
        public ModelRegistrationDtoBuilder endpoint(String endpoint) { this.endpoint = endpoint; return this; }
        public ModelRegistrationDtoBuilder apiType(String apiType) { this.apiType = apiType; return this; }
        public ModelRegistrationDtoBuilder supportedFeatures(String supportedFeatures) { this.supportedFeatures = supportedFeatures; return this; }
        public ModelRegistrationDtoBuilder costPer1kInput(Double costPer1kInput) { this.costPer1kInput = costPer1kInput; return this; }
        public ModelRegistrationDtoBuilder costPer1kOutput(Double costPer1kOutput) { this.costPer1kOutput = costPer1kOutput; return this; }
        public ModelRegistrationDtoBuilder contextWindow(Integer contextWindow) { this.contextWindow = contextWindow; return this; }
        public ModelRegistrationDtoBuilder streamingSupport(Boolean streamingSupport) { this.streamingSupport = streamingSupport; return this; }
        public ModelRegistrationDtoBuilder status(String status) { this.status = status; return this; }
        public ModelRegistrationDtoBuilder owner(String owner) { this.owner = owner; return this; }
        public ModelRegistrationDtoBuilder healthStatus(String healthStatus) { this.healthStatus = healthStatus; return this; }
        public ModelRegistrationDtoBuilder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        public ModelRegistrationDtoBuilder lastHealthCheck(LocalDateTime lastHealthCheck) { this.lastHealthCheck = lastHealthCheck; return this; }
        public ModelRegistrationDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ModelRegistrationDtoBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public ModelRegistrationDto build() {
            return new ModelRegistrationDto(id, modelName, vendor, version, endpoint, apiType, supportedFeatures,
                    costPer1kInput, costPer1kOutput, contextWindow, streamingSupport, status, owner, healthStatus,
                    enabled, lastHealthCheck, createdAt, updatedAt);
        }
    }
}
