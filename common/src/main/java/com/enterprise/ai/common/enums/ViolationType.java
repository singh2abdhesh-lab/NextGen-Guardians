package com.enterprise.ai.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Specific threat or compliance violation pattern identified by security detectors")
public enum ViolationType {
    @Schema(description = "Generic Personally Identifiable Information")
    PII("PII", 30),

    @Schema(description = "United States Social Security Number pattern")
    PII_SSN("PII", 40),

    @Schema(description = "Payment Card Number pattern (Visa, MasterCard, Amex)")
    PII_CREDIT_CARD("PII", 75),

    @Schema(description = "Corporate or personal email address")
    PII_EMAIL("PII", 10),

    @Schema(description = "Prompt injection, system prompt override, or jailbreak instruction")
    PROMPT_INJECTION("MALICIOUS_PROMPT", 75),

    @Schema(description = "High-entropy API secret key (AWS, OpenAI, GitHub, Private Key)")
    SECRET_LEAK("CONFIDENTIAL", 75),

    @Schema(description = "Proprietary source code or unapproved database query structure")
    SOURCE_CODE_LEAK("SOURCE_CODE", 25),

    @Schema(description = "Toxic, abusive, or harmful language vector in response")
    TOXIC_OUTPUT("QUALITY", 40),

    @Schema(description = "Model hallucination or factual inconsistency detected in output")
    HALLUCINATION("QUALITY", 30);

    private final String category;
    private final int defaultWeight;

    ViolationType(String category, int defaultWeight) {
        this.category = category;
        this.defaultWeight = defaultWeight;
    }

    public String getCategory() {
        return category;
    }

    public int getDefaultWeight() {
        return defaultWeight;
    }
}
