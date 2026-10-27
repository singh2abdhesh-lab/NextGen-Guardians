package com.enterprise.ai.scanner.detector;

import com.enterprise.ai.common.dto.Violation;
import com.enterprise.ai.common.dto.Violation.Severity;
import com.enterprise.ai.common.util.SanitizationUtils;
import com.enterprise.ai.scanner.engine.RegexEngine;
import com.enterprise.ai.scanner.engine.RegexEngine.RegexMatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Detector for Personally Identifiable Information (SSN, Payment Cards, Emails, Phone Numbers).
 */
@Component
public class PiiDetector implements Detector {

    private final RegexEngine regexEngine;

    private static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile("\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|6(?:011|5[0-9]{2})[0-9]{12}|(?:\\d{4}[ -]?){3}\\d{4})\\b");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b(?:\\+?1[-. ]?)?\\(?([0-9]{3})\\)?[-. ]?([0-9]{3})[-. ]?([0-9]{4})\\b");

    public PiiDetector() {
        this.regexEngine = new RegexEngine();
    }

    public PiiDetector(RegexEngine regexEngine) {
        this.regexEngine = regexEngine;
    }

    @Override
    public String getCategory() {
        return "PII";
    }

    @Override
    public List<Violation> detect(String prompt) {
        List<Violation> violations = new ArrayList<>();
        List<DetectorMatch> matches = detectWithMatches(prompt);
        for (DetectorMatch m : matches) {
            violations.add(m.getViolation());
        }
        return violations;
    }

    @Override
    public List<DetectorMatch> detectWithMatches(String prompt) {
        List<DetectorMatch> matches = new ArrayList<>();
        if (prompt == null || prompt.isEmpty()) {
            return matches;
        }

        // 1. Credit Card detection (CRITICAL severity)
        for (RegexMatch match : regexEngine.findMatches(CREDIT_CARD_PATTERN, prompt)) {
            Violation v = Violation.builder()
                    .type("PII_CREDIT_CARD")
                    .severity(Severity.CRITICAL)
                    .description("Payment card number pattern identified")
                    .matchedPattern(SanitizationUtils.maskMiddle(match.getMatchedText()))
                    .build();
            matches.add(new DetectorMatch(v, match.getMatchedText(), match.getStart(), match.getEnd()));
        }

        // 2. SSN detection (HIGH severity)
        for (RegexMatch match : regexEngine.findMatches(SSN_PATTERN, prompt)) {
            Violation v = Violation.builder()
                    .type("PII_SSN")
                    .severity(Severity.HIGH)
                    .description("Social Security Number format identified (XXX-XX-XXXX)")
                    .matchedPattern(SanitizationUtils.maskMiddle(match.getMatchedText()))
                    .build();
            matches.add(new DetectorMatch(v, match.getMatchedText(), match.getStart(), match.getEnd()));
        }

        // 3. Email detection (LOW severity)
        for (RegexMatch match : regexEngine.findMatches(EMAIL_PATTERN, prompt)) {
            Violation v = Violation.builder()
                    .type("PII_EMAIL")
                    .severity(Severity.LOW)
                    .description("Corporate or personal email address detected")
                    .matchedPattern(SanitizationUtils.maskMiddle(match.getMatchedText()))
                    .build();
            matches.add(new DetectorMatch(v, match.getMatchedText(), match.getStart(), match.getEnd()));
        }

        // 4. Phone Number detection (LOW severity)
        for (RegexMatch match : regexEngine.findMatches(PHONE_PATTERN, prompt)) {
            Violation v = Violation.builder()
                    .type("PII_PHONE")
                    .severity(Severity.LOW)
                    .description("Telephone number pattern detected")
                    .matchedPattern(SanitizationUtils.maskMiddle(match.getMatchedText()))
                    .build();
            matches.add(new DetectorMatch(v, match.getMatchedText(), match.getStart(), match.getEnd()));
        }

        return matches;
    }
}
