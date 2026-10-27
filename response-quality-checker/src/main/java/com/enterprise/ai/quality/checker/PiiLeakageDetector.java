package com.enterprise.ai.quality.checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects whether Personally Identifiable Information (PII) present in the prompt
 * is reflected back into the response, or if the response exposes unredacted PII.
 */
@Component
public class PiiLeakageDetector {

    private static final Logger log = LoggerFactory.getLogger(PiiLeakageDetector.class);

    private static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b(?:\\+?1[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
            "\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|6(?:011|5[0-9]{2})[0-9]{12})\\b"
    );
    private static final Pattern MRN_PATTERN = Pattern.compile("(?i)\\bMRN[-:\\s]*[A-Z0-9]{6,10}\\b");

    /**
     * Inspects prompt and response to identify echoed or exposed PII.
     *
     * @param prompt   the input prompt
     * @param response the model completion
     * @return list of PII leakage violations
     */
    public List<String> detectPiiLeakage(String prompt, String response) {
        List<String> violations = new ArrayList<>();
        if (response == null || response.isBlank()) {
            return violations;
        }

        // 1. Check prompt-to-response echo of sensitive identifiers
        checkEcho(SSN_PATTERN, prompt, response, "Response echoes Social Security Number (SSN) from prompt", violations);
        checkEcho(EMAIL_PATTERN, prompt, response, "Response echoes email address from prompt", violations);
        checkEcho(PHONE_PATTERN, prompt, response, "Response echoes telephone number from prompt", violations);
        checkEcho(CREDIT_CARD_PATTERN, prompt, response, "Response echoes credit card number from prompt", violations);
        checkEcho(MRN_PATTERN, prompt, response, "Response echoes Medical Record Number (MRN) from prompt", violations);

        // 2. Check unsolicited direct emission of high-risk PII in completion
        if (SSN_PATTERN.matcher(response).find() && !violations.contains("Response echoes Social Security Number (SSN) from prompt")) {
            violations.add("Response contains unredacted Social Security Number (SSN)");
        }
        if (CREDIT_CARD_PATTERN.matcher(response).find() && !violations.contains("Response echoes credit card number from prompt")) {
            violations.add("Response contains unredacted payment card number");
        }

        if (!violations.isEmpty()) {
            log.warn("PII leakage violations detected: {}", violations);
        }

        return violations;
    }

    private void checkEcho(Pattern pattern, String prompt, String response, String message, List<String> violations) {
        if (prompt == null || prompt.isBlank()) {
            return;
        }

        Matcher promptMatcher = pattern.matcher(prompt);
        while (promptMatcher.find()) {
            String token = promptMatcher.group();
            if (response.contains(token)) {
                violations.add(message);
                break;
            }
        }
    }
}
