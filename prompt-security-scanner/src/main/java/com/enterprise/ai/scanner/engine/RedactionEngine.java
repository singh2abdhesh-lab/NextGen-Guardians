package com.enterprise.ai.scanner.engine;

import com.enterprise.ai.common.dto.RedactResult;
import com.enterprise.ai.common.dto.Violation;
import com.enterprise.ai.scanner.detector.DetectorMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Thread-safe Redaction Engine performing boundary-aware sanitization of sensitive matches.
 */
@Component
public class RedactionEngine {

    /**
     * Replaces all matches in the prompt with appropriate security tokens without offset corruption.
     */
    public RedactResult redact(String prompt, List<DetectorMatch> matches) {
        if (prompt == null || prompt.isEmpty() || matches == null || matches.isEmpty()) {
            return RedactResult.builder()
                    .redactedPrompt(prompt != null ? prompt : "")
                    .violations(new ArrayList<>())
                    .build();
        }

        // Sort matches by offset descending so replacement doesn't shift earlier character positions
        List<DetectorMatch> sortedMatches = new ArrayList<>(matches);
        sortedMatches.sort(Comparator.comparingInt(DetectorMatch::getStartOffset).reversed());

        StringBuilder sb = new StringBuilder(prompt);
        List<Violation> violations = new ArrayList<>();

        for (DetectorMatch match : sortedMatches) {
            violations.add(match.getViolation());

            int start = match.getStartOffset();
            int end = match.getEndOffset();

            // Validate character bounds
            if (start >= 0 && end <= sb.length() && start < end) {
                String replacement = getReplacementToken(match.getViolation());
                sb.replace(start, end, replacement);
            } else if (match.getRawMatch() != null && !match.getRawMatch().isEmpty()) {
                // Fallback direct string replacement if offsets are not specified
                int idx = sb.indexOf(match.getRawMatch());
                if (idx != -1) {
                    sb.replace(idx, idx + match.getRawMatch().length(), getReplacementToken(match.getViolation()));
                }
            }
        }

        return RedactResult.builder()
                .redactedPrompt(sb.toString())
                .violations(violations)
                .build();
    }

    /**
     * Determines the appropriate masked token based on the violation type or category.
     */
    public String getReplacementToken(Violation violation) {
        if (violation == null || violation.getType() == null) {
            return "[REDACTED]";
        }

        String type = violation.getType().toUpperCase();
        if (type.contains("SSN")) {
            return "[REDACTED_SSN]";
        } else if (type.contains("CREDIT_CARD") || type.contains("CARD")) {
            return "[REDACTED_CARD]";
        } else if (type.contains("EMAIL")) {
            return "[REDACTED_EMAIL]";
        } else if (type.contains("SECRET") || type.contains("API_KEY") || type.contains("PRIVATE_KEY")) {
            return "[REDACTED_API_KEY]";
        } else if (type.contains("CREDENTIAL") || type.contains("PASSWORD") || type.contains("BEARER")) {
            return "[REDACTED_CREDENTIAL]";
        } else if (type.contains("CONFIDENTIAL")) {
            return "[REDACTED_CONFIDENTIAL]";
        }
        return "[REDACTED]";
    }
}
