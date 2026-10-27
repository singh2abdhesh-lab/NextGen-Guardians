package com.enterprise.ai.common.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Security policy violation detected within input prompt or model response")
public class Violation {

    @Schema(description = "Violation identifier or rule name", example = "PROMPT_INJECTION")
    private String type;

    @Schema(description = "Violation severity tier (LOW, MEDIUM, HIGH, CRITICAL)")
    private Severity severity;

    @Schema(description = "Human-readable description of detected security condition", example = "Jailbreak override pattern identified")
    private String description;

    @JsonAlias({"snippet", "matchedText", "match"})
    @Schema(description = "Masked snippet of matching pattern (first 2 and last 2 characters retained)", example = "dr****le")
    private String matchedPattern;

    public Violation() {
    }

    public Violation(String type, Severity severity, String description, String matchedPattern) {
        this.type = type;
        this.severity = severity;
        this.description = description;
        this.matchedPattern = matchedPattern;
    }

    public static ViolationBuilder builder() {
        return new ViolationBuilder();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMatchedPattern() {
        return matchedPattern;
    }

    public void setMatchedPattern(String matchedPattern) {
        this.matchedPattern = matchedPattern;
    }

    @Schema(description = "Severity tier with associated score weight contribution")
    public enum Severity {
        LOW(5), MEDIUM(10), HIGH(25), CRITICAL(40);

        private final int weight;

        Severity(int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }
    }

    public static class ViolationBuilder {
        private String type;
        private Severity severity;
        private String description;
        private String matchedPattern;

        public ViolationBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ViolationBuilder type(com.enterprise.ai.common.enums.ViolationType violationType) {
            this.type = violationType != null ? violationType.name() : null;
            return this;
        }

        public ViolationBuilder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public ViolationBuilder severity(String severityStr) {
            if (severityStr != null) {
                try {
                    this.severity = Severity.valueOf(severityStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    this.severity = Severity.MEDIUM;
                }
            }
            return this;
        }

        public ViolationBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ViolationBuilder matchedPattern(String matchedPattern) {
            this.matchedPattern = matchedPattern;
            return this;
        }

        public Violation build() {
            return new Violation(type, severity, description, matchedPattern);
        }
    }
}
