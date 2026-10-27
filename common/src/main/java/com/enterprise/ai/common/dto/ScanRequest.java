package com.enterprise.ai.common.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Payload submitted to Prompt Security Scanner")
public class ScanRequest {

    @Schema(description = "Unique correlation request ID", example = "req-10293")
    private String requestId;

    @NotBlank(message = "Prompt payload cannot be blank")
    @Size(max = 32000, message = "Prompt exceeds maximum scanning threshold")
    @JsonAlias({"promptText", "text", "query"})
    @Schema(description = "Prompt content to scan for threats and sensitive patterns", requiredMode = Schema.RequiredMode.REQUIRED)
    private String prompt;

    public ScanRequest() {
    }

    public ScanRequest(String requestId, String prompt) {
        this.requestId = requestId;
        this.prompt = prompt;
    }

    public static ScanRequestBuilder builder() {
        return new ScanRequestBuilder();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public static class ScanRequestBuilder {
        private String requestId;
        private String prompt;

        public ScanRequestBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public ScanRequestBuilder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public ScanRequest build() {
            return new ScanRequest(requestId, prompt);
        }
    }
}
