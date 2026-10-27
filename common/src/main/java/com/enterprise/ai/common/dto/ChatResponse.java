package com.enterprise.ai.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Aggregated response returned by Enterprise AI Gateway pipeline")
public class ChatResponse {

    @Schema(description = "Unique correlation request ID", example = "req-10293")
    private String requestId;

    @Schema(description = "Indicates whether the request was allowed to execute and return output", example = "true")
    private boolean allowed;

    @Schema(description = "Final policy action enforced (ALLOW, BLOCK, REVIEW)", example = "ALLOW")
    private String action;

    @Schema(description = "LLM completion answer (or null if blocked)")
    private String answer;

    @Schema(description = "Reason message if request was blocked", example = "Critical prompt injection detected")
    private String blockReason;

    @Schema(description = "Detailed telemetry from Prompt Security Scanner")
    private ScanResult scan;

    @Schema(description = "Detailed telemetry from Risk Scoring Engine")
    private RiskScore risk;

    @Schema(description = "Detailed telemetry from Response Quality Checker")
    private QualityResult quality;

    @Schema(description = "Token consumption and cost telemetry")
    private Usage usage;

    @Schema(description = "Indicates whether human-in-the-loop review was triggered", example = "false")
    private boolean approvalRequired;

    @Schema(description = "Hexagonal pipeline stage trace records")
    private List<PipelineStage> pipelineTrace;

    @Schema(description = "Indicates whether prompt was sanitized or redacted prior to LLM dispatch", example = "false")
    private boolean redacted;

    @Schema(description = "Actual prompt sent to upstream model after redactions")
    private String effectivePrompt;

    public ChatResponse() {
    }

    public ChatResponse(String requestId, boolean allowed, String action, String answer, String blockReason,
                        ScanResult scan, RiskScore risk, QualityResult quality, Usage usage,
                        boolean approvalRequired, List<PipelineStage> pipelineTrace,
                        boolean redacted, String effectivePrompt) {
        this.requestId = requestId;
        this.allowed = allowed;
        this.action = action;
        this.answer = answer;
        this.blockReason = blockReason;
        this.scan = scan;
        this.risk = risk;
        this.quality = quality;
        this.usage = usage;
        this.approvalRequired = approvalRequired;
        this.pipelineTrace = pipelineTrace;
        this.redacted = redacted;
        this.effectivePrompt = effectivePrompt;
    }

    public static ChatResponseBuilder builder() {
        return new ChatResponseBuilder();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }

    public ScanResult getScan() {
        return scan;
    }

    public void setScan(ScanResult scan) {
        this.scan = scan;
    }

    public RiskScore getRisk() {
        return risk;
    }

    public void setRisk(RiskScore risk) {
        this.risk = risk;
    }

    public QualityResult getQuality() {
        return quality;
    }

    public void setQuality(QualityResult quality) {
        this.quality = quality;
    }

    public Usage getUsage() {
        return usage;
    }

    public void setUsage(Usage usage) {
        this.usage = usage;
    }

    public boolean isApprovalRequired() {
        return approvalRequired;
    }

