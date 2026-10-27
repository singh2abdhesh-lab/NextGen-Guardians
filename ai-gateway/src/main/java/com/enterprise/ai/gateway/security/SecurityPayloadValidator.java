package com.enterprise.ai.gateway.security;

import com.enterprise.ai.common.dto.ChatRequest;
import com.enterprise.ai.common.util.SanitizationUtils;
import org.springframework.stereotype.Component;

/**
 * Enterprise payload validator enforcing OWASP LLM04 (Model Denial of Service) protections,
 * strict prompt length limits, and character set sanitization.
 */
@Component
public class SecurityPayloadValidator {

    public static final int MAX_PROMPT_LENGTH = 32_000;
    public static final int MIN_PROMPT_LENGTH = 1;

    /**
     * Validates and cleans incoming ChatRequest payload.
     *
     * @param request ChatRequest
     * @return sanitized and validated prompt string
     */
    public String validateAndSanitize(ChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Chat request payload cannot be null");
        }

        if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
            throw new IllegalArgumentException("Prompt cannot be empty or blank");
        }

        String rawPrompt = request.getPrompt().trim();

        if (rawPrompt.length() > MAX_PROMPT_LENGTH) {
            throw new IllegalArgumentException(
                    "Prompt exceeds maximum permitted character threshold (length=" + rawPrompt.length() + ", max=" + MAX_PROMPT_LENGTH + ")"
            );
        }

        // Clean hidden/invisible zero-width unicode characters
        return SanitizationUtils.stripInvisibleChars(rawPrompt);
    }
}
