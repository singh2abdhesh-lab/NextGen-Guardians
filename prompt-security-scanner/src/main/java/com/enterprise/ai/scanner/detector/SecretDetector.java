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
 * Detector for High-Entropy API Secrets, Cloud Provider Keys, and Cryptographic Private Keys.
 */
@Component
public class SecretDetector implements Detector {

    private final RegexEngine regexEngine;

    private static final Pattern OPENAI_KEY_PATTERN = Pattern.compile("\\bsk-[a-zA-Z0-9_-]{20,}\\b");
    private static final Pattern AWS_ACCESS_KEY_PATTERN = Pattern.compile("\\bAKIA[0-9A-Z]{16}\\b");
    private static final Pattern AWS_SECRET_PATTERN = Pattern.compile("(?i)aws_secret_access_key\\s*[:=]\\s*['\"]?([A-Za-z0-9/+=]{40})['\"]?");
    private static final Pattern GITHUB_TOKEN_PATTERN = Pattern.compile("\\b(?:ghp_[a-zA-Z0-9]{36}|github_pat_[a-zA-Z0-9_]{50,})\\b");
    private static final Pattern SLACK_TOKEN_PATTERN = Pattern.compile("\\bxox[baprs]-[0-9a-zA-Z-]{20,}\\b");
    private static final Pattern PRIVATE_KEY_PATTERN = Pattern.compile("-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----");

    public SecretDetector() {
        this.regexEngine = new RegexEngine();
    }

    public SecretDetector(RegexEngine regexEngine) {
        this.regexEngine = regexEngine;
    }

    @Override
    public String getCategory() {
        return "SECRET";
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

        // 1. OpenAI Keys
        for (RegexMatch m : regexEngine.findMatches(OPENAI_KEY_PATTERN, prompt)) {
            Violation v = Violation.builder()
                    .type("SECRET_OPENAI_KEY")
                    .severity(Severity.CRITICAL)
                    .description("OpenAI API secret key pattern detected")
                    .matchedPattern(SanitizationUtils.maskMiddle(m.getMatchedText()))
                    .build();
            matches.add(new DetectorMatch(v, m.getMatchedText(), m.getStart(), m.getEnd()));
        }

        // 2. AWS Access Keys
        for (RegexMatch m : regexEngine.findMatches(AWS_ACCESS_KEY_PATTERN, prompt)) {
            Violation v = Violation.builder()
                    .type("SECRET_AWS_ACCESS_KEY")
                    .severity(Severity.CRITICAL)
                    .description("Amazon Web Services access key ID detected")
                    .matchedPattern(SanitizationUtils.maskMiddle(m.getMatchedText()))
                    .build();
            matches.add(new DetectorMatch(v, m.getMatchedText(), m.getStart(), m.getEnd()));
        }

        // 3. AWS Secret Access Keys
        for (RegexMatch m : regexEngine.findMatches(AWS_SECRET_PATTERN, prompt)) {
            Violation v = Violation.builder()
                    .type("SECRET_AWS_SECRET_KEY")
                    .severity(Severity.CRITICAL)
                    .description("Amazon Web Services secret access credential detected")
                    .matchedPattern(SanitizationUtils.maskMiddle(m.getMatchedText()))
                    .build();
            matches.add(new DetectorMatch(v, m.getMatchedText(), m.getStart(), m.getEnd()));
        }

        // 4. GitHub Tokens
        for (RegexMatch m : regexEngine.findMatches(GITHUB_TOKEN_PATTERN, prompt)) {
            Violation v = Violation.builder()
                    .type("SECRET_GITHUB_TOKEN")
                    .severity(Severity.CRITICAL)
                    .description("GitHub Personal Access Token detected")
                    .matchedPattern(SanitizationUtils.maskMiddle(m.getMatchedText()))
                    .build();
            matches.add(new DetectorMatch(v, m.getMatchedText(), m.getStart(), m.getEnd()));
        }

        // 5. Slack Bot / User Tokens
        for (RegexMatch m : regexEngine.findMatches(SLACK_TOKEN_PATTERN, prompt)) {
            Violation v = Violation.builder()
                    .type("SECRET_SLACK_TOKEN")
                    .severity(Severity.HIGH)
                    .description("Slack OAuth integration token detected")
                    .matchedPattern(SanitizationUtils.maskMiddle(m.getMatchedText()))
                    .build();
            matches.add(new DetectorMatch(v, m.getMatchedText(), m.getStart(), m.getEnd()));
        }

        // 6. Cryptographic Private Key Headers
        for (RegexMatch m : regexEngine.findMatches(PRIVATE_KEY_PATTERN, prompt)) {
            Violation v = Violation.builder()
                    .type("SECRET_PRIVATE_KEY")
                    .severity(Severity.CRITICAL)
                    .description("PEM Encoded Private Cryptographic Key header detected")
                    .matchedPattern(SanitizationUtils.maskMiddle(m.getMatchedText()))
                    .build();
            matches.add(new DetectorMatch(v, m.getMatchedText(), m.getStart(), m.getEnd()));
        }

        return matches;
    }
}
