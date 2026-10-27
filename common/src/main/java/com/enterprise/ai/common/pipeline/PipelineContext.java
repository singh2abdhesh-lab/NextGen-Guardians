package com.enterprise.ai.common.pipeline;

import com.enterprise.ai.common.dto.QualityResult;
import com.enterprise.ai.common.dto.RiskScore;
import com.enterprise.ai.common.dto.ScanResult;
import com.enterprise.ai.common.enums.EnforcementAction;
import com.enterprise.ai.common.enums.RiskLevel;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Thread-safe reactive execution context carrying state across all pipeline stages")
public class PipelineContext {

    @Schema(description = "Unique correlation request ID", example = "req-98218-a")
    private String requestId;

    @Schema(description = "Caller identifier", example = "engineer.alex@enterprise.ai")
    private String userId;

    @Schema(description = "Caller security role", example = "ENGINEER")
    private String userRole;

    @Schema(description = "Raw prompt submitted by caller")
    private String rawPrompt;

    @Schema(description = "Sanitized or redacted prompt")
    private String sanitizedPrompt;

    @Schema(description = "Category of prompt", example = "CODE")
    private String promptCategory;

    @Schema(description = "Target LLM model name", example = "gpt-4o")
    private String model;

    @Schema(description = "Output from prompt security scanner")
    private ScanResult scanResult;

    @Schema(description = "Output from risk scoring engine")
    private RiskScore riskScore;

    @Schema(description = "Decided enforcement action")
    private EnforcementAction decisionAction;

    @Schema(description = "Evaluated risk level")
    private RiskLevel riskLevel;

    @Schema(description = "Generated text from upstream LLM")
    private String upstreamResponse;

    @Schema(description = "Output from response quality checker")
    private QualityResult qualityResult;

    @Schema(description = "Tokens consumed in prompt input")
    private int tokensIn;

    @Schema(description = "Tokens generated in completion output")
    private int tokensOut;

    @Schema(description = "Financial cost in USD", example = "0.00350")
    private double costUsd;

    @Schema(description = "Persisted audit event identifier")
    private Long auditEventId;

    @Schema(description = "Total execution processing latency in milliseconds", example = "85")
    private long totalDurationMs;

    @Schema(description = "Telemetry results for each pipeline stage")
    private List<StageResult> stageResults = new ArrayList<>();

    public PipelineContext() {}

    public PipelineContext(String requestId, String userId, String userRole, String rawPrompt,
                           String sanitizedPrompt, String promptCategory, String model,
                           ScanResult scanResult, RiskScore riskScore, EnforcementAction decisionAction,
                           RiskLevel riskLevel, String upstreamResponse, QualityResult qualityResult,
                           int tokensIn, int tokensOut, double costUsd, Long auditEventId,
                           long totalDurationMs, List<StageResult> stageResults) {
        this.requestId = requestId;
        this.userId = userId;
        this.userRole = userRole;
        this.rawPrompt = rawPrompt;
        this.sanitizedPrompt = sanitizedPrompt;
        this.promptCategory = promptCategory;
        this.model = model;
        this.scanResult = scanResult;
        this.riskScore = riskScore;
        this.decisionAction = decisionAction;
        this.riskLevel = riskLevel;
        this.upstreamResponse = upstreamResponse;
        this.qualityResult = qualityResult;
        this.tokensIn = tokensIn;
        this.tokensOut = tokensOut;
        this.costUsd = costUsd;
        this.auditEventId = auditEventId;
        this.totalDurationMs = totalDurationMs;
        this.stageResults = stageResults != null ? stageResults : new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String requestId;
        private String userId;
        private String userRole;
        private String rawPrompt;
        private String sanitizedPrompt;
        private String promptCategory;
        private String model;
        private ScanResult scanResult;
        private RiskScore riskScore;
        private EnforcementAction decisionAction;
        private RiskLevel riskLevel;
        private String upstreamResponse;
        private QualityResult qualityResult;
        private int tokensIn;
        private int tokensOut;
        private double costUsd;
        private Long auditEventId;
        private long totalDurationMs;
        private List<StageResult> stageResults = new ArrayList<>();

        public Builder requestId(String requestId) { this.requestId = requestId; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder userRole(String userRole) { this.userRole = userRole; return this; }
        public Builder rawPrompt(String rawPrompt) { this.rawPrompt = rawPrompt; return this; }
        public Builder sanitizedPrompt(String sanitizedPrompt) { this.sanitizedPrompt = sanitizedPrompt; return this; }
        public Builder promptCategory(String promptCategory) { this.promptCategory = promptCategory; return this; }
        public Builder model(String model) { this.model = model; return this; }
        public Builder scanResult(ScanResult scanResult) { this.scanResult = scanResult; return this; }
        public Builder riskScore(RiskScore riskScore) { this.riskScore = riskScore; return this; }
        public Builder decisionAction(EnforcementAction decisionAction) { this.decisionAction = decisionAction; return this; }
        public Builder riskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; return this; }
        public Builder upstreamResponse(String upstreamResponse) { this.upstreamResponse = upstreamResponse; return this; }
        public Builder qualityResult(QualityResult qualityResult) { this.qualityResult = qualityResult; return this; }
        public Builder tokensIn(int tokensIn) { this.tokensIn = tokensIn; return this; }
        public Builder tokensOut(int tokensOut) { this.tokensOut = tokensOut; return this; }
        public Builder costUsd(double costUsd) { this.costUsd = costUsd; return this; }
        public Builder auditEventId(Long auditEventId) { this.auditEventId = auditEventId; return this; }
        public Builder totalDurationMs(long totalDurationMs) { this.totalDurationMs = totalDurationMs; return this; }
        public Builder stageResults(List<StageResult> stageResults) {
            this.stageResults = stageResults != null ? stageResults : new ArrayList<>();
            return this;
        }

        public PipelineContext build() {
            return new PipelineContext(requestId, userId, userRole, rawPrompt, sanitizedPrompt,
                    promptCategory, model, scanResult, riskScore, decisionAction, riskLevel,
                    upstreamResponse, qualityResult, tokensIn, tokensOut, costUsd, auditEventId,
                    totalDurationMs, stageResults);
        }
    }

