package com.enterprise.ai.common.validation;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Aggregated validation outcome containing all detected constraint failures")
public class ValidationResult {

    @Schema(description = "Indicates whether the payload satisfied all constraints", example = "true")
    private boolean valid;

    @Schema(description = "List of individual validation errors")
    private List<ValidationError> errors = new ArrayList<>();

    public ValidationResult() {}

    public ValidationResult(boolean valid, List<ValidationError> errors) {
        this.valid = valid;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public static ValidationResult success() {
        return new ValidationResult(true, new ArrayList<>());
    }

    public static ValidationResult failure(List<ValidationError> errors) {
        return new ValidationResult(false, errors != null ? errors : new ArrayList<>());
    }

    public void addError(String field, Object rejectedValue, String message) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(new ValidationError(field, rejectedValue, message));
        this.valid = false;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean valid;
        private List<ValidationError> errors = new ArrayList<>();

        public Builder valid(boolean valid) {
            this.valid = valid;
            return this;
        }

        public Builder errors(List<ValidationError> errors) {
            this.errors = errors != null ? errors : new ArrayList<>();
            return this;
        }

        public ValidationResult build() {
            return new ValidationResult(valid, errors);
        }
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResult that = (ValidationResult) o;
        return valid == that.valid && Objects.equals(errors, that.errors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, errors);
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", errors=" + errors +
                '}';
    }
}
