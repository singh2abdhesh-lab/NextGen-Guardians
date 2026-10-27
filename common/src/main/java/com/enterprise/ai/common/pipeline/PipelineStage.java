package com.enterprise.ai.common.pipeline;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Stages of the reactive AI Guardian processing pipeline")
public enum PipelineStage {
    @Schema(description = "Gateway ingress reception and initial token validation")
    INGRESS,

    @Schema(description = "Threat, PII, and prompt injection scanning")
    SECURITY_SCAN,

    @Schema(description = "Multi-factor weighted risk scoring and enforcement determination")
    RISK_ASSESSMENT,

    @Schema(description = "Upstream LLM invocation and raw token stream generation")
    LLM_INVOCATION,

    @Schema(description = "Model output hallucination, toxicity, and safety verification")
    QUALITY_EVALUATION,

    @Schema(description = "Immutable audit log persistence and event streaming")
    AUDIT_PERSISTENCE
}
