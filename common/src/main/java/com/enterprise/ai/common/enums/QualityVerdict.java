package com.enterprise.ai.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Verification verdict rendered by the Response Quality Checker")
public enum QualityVerdict {
    @Schema(description = "Model output passed all hallucination, toxicity, and safety criteria")
    PASS,

    @Schema(description = "Model output violated safety thresholds and must not be returned to caller")
    FAIL,

    @Schema(description = "Output contains marginal confidence or sensitive topics requiring supervisor review")
    PENDING_APPROVAL,

    @Schema(description = "Output flagged with warning metadata but returned to caller")
    FLAGGED
}
