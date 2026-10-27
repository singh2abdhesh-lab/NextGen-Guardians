package com.enterprise.ai.audit.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA entity capturing daily or periodic regulatory framework compliance posture,
 * violation rates, and governance health indicators.
 */
@Entity
@Table(name = "compliance_snapshots", indexes = {
        @Index(name = "idx_comp_date", columnList = "snapshot_date"),
        @Index(name = "idx_comp_framework", columnList = "framework"),
        @Index(name = "idx_comp_status", columnList = "status")
})
public class ComplianceSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "framework", length = 100, nullable = false)
    private String framework;

    @Column(name = "total_evaluated", nullable = false)
    private Long totalEvaluated;

    @Column(name = "violations_count", nullable = false)
    private Long violationsCount;

    @Column(name = "compliance_score", nullable = false)
    private Double complianceScore;

    @Column(name = "status", length = 50, nullable = false)
    private String status; // COMPLIANT, WARNING, NON_COMPLIANT

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public ComplianceSnapshotEntity() {}

    public ComplianceSnapshotEntity(Long id, LocalDate snapshotDate, String framework, Long totalEvaluated,
                                    Long violationsCount, Double complianceScore, String status, LocalDateTime createdAt) {
        this.id = id;
        this.snapshotDate = snapshotDate;
        this.framework = framework;
        this.totalEvaluated = totalEvaluated;
        this.violationsCount = violationsCount;
        this.complianceScore = complianceScore;
        this.status = status;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getSnapshotDate() { return snapshotDate; }
    public void setSnapshotDate(LocalDate snapshotDate) { this.snapshotDate = snapshotDate; }
    public String getFramework() { return framework; }
    public void setFramework(String framework) { this.framework = framework; }
    public Long getTotalEvaluated() { return totalEvaluated; }
    public void setTotalEvaluated(Long totalEvaluated) { this.totalEvaluated = totalEvaluated; }
    public Long getViolationsCount() { return violationsCount; }
    public void setViolationsCount(Long violationsCount) { this.violationsCount = violationsCount; }
    public Double getComplianceScore() { return complianceScore; }
    public void setComplianceScore(Double complianceScore) { this.complianceScore = complianceScore; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
