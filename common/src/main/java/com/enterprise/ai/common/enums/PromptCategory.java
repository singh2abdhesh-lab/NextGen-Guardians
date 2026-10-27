package com.enterprise.ai.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Domain classification of the input prompt")
public enum PromptCategory {
    @Schema(description = "General enterprise inquiries and text generation")
    GENERAL(20),

    @Schema(description = "Programming, debugging, and code generation")
    CODE(65),

    @Schema(description = "Accounting, financial models, and revenue projections")
    FINANCE(75),

    @Schema(description = "Human resources, employee compensation, and personnel inquiries")
    HR(70),

    @Schema(description = "Contracts, compliance, and legal discovery")
    LEGAL(60),

    @Schema(description = "Medical or health-related data processing")
    HEALTHCARE(80),

    @Schema(description = "Public relations and marketing copy")
    MARKETING(30);

    private final int baselineRiskScore;

    PromptCategory(int baselineRiskScore) {
        this.baselineRiskScore = baselineRiskScore;
    }

    public int getBaselineRiskScore() {
        return baselineRiskScore;
    }

    public static PromptCategory fromString(String category) {
        if (category == null) return GENERAL;
        try {
            return PromptCategory.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return GENERAL;
        }
    }
}
