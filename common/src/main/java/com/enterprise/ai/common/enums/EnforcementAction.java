package com.enterprise.ai.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Enforcement action assigned to an evaluated AI request")
public enum EnforcementAction {
    @Schema(description = "Request is verified compliant and permitted to execute upstream")
    ALLOW,

    @Schema(description = "Request terminated at the gateway boundary due to critical violations")
    BLOCK,

    @Schema(description = "Request routed to the human-in-the-loop approval queue")
    REVIEW,

    @Schema(description = "Sensitive PII or confidential segments redacted prior to upstream execution")
    REDACT
}
