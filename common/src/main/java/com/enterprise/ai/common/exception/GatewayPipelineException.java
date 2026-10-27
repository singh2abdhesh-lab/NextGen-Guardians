package com.enterprise.ai.common.exception;

/**
 * Thrown when an error occurs during reactive orchestration across the security pipeline.
 */
public class GatewayPipelineException extends AiGuardianException {

    private final String stage;

    public GatewayPipelineException(String message, String stage) {
        super(message, "GATEWAY_PIPELINE_ERROR", 500);
        this.stage = stage;
    }

    public GatewayPipelineException(String message, Throwable cause, String stage) {
        super(message, cause, "GATEWAY_PIPELINE_ERROR", 500);
        this.stage = stage;
    }

    public String getStage() {
        return stage;
    }
}
