package com.enterprise.ai.common.validation;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Details regarding an individual constraint or field validation failure")
public class ValidationError {

    @Schema(description = "Target property path or field name", example = "prompt")
    private String field;

    @Schema(description = "Rejected or invalid input value", example = "")
    private Object rejectedValue;

    @Schema(description = "Validation constraint violation message", example = "Prompt payload cannot be empty")
    private String message;

    public ValidationError() {}

    public ValidationError(String field, Object rejectedValue, String message) {
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.message = message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String field;
        private Object rejectedValue;
        private String message;

        public Builder field(String field) {
            this.field = field;
            return this;
        }

        public Builder rejectedValue(Object rejectedValue) {
            this.rejectedValue = rejectedValue;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public ValidationError build() {
            return new ValidationError(field, rejectedValue, message);
        }
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }

    public void setRejectedValue(Object rejectedValue) {
        this.rejectedValue = rejectedValue;
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
        ValidationError that = (ValidationError) o;
        return Objects.equals(field, that.field) &&
                Objects.equals(rejectedValue, that.rejectedValue) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, rejectedValue, message);
    }

    @Override
    public String toString() {
        return "ValidationError{" +
                "field='" + field + '\'' +
                ", rejectedValue=" + rejectedValue +
                ", message='" + message + '\'' +
                '}';
    }
}
