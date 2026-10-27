package com.enterprise.ai.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Aggregated operational metrics for executive governance dashboard")
public class DashboardOverview {

    @Schema(description = "Total number of prompts processed", example = "28420")
    private long totalRequests;

    @Schema(description = "Count of requests blocked by guardrails", example = "1420")
    private long blockedCount;

    @Schema(description = "Count of requests held in human review queue", example = "18")
    private long pendingApprovals;

    @Schema(description = "Cumulative model spend in USD", example = "312.45")
    private double totalCost;

    @Schema(description = "Request distribution breakdown grouped by RiskLevel (LOW, MEDIUM, HIGH, CRITICAL)")
    private Map<String, Long> eventsByRiskLevel;

    @Schema(description = "Enforcement action distribution breakdown (ALLOW, BLOCK, REVIEW)")
    private Map<String, Long> eventsByAction;

    public DashboardOverview() {
    }

    public DashboardOverview(long totalRequests, long blockedCount, long pendingApprovals,
                             double totalCost, Map<String, Long> eventsByRiskLevel,
                             Map<String, Long> eventsByAction) {
        this.totalRequests = totalRequests;
        this.blockedCount = blockedCount;
        this.pendingApprovals = pendingApprovals;
        this.totalCost = totalCost;
        this.eventsByRiskLevel = eventsByRiskLevel;
        this.eventsByAction = eventsByAction;
    }

    public static DashboardOverviewBuilder builder() {
        return new DashboardOverviewBuilder();
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getBlockedCount() {
        return blockedCount;
    }

    public void setBlockedCount(long blockedCount) {
        this.blockedCount = blockedCount;
    }

    public long getPendingApprovals() {
        return pendingApprovals;
    }

    public void setPendingApprovals(long pendingApprovals) {
        this.pendingApprovals = pendingApprovals;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public Map<String, Long> getEventsByRiskLevel() {
        return eventsByRiskLevel;
    }

    public void setEventsByRiskLevel(Map<String, Long> eventsByRiskLevel) {
        this.eventsByRiskLevel = eventsByRiskLevel;
    }

    public Map<String, Long> getEventsByAction() {
        return eventsByAction;
    }

    public void setEventsByAction(Map<String, Long> eventsByAction) {
        this.eventsByAction = eventsByAction;
    }

    public static class DashboardOverviewBuilder {
        private long totalRequests;
        private long blockedCount;
        private long pendingApprovals;
        private double totalCost;
        private Map<String, Long> eventsByRiskLevel;
        private Map<String, Long> eventsByAction;

        public DashboardOverviewBuilder totalRequests(long totalRequests) {
            this.totalRequests = totalRequests;
            return this;
        }

        public DashboardOverviewBuilder blockedCount(long blockedCount) {
            this.blockedCount = blockedCount;
            return this;
        }

        public DashboardOverviewBuilder pendingApprovals(long pendingApprovals) {
            this.pendingApprovals = pendingApprovals;
            return this;
        }

        public DashboardOverviewBuilder totalCost(double totalCost) {
            this.totalCost = totalCost;
            return this;
        }

        public DashboardOverviewBuilder eventsByRiskLevel(Map<String, Long> eventsByRiskLevel) {
            this.eventsByRiskLevel = eventsByRiskLevel;
            return this;
        }

        public DashboardOverviewBuilder eventsByAction(Map<String, Long> eventsByAction) {
            this.eventsByAction = eventsByAction;
            return this;
        }

        public DashboardOverview build() {
            return new DashboardOverview(totalRequests, blockedCount, pendingApprovals, totalCost, eventsByRiskLevel, eventsByAction);
        }
    }
}
