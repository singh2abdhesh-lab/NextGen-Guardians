package com.enterprise.ai.common.pipeline;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Execution outcome telemetry for an individual pipeline stage")
public class StageResult {

    @Schema(description = "Target pipeline stage")
    private PipelineStage stage;

    @Schema(description = "Status of execution (SUCCESS, SKIPPED, FAILED)")
    private String status;

    @Schema(description = "Duration of the stage in milliseconds", example = "42")
    private long durationMs;

    @Schema(description = "Informational or error message")
    private String message;

    public StageResult() {}

    public StageResult(PipelineStage stage, String status, long durationMs, String message) {
        this.stage = stage;
        this.status = status;
        this.durationMs = durationMs;
        this.message = message;
    }

    public static StageResult success(PipelineStage stage, long durationMs) {
        return new StageResult(stage, "SUCCESS", durationMs, null);
    }

    public static StageResult failure(PipelineStage stage, long durationMs, String message) {
        return new StageResult(stage, "FAILED", durationMs, message);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PipelineStage stage;
        private String status;
        private long durationMs;
        private String message;

        public Builder stage(PipelineStage stage) {
            this.stage = stage;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder durationMs(long durationMs) {
            this.durationMs = durationMs;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public StageResult build() {
            return new StageResult(stage, status, durationMs, message);
        }
    }

    public PipelineStage getStage() {
        return stage;
    }

    public void setStage(PipelineStage stage) {
        this.stage = stage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StageResult that = (StageResult) o;
        return durationMs == that.durationMs &&
                stage == that.stage &&
                Objects.equals(status, that.status) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stage, status, durationMs, message);
    }

    @Override
    public String toString() {
        return "StageResult{" +
                "stage=" + stage +
                ", status='" + status + '\'' +
                ", durationMs=" + durationMs +
                ", message='" + message + '\'' +
                '}';
    }
}