    public void setApprovalRequired(boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public List<PipelineStage> getPipelineTrace() {
        return pipelineTrace;
    }

    public void setPipelineTrace(List<PipelineStage> pipelineTrace) {
        this.pipelineTrace = pipelineTrace;
    }

    public boolean isRedacted() {
        return redacted;
    }

    public void setRedacted(boolean redacted) {
        this.redacted = redacted;
    }

    public String getEffectivePrompt() {
        return effectivePrompt;
    }

    public void setEffectivePrompt(String effectivePrompt) {
        this.effectivePrompt = effectivePrompt;
    }

    public String getRiskLevel() {
        return (risk != null && risk.getLevel() != null) ? risk.getLevel().name() : null;
    }

    public Integer getRiskScore() {
        return risk != null ? risk.getScore() : null;
    }

    public String getQualityVerdict() {
        return (quality != null && quality.getVerdict() != null) ? quality.getVerdict().name() : null;
    }

    public String getBlockedReason() {
        return blockReason;
    }

    public String getRedactedPrompt() {
        return effectivePrompt;
    }

    private String model;

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public static class ChatResponseBuilder {
        private String requestId;
        private boolean allowed;
        private String action;
        private String answer;
        private String blockReason;
        private ScanResult scan;
        private RiskScore risk;
        private QualityResult quality;
        private Usage usage;
        private boolean approvalRequired;
        private List<PipelineStage> pipelineTrace;
        private boolean redacted;
        private String effectivePrompt;
        private String model;

        public ChatResponseBuilder model(String model) {
            this.model = model;
            return this;
        }

        public ChatResponseBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public ChatResponseBuilder allowed(boolean allowed) {
            this.allowed = allowed;
            return this;
        }

        public ChatResponseBuilder action(String action) {
            this.action = action;
            return this;
        }

        public ChatResponseBuilder answer(String answer) {
            this.answer = answer;
            return this;
        }

        public ChatResponseBuilder blockReason(String blockReason) {
            this.blockReason = blockReason;
            return this;
        }

        public ChatResponseBuilder scan(ScanResult scan) {
            this.scan = scan;
            return this;
        }

        public ChatResponseBuilder risk(RiskScore risk) {
            this.risk = risk;
            return this;
        }

        public ChatResponseBuilder quality(QualityResult quality) {
            this.quality = quality;
            return this;
        }

        public ChatResponseBuilder usage(Usage usage) {
            this.usage = usage;
            return this;
        }

        public ChatResponseBuilder approvalRequired(boolean approvalRequired) {
            this.approvalRequired = approvalRequired;
            return this;
        }

        public ChatResponseBuilder pipelineTrace(List<PipelineStage> pipelineTrace) {
            this.pipelineTrace = pipelineTrace;
            return this;
        }

        public ChatResponseBuilder redacted(boolean redacted) {
            this.redacted = redacted;
            return this;
        }

        public ChatResponseBuilder effectivePrompt(String effectivePrompt) {
            this.effectivePrompt = effectivePrompt;
            return this;
        }

        public ChatResponse build() {
            ChatResponse cr = new ChatResponse(requestId, allowed, action, answer, blockReason,
                    scan, risk, quality, usage, approvalRequired, pipelineTrace, redacted, effectivePrompt);
            cr.setModel(model);
            return cr;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Token consumption metrics and financial calculation")
    public static class Usage {
        @Schema(description = "Tokens in prompt input", example = "140")
        private int tokensIn;

        @Schema(description = "Tokens in model completion", example = "350")
        private int tokensOut;

        @Schema(description = "Calculated cost in USD", example = "0.00350")
        private double costUsd;

        public Usage() {
        }

        public Usage(int tokensIn, int tokensOut, double costUsd) {
            this.tokensIn = tokensIn;
            this.tokensOut = tokensOut;
            this.costUsd = costUsd;
        }

        public static UsageBuilder builder() {
            return new UsageBuilder();
        }

        public int getTokensIn() {
            return tokensIn;
        }

        public void setTokensIn(int tokensIn) {
            this.tokensIn = tokensIn;
        }

        public int getTokensOut() {
            return tokensOut;
        }

        public void setTokensOut(int tokensOut) {
            this.tokensOut = tokensOut;
        }

        public double getCostUsd() {
            return costUsd;
        }

        public void setCostUsd(double costUsd) {
            this.costUsd = costUsd;
        }

        public static class UsageBuilder {
            private int tokensIn;
            private int tokensOut;
            private double costUsd;

            public UsageBuilder tokensIn(int tokensIn) {
                this.tokensIn = tokensIn;
                return this;
            }

            public UsageBuilder tokensOut(int tokensOut) {
                this.tokensOut = tokensOut;
                return this;
            }

            public UsageBuilder costUsd(double costUsd) {
                this.costUsd = costUsd;
                return this;
            }

            public Usage build() {
                return new Usage(tokensIn, tokensOut, costUsd);
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Execution telemetry for each hexagonal pipeline stage")
    public static class PipelineStage {
        @Schema(description = "Pipeline step name", example = "SCANNER")
        private String stageName;

        @Schema(description = "Target microservice invoked", example = "prompt-security-scanner")
        private String serviceName;

        @Schema(description = "Stage execution status (OK, BLOCKED, REVIEW, SKIPPED, FAILED)", example = "OK")
        private String status;

        @Schema(description = "Summary message or result overview", example = "Scan completed: 0 violations")
        private String summary;

        @Schema(description = "Payload dispatched to the stage")
        private Object dataSent;

        @Schema(description = "Payload received from the stage")
        private Object dataReceived;

        public PipelineStage() {
        }

        public PipelineStage(String stageName, String serviceName, String status,
                             String summary, Object dataSent, Object dataReceived) {
            this.stageName = stageName;
            this.serviceName = serviceName;
            this.status = status;
            this.summary = summary;
            this.dataSent = dataSent;
            this.dataReceived = dataReceived;
        }

        public static PipelineStageBuilder builder() {
            return new PipelineStageBuilder();
        }

        public String getStageName() {
            return stageName;
        }

        public void setStageName(String stageName) {
            this.stageName = stageName;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public Object getDataSent() {
            return dataSent;
        }

        public void setDataSent(Object dataSent) {
            this.dataSent = dataSent;
        }

        public Object getDataReceived() {
            return dataReceived;
        }

        public void setDataReceived(Object dataReceived) {
            this.dataReceived = dataReceived;
        }

        public static class PipelineStageBuilder {
            private String stageName;
            private String serviceName;
            private String status;
            private String summary;
            private Object dataSent;
            private Object dataReceived;

            public PipelineStageBuilder stageName(String stageName) {
                this.stageName = stageName;
                return this;
            }

            public PipelineStageBuilder serviceName(String serviceName) {
                this.serviceName = serviceName;
                return this;
            }

            public PipelineStageBuilder status(String status) {
                this.status = status;
                return this;
            }

            public PipelineStageBuilder summary(String summary) {
                this.summary = summary;
                return this;
            }

            public PipelineStageBuilder dataSent(Object dataSent) {
                this.dataSent = dataSent;
                return this;
            }

            public PipelineStageBuilder dataReceived(Object dataReceived) {
                this.dataReceived = dataReceived;
                return this;
            }

            public PipelineStage build() {
                return new PipelineStage(stageName, serviceName, status, summary, dataSent, dataReceived);
            }
        }
    }
}
