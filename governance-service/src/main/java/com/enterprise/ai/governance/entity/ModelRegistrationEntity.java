package com.enterprise.ai.governance.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gov_model_registry", indexes = {
        @Index(name = "idx_mod_name", columnList = "model_name", unique = true),
        @Index(name = "idx_mod_vendor", columnList = "vendor"),
        @Index(name = "idx_mod_status", columnList = "status")
})
public class ModelRegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(nullable = false, length = 60)
    private String vendor;

    @Column(length = 50)
    private String version;

    @Column(length = 255)
    private String endpoint;

    @Column(name = "api_type", length = 50)
    private String apiType;

    @Column(name = "supported_features", length = 255)
    private String supportedFeatures;

    @Column(name = "cost_per_1k_input")
    private Double costPer1kInput;

    @Column(name = "cost_per_1k_output")
    private Double costPer1kOutput;

    @Column(name = "context_window")
    private Integer contextWindow;

    @Column(name = "streaming_support")
    private Boolean streamingSupport;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(length = 100)
    private String owner;

    @Column(name = "health_status", length = 30)
    private String healthStatus;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "last_health_check")
    private LocalDateTime lastHealthCheck;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ModelRegistrationEntity() {}

    public ModelRegistrationEntity(Long id, String modelName, String vendor, String version, String endpoint,
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

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (enabled == null) enabled = true;
        if (status == null) status = "APPROVED";
        if (healthStatus == null) healthStatus = "HEALTHY";
        if (streamingSupport == null) streamingSupport = true;
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = LocalDateTime.now();
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

    public static ModelRegistrationEntityBuilder builder() {
        return new ModelRegistrationEntityBuilder();
    }

    public static class ModelRegistrationEntityBuilder {
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

        public ModelRegistrationEntityBuilder id(Long id) { this.id = id; return this; }
        public ModelRegistrationEntityBuilder modelName(String modelName) { this.modelName = modelName; return this; }
        public ModelRegistrationEntityBuilder vendor(String vendor) { this.vendor = vendor; return this; }
        public ModelRegistrationEntityBuilder version(String version) { this.version = version; return this; }
        public ModelRegistrationEntityBuilder endpoint(String endpoint) { this.endpoint = endpoint; return this; }
        public ModelRegistrationEntityBuilder apiType(String apiType) { this.apiType = apiType; return this; }
        public ModelRegistrationEntityBuilder supportedFeatures(String supportedFeatures) { this.supportedFeatures = supportedFeatures; return this; }
        public ModelRegistrationEntityBuilder costPer1kInput(Double costPer1kInput) { this.costPer1kInput = costPer1kInput; return this; }
        public ModelRegistrationEntityBuilder costPer1kOutput(Double costPer1kOutput) { this.costPer1kOutput = costPer1kOutput; return this; }
        public ModelRegistrationEntityBuilder contextWindow(Integer contextWindow) { this.contextWindow = contextWindow; return this; }
        public ModelRegistrationEntityBuilder streamingSupport(Boolean streamingSupport) { this.streamingSupport = streamingSupport; return this; }
        public ModelRegistrationEntityBuilder status(String status) { this.status = status; return this; }
        public ModelRegistrationEntityBuilder owner(String owner) { this.owner = owner; return this; }
        public ModelRegistrationEntityBuilder healthStatus(String healthStatus) { this.healthStatus = healthStatus; return this; }
        public ModelRegistrationEntityBuilder enabled(Boolean enabled) { this.enabled = enabled; return this; }
        public ModelRegistrationEntityBuilder lastHealthCheck(LocalDateTime lastHealthCheck) { this.lastHealthCheck = lastHealthCheck; return this; }
        public ModelRegistrationEntityBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public ModelRegistrationEntityBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public ModelRegistrationEntity build() {
            return new ModelRegistrationEntity(id, modelName, vendor, version, endpoint, apiType, supportedFeatures,
                    costPer1kInput, costPer1kOutput, contextWindow, streamingSupport, status, owner, healthStatus,
                    enabled, lastHealthCheck, createdAt, updatedAt);
        }
    }
}
