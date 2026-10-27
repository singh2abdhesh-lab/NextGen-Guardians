package com.enterprise.ai.common.dto;

import com.enterprise.ai.common.enums.ApprovalStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Payload submitted by human operator to resolve a pending review item")
public class ApprovalRequest {

    @NotNull(message = "Event ID cannot be null")
    @Schema(description = "Audit event record ID being authorized or rejected", example = "1092")
    private Long eventId;

    @NotNull(message = "Approval status decision cannot be null")
    @Schema(description = "Decision verdict: APPROVED or REJECTED", example = "APPROVED")
    private ApprovalStatus decision;

    @NotBlank(message = "Approver identity cannot be blank")
    @Schema(description = "Email or username of authorizing security auditor", example = "secops.lead@enterprise.ai")
    private String approver;

    @Schema(description = "Optional justification or audit commentary", example = "Prompt reviewed and authorized under quarterly compliance audit exception")
    private String reason;

    public ApprovalRequest() {}

    public ApprovalRequest(Long eventId, ApprovalStatus decision, String approver, String reason) {
        this.eventId = eventId;
        this.decision = decision;
        this.approver = approver;
        this.reason = reason;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long eventId;
        private ApprovalStatus decision;
        private String approver;
        private String reason;

        public Builder eventId(Long eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder decision(ApprovalStatus decision) {
            this.decision = decision;
            return this;
        }

        public Builder approver(String approver) {
            this.approver = approver;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public ApprovalRequest build() {
            return new ApprovalRequest(eventId, decision, approver, reason);
        }
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public ApprovalStatus getDecision() {
        return decision;
    }

    public void setDecision(ApprovalStatus decision) {
        this.decision = decision;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApprovalRequest that = (ApprovalRequest) o;
        return Objects.equals(eventId, that.eventId) &&
                decision == that.decision &&
                Objects.equals(approver, that.approver) &&
                Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, decision, approver, reason);
    }

    @Override
    public String toString() {
        return "ApprovalRequest{" +
                "eventId=" + eventId +
                ", decision=" + decision +
                ", approver='" + approver + '\'' +
                ", reason='" + reason + '\'' +
                '}';
    }
}
