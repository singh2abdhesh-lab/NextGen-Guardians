package com.enterprise.ai.common.dto;

import com.enterprise.ai.common.validation.ValidationError;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized RFC 7807 compatible error and problem details response")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "HTTP error title or reason phrase", example = "Bad Request")
    private String error;

    @Schema(description = "Detailed human-readable error description", example = "Input prompt contains blocked threat vectors")
    private String message;

    @Schema(description = "Internal machine-readable error code", example = "SECURITY_VIOLATION_DETECTED")
    private String errorCode;

    @Schema(description = "URI path of the originating request", example = "/api/v1/ai/chat")
    private String path;

    @Schema(description = "Correlation request identifier", example = "req-9812-c")
    private String requestId;

    @Schema(description = "Timestamp when error occurred")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "List of field-level constraint validation failures")
    private List<ValidationError> validationErrors;

    public ErrorResponse() {}

    public ErrorResponse(int status, String error, String message, String errorCode, String path,
                         String requestId, LocalDateTime timestamp, List<ValidationError> validationErrors) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.errorCode = errorCode;
        this.path = path;
        this.requestId = requestId;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.validationErrors = validationErrors;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int status;
        private String error;
        private String message;
        private String errorCode;
        private String path;
        private String requestId;
        private LocalDateTime timestamp = LocalDateTime.now();
        private List<ValidationError> validationErrors;

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder validationErrors(List<ValidationError> validationErrors) {
            this.validationErrors = validationErrors;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(status, error, message, errorCode, path, requestId, timestamp, validationErrors);
        }
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<ValidationError> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(List<ValidationError> validationErrors) {
        this.validationErrors = validationErrors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorResponse that = (ErrorResponse) o;
        return status == that.status &&
                Objects.equals(error, that.error) &&
                Objects.equals(message, that.message) &&
                Objects.equals(errorCode, that.errorCode) &&
                Objects.equals(path, that.path) &&
                Objects.equals(requestId, that.requestId) &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(validationErrors, that.validationErrors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, error, message, errorCode, path, requestId, timestamp, validationErrors);
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "status=" + status +
                ", error='" + error + '\'' +
                ", message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", path='" + path + '\'' +
                ", requestId='" + requestId + '\'' +
                ", timestamp=" + timestamp +
                ", validationErrors=" + validationErrors +
                '}';
    }
}
