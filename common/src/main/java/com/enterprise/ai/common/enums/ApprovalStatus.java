package com.enterprise.ai.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Human-in-the-loop review and authorization lifecycle state")
public enum ApprovalStatus {
    @Schema(description = "Awaiting manual security review")
    PENDING,

    @Schema(description = "Authorized by human security auditor")
    APPROVED,

    @Schema(description = "Rejected and terminated by human security auditor")
    REJECTED,

    @Schema(description = "Standard request, no manual approval required")
    NONE
}
