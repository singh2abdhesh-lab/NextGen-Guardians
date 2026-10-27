package com.enterprise.ai.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Multi-factor risk assessment score and enforcement action")
public class RiskScore {

    @Schema(description = "Correlation tracking request ID", example = "req-10293")
    private String requestId;

    @Schema(description = "Aggregated composite risk score (0-100)", example = "65")
    private int totalScore;

    @Schema(description = "Categorized risk severity level")
    private RiskLevel riskLevel;

    @Schema(description = "Enforcement action decided by risk engine")
    private Action action;

    @Schema(description = "Detailed factor points contribution (dataSensitivity, userRole, promptCategory, modelUsed)")
    private Map<String, Integer> factorScores;

    public RiskScore() {
    }

    public RiskScore(String requestId, int totalScore, RiskLevel riskLevel, Action action, Map<String, Integer> factorScores) {
        this.requestId = requestId;
        this.totalScore = totalScore;
        this.riskLevel = riskLevel;
        this.action = action;
        this.factorScores = factorScores;
    }

    public static RiskScoreBuilder builder() {
        return new RiskScoreBuilder();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getScore() {
        return totalScore;
    }

    public int getCompositeScore() {
        return totalScore;
    }

    public RiskLevel getLevel() {
        return riskLevel;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Map<String, Integer> getFactorScores() {
        return factorScores;
    }

    public void setFactorScores(Map<String, Integer> factorScores) {
        this.factorScores = factorScores;
    }

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum Action {
        ALLOW, REVIEW, BLOCK
    }

    public static class RiskScoreBuilder {
        private String requestId;
        private int totalScore;
        private RiskLevel riskLevel;
        private Action action;
        private Map<String, Integer> factorScores;

        public RiskScoreBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public RiskScoreBuilder totalScore(int totalScore) {
            this.totalScore = totalScore;
            return this;
        }

        public RiskScoreBuilder compositeScore(int compositeScore) {
            this.totalScore = compositeScore;
            return this;
        }

        public RiskScoreBuilder score(int score) {
            this.totalScore = score;
            return this;
        }

        public RiskScoreBuilder riskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }

        public RiskScoreBuilder riskLevel(com.enterprise.ai.common.enums.RiskLevel riskLevelEnum) {
            if (riskLevelEnum != null) {
                try {
                    this.riskLevel = RiskLevel.valueOf(riskLevelEnum.name());
                } catch (IllegalArgumentException e) {
                    this.riskLevel = RiskLevel.LOW;
                }
            }
            return this;
        }

        public RiskScoreBuilder action(Action action) {
            this.action = action;
            return this;
        }

        public RiskScoreBuilder recommendedAction(com.enterprise.ai.common.enums.EnforcementAction enforcementAction) {
            if (enforcementAction != null) {
                try {
                    this.action = Action.valueOf(enforcementAction.name());
                } catch (IllegalArgumentException e) {
                    this.action = Action.ALLOW;
                }
            }
            return this;
        }

        public RiskScoreBuilder recommendedAction(Action action) {
            this.action = action;
            return this;
        }

        public RiskScoreBuilder factors(List<?> factors) {
            return this;
        }

        public RiskScoreBuilder factorScores(Map<String, Integer> factorScores) {
            this.factorScores = factorScores;
            return this;
        }

        public RiskScore build() {
            return new RiskScore(requestId, totalScore, riskLevel, action, factorScores);
        }
    }
}
