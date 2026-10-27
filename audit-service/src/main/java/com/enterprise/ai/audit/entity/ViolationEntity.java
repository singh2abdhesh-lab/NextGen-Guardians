package com.enterprise.ai.audit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing an individual threat, PII, secret, or injection violation
 * mapped to a parent AuditEventEntity.
 */
@Entity
@Table(name = "audit_violations", indexes = {
        @Index(name = "idx_viol_audit_event", columnList = "audit_event_id"),
        @Index(name = "idx_viol_type", columnList = "violation_type"),
        @Index(name = "idx_viol_severity", columnList = "severity")
})
public class ViolationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_event_id", nullable = false)
    private AuditEventEntity auditEvent;

    @Column(name = "violation_type", length = 50, nullable = false)
    private String violationType;

    @Column(name = "severity", length = 20, nullable = false)
    private String severity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "matched_text", columnDefinition = "TEXT")
    private String matchedText;

    @Column(name = "start_index")
    private Integer startIndex;

    @Column(name = "end_index")
    private Integer endIndex;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public ViolationEntity() {}

    public ViolationEntity(Long id, AuditEventEntity auditEvent, String violationType, String severity,
                           String description, String matchedText, Integer startIndex, Integer endIndex,
                           LocalDateTime createdAt) {
        this.id = id;
        this.auditEvent = auditEvent;
        this.violationType = violationType;
        this.severity = severity;
        this.description = description;
        this.matchedText = matchedText;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AuditEventEntity getAuditEvent() { return auditEvent; }
    public void setAuditEvent(AuditEventEntity auditEvent) { this.auditEvent = auditEvent; }
    public String getViolationType() { return violationType; }
    public void setViolationType(String violationType) { this.violationType = violationType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMatchedText() { return matchedText; }
    public void setMatchedText(String matchedText) { this.matchedText = matchedText; }
    public Integer getStartIndex() { return startIndex; }
    public void setStartIndex(Integer startIndex) { this.startIndex = startIndex; }
    public Integer getEndIndex() { return endIndex; }
    public void setEndIndex(Integer endIndex) { this.endIndex = endIndex; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static ViolationEntityBuilder builder() {
        return new ViolationEntityBuilder();
    }

    public static class ViolationEntityBuilder {
        private Long id;
        private AuditEventEntity auditEvent;
        private String violationType;
        private String severity;
        private String description;
        private String matchedText;
        private Integer startIndex;
        private Integer endIndex;
        private LocalDateTime createdAt = LocalDateTime.now();

        public ViolationEntityBuilder id(Long id) { this.id = id; return this; }
        public ViolationEntityBuilder auditEvent(AuditEventEntity auditEvent) { this.auditEvent = auditEvent; return this; }
        public ViolationEntityBuilder violationType(String violationType) { this.violationType = violationType; return this; }
        public ViolationEntityBuilder severity(String severity) { this.severity = severity; return this; }
        public ViolationEntityBuilder description(String description) { this.description = description; return this; }
        public ViolationEntityBuilder matchedText(String matchedText) { this.matchedText = matchedText; return this; }
        public ViolationEntityBuilder startIndex(Integer startIndex) { this.startIndex = startIndex; return this; }
        public ViolationEntityBuilder endIndex(Integer endIndex) { this.endIndex = endIndex; return this; }
        public ViolationEntityBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ViolationEntity build() {
            return new ViolationEntity(id, auditEvent, violationType, severity, description, matchedText, startIndex, endIndex, createdAt);
        }
    }
}
