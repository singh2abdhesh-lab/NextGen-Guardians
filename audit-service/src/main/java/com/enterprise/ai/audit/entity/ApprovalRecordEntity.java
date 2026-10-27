package com.enterprise.ai.audit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity representing human-in-the-loop review authorization, rejection,
 * and escalation decisions linked directly to an AuditEventEntity.
 */
@Entity
@Table(name = "approval_records", indexes = {
        @Index(name = "idx_approval_event", columnList = "audit_event_id"),
        @Index(name = "idx_approval_decision", columnList = "decision"),
        @Index(name = "idx_approval_approver", columnList = "approver"),
        @Index(name = "idx_approval_reviewed", columnList = "reviewed_at")
})
public class ApprovalRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_event_id", unique = true, nullable = false)
    private AuditEventEntity auditEvent;

    @Column(name = "decision", length = 20, nullable = false)
    private String decision; // PENDING, APPROVED, REJECTED

    @Column(name = "approver", length = 255, nullable = false)
    private String approver;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt = LocalDateTime.now();

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public ApprovalRecordEntity() {}

    public ApprovalRecordEntity(Long id, AuditEventEntity auditEvent, String decision, String approver,
                                String reason, LocalDateTime reviewedAt, String notes) {
        this.id = id;
        this.auditEvent = auditEvent;
        this.decision = decision;
        this.approver = approver;
        this.reason = reason;
        this.reviewedAt = reviewedAt != null ? reviewedAt : LocalDateTime.now();
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AuditEventEntity getAuditEvent() { return auditEvent; }
    public void setAuditEvent(AuditEventEntity auditEvent) { this.auditEvent = auditEvent; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getApprover() { return approver; }
    public void setApprover(String approver) { this.approver = approver; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public static ApprovalRecordEntityBuilder builder() {
        return new ApprovalRecordEntityBuilder();
    }

    public static class ApprovalRecordEntityBuilder {
        private Long id;
        private AuditEventEntity auditEvent;
        private String decision;
        private String approver;
        private String reason;
        private LocalDateTime reviewedAt = LocalDateTime.now();
        private String notes;

        public ApprovalRecordEntityBuilder id(Long id) { this.id = id; return this; }
        public ApprovalRecordEntityBuilder auditEvent(AuditEventEntity auditEvent) { this.auditEvent = auditEvent; return this; }
        public ApprovalRecordEntityBuilder decision(String decision) { this.decision = decision; return this; }
        public ApprovalRecordEntityBuilder approver(String approver) { this.approver = approver; return this; }
        public ApprovalRecordEntityBuilder reason(String reason) { this.reason = reason; return this; }
        public ApprovalRecordEntityBuilder reviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; return this; }
        public ApprovalRecordEntityBuilder notes(String notes) { this.notes = notes; return this; }

        public ApprovalRecordEntity build() {
            return new ApprovalRecordEntity(id, auditEvent, decision, approver, reason, reviewedAt, notes);
        }
    }
}
