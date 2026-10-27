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
 * Detector for Database Connection Strings, Authentication Tokens, and Plaintext Passwords.
 */
@Component
public class CredentialDetector implements Detector {

    private final RegexEngine regexEngine;

    private static final Pattern JWT_BEARER_PATTERN = Pattern.compile("\\b(?:Bearer\\s+)?(eyJ[A-Za-z0-9-_]{10,}\\.eyJ[A-Za-z0-9-_]{10,}\\.[A-Za-z0-9-_]{10,})\\b");
    private static final Pattern BASIC_AUTH_PATTERN = Pattern.compile("\\bBasic\\s+[A-Za-z0-9+/=]{12,}\\b");
    private static final Pattern DB_CONN_STRING_PATTERN = Pattern.compile("(?:jdbc:(?:postgresql|mysql|oracle|sqlserver)|mongodb(?:\\+srv)?|redis)://[A-Za-z0-9._%+-]+:[^@\\s]+@[A-Za-z0-9.-]+(?::[0-9]+)?/[A-Za-z0-9._-]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern PASSWORD_ASSIGNMENT_PATTERN = Pattern.compile("(?i)\\b(?:password|passwd|db_password|api_secret)\\s*[:=]\\s*['\"]([^'\"\\s]{5,})['\"]");

    public CredentialDetector() {
        this.regexEngine = new RegexEngine();
    }

    public CredentialDetector(RegexEngine regexEngine) {
        this.regexEngine = regexEngine;
    }

    @Override
    public String getCategory() {
        return "CREDENTIAL";
    }

    @Override
    public List<Violation> detect(String prompt) {
        List<Violation> violations = new ArrayList<>();
        for (DetectorMatch m : detectWithMatches(prompt)) {
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

        // 1. Database Connection Strings with embedded credentials (CRITICAL)
        for (RegexMatch m : regexEngine.findMatches(DB_CONN_STRING_PATTERN, prompt)) {
            Violation v = Violation.builder()
                    .type("CREDENTIAL_DB_CONNECTION")
                    .severity(Severity.CRITICAL)
                    .description("Database URI with embedded authentication credentials identified")
                    .matchedPattern(SanitizationUtils.maskMiddle(m.getMatchedText()))
                    .build();
            matches.add(new DetectorMatch(v, m.getMatchedText(), m.getStart(), m.getEnd()));
        }

        // 2. JWT and Bearer Authorization Tokens (HIGH)
        for (RegexMatch m : regexEngine.findMatches(JWT_BEARER_PATTERN, prompt)) {
            Violation v = Violation.builder()
                    .type("CREDENTIAL_JWT_TOKEN")
                    .severity(Severity.HIGH)
                    .description("JSON Web Token (JWT) or Bearer authorization string detected")
                    .matchedPattern(SanitizationUtils.maskMiddle(m.getMatchedText()))
                    .build();
            matches.add(new DetectorMatch(v, m.getMatchedText(), m.getStart(), m.getEnd()));
        }

        // 3. Basic Authentication Headers (HIGH)
        for (RegexMatch m : regexEngine.findMatches(BASIC_AUTH_PATTERN, prompt)) {
            Violation v = Violation.builder()
                    .type("CREDENTIAL_BASIC_AUTH")
                    .severity(Severity.HIGH)
                    .description("HTTP Basic authorization credential header detected")
                    .matchedPattern(SanitizationUtils.maskMiddle(m.getMatchedText()))
                    .build();
            matches.add(new DetectorMatch(v, m.getMatchedText(), m.getStart(), m.getEnd()));
        }

        // 4. Plaintext Password Assignment (HIGH)
        for (RegexMatch m : regexEngine.findMatches(PASSWORD_ASSIGNMENT_PATTERN, prompt)) {
            Violation v = Violation.builder()
                    .type("CREDENTIAL_PASSWORD_PLAINTEXT")
                    .severity(Severity.HIGH)
                    .description("Plaintext password or API secret variable declaration identified")
                    .matchedPattern(SanitizationUtils.maskMiddle(m.getMatchedText()))
                    .build();
            matches.add(new DetectorMatch(v, m.getMatchedText(), m.getStart(), m.getEnd()));
        }

        return matches;
    }
}