    public void addStageResult(StageResult result) {
        if (this.stageResults == null) {
            this.stageResults = new ArrayList<>();
        }
        this.stageResults.add(result);
    }

    public List<StageResult> getStageResults() {
        return stageResults != null ? Collections.unmodifiableList(stageResults) : Collections.emptyList();
    }

    public void setStageResults(List<StageResult> stageResults) {
        this.stageResults = stageResults != null ? stageResults : new ArrayList<>();
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public String getRawPrompt() { return rawPrompt; }
    public void setRawPrompt(String rawPrompt) { this.rawPrompt = rawPrompt; }

    public String getSanitizedPrompt() { return sanitizedPrompt; }
    public void setSanitizedPrompt(String sanitizedPrompt) { this.sanitizedPrompt = sanitizedPrompt; }

    public String getPromptCategory() { return promptCategory; }
    public void setPromptCategory(String promptCategory) { this.promptCategory = promptCategory; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public ScanResult getScanResult() { return scanResult; }
    public void setScanResult(ScanResult scanResult) { this.scanResult = scanResult; }

    public RiskScore getRiskScore() { return riskScore; }
    public void setRiskScore(RiskScore riskScore) { this.riskScore = riskScore; }

    public EnforcementAction getDecisionAction() { return decisionAction; }
    public void setDecisionAction(EnforcementAction decisionAction) { this.decisionAction = decisionAction; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getUpstreamResponse() { return upstreamResponse; }
    public void setUpstreamResponse(String upstreamResponse) { this.upstreamResponse = upstreamResponse; }

    public QualityResult getQualityResult() { return qualityResult; }
    public void setQualityResult(QualityResult qualityResult) { this.qualityResult = qualityResult; }

    public int getTokensIn() { return tokensIn; }
    public void setTokensIn(int tokensIn) { this.tokensIn = tokensIn; }

    public int getTokensOut() { return tokensOut; }
    public void setTokensOut(int tokensOut) { this.tokensOut = tokensOut; }

    public double getCostUsd() { return costUsd; }
    public void setCostUsd(double costUsd) { this.costUsd = costUsd; }

    public Long getAuditEventId() { return auditEventId; }
    public void setAuditEventId(Long auditEventId) { this.auditEventId = auditEventId; }

    public long getTotalDurationMs() { return totalDurationMs; }
    public void setTotalDurationMs(long totalDurationMs) { this.totalDurationMs = totalDurationMs; }
}
