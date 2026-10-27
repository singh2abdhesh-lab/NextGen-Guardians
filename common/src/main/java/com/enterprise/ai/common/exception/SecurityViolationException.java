package com.enterprise.ai.common.exception;

import com.enterprise.ai.common.dto.Violation;
import java.util.Collections;
import java.util.List;

/**
 * Thrown when an input prompt contains critical threat violations, injection vectors, or excessive PII.
 */
public class SecurityViolationException extends AiGuardianException {

    private final List<Violation> violations;

    public SecurityViolationException(String message, List<Violation> violations) {
        super(message, "SECURITY_VIOLATION_DETECTED", 403);
        this.violations = violations != null ? Collections.unmodifiableList(violations) : Collections.emptyList();
    }

    public SecurityViolationException(String message) {
        this(message, Collections.emptyList());
    }

    public List<Violation> getViolations() {
        return violations;
    }
}
