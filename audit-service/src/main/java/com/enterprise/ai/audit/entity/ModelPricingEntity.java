package com.enterprise.ai.audit.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity defining per-1k input and output token pricing schedules
 * across AI models for spend tracking and financial auditing.
 */
@Entity
@Table(name = "model_pricing", indexes = {
        @Index(name = "idx_pricing_model", columnList = "model_name", unique = true)
})
public class ModelPricingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name", length = 100, nullable = false, unique = true)
    private String modelName;

    @Column(name = "cost_per_1k_input", nullable = false)
    private Double costPer1kInput;

    @Column(name = "cost_per_1k_output", nullable = false)
    private Double costPer1kOutput;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public ModelPricingEntity() {}

    public ModelPricingEntity(Long id, String modelName, Double costPer1kInput, Double costPer1kOutput,
                              Boolean active, LocalDateTime updatedAt) {
        this.id = id;
        this.modelName = modelName;
        this.costPer1kInput = costPer1kInput;
        this.costPer1kOutput = costPer1kOutput;
        this.active = active != null ? active : true;
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public Double getCostPer1kInput() { return costPer1kInput; }
    public void setCostPer1kInput(Double costPer1kInput) { this.costPer1kInput = costPer1kInput; }
    public Double getCostPer1kOutput() { return costPer1kOutput; }
    public void setCostPer1kOutput(Double costPer1kOutput) { this.costPer1kOutput = costPer1kOutput; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
