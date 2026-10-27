package com.enterprise.ai.common.exception;

/**
 * Base Runtime Exception for all Enterprise AI Guardian platform domain and infrastructure errors.
 */
public class AiGuardianException extends RuntimeException {

    private final String errorCode;
    private final int status;

    public AiGuardianException(String message) {
        super(message);
        this.errorCode = "AI_GUARDIAN_GENERAL_ERROR";
        this.status = 500;
    }

    public AiGuardianException(String message, String errorCode, int status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public AiGuardianException(String message, Throwable cause, String errorCode, int status) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getStatus() {
        return status;
    }
}
