package com.enterprise.ai.scanner.detector;

import com.enterprise.ai.common.dto.Violation;
import com.enterprise.ai.common.dto.Violation.Severity;
import com.enterprise.ai.scanner.service.ScannerService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Detects source code, SQL, private keys, hardcoded credentials, known credential prefixes.
 */
@Component
public class SourceCodeDetector implements Detector {

    private record Rule(Pattern pattern, Severity severity, String type, String description) {}

    private final List<Rule> rules = List.of(
            // Fenced code blocks
            new Rule(Pattern.compile("```[\\s\\S]*?```"), Severity.HIGH,
                    "CODE_BLOCK", "Fenced code block detected"),
            // Import statements
            new Rule(Pattern.compile("\\b(?:import|from)\\s+[\\w.]+"), Severity.MEDIUM,
                    "IMPORT_STATEMENT", "Import statement detected"),
            // Method signatures (Java/C-style)
            new Rule(Pattern.compile("\\b(?:public|private|protected|static)\\s+\\w+\\s+\\w+\\s*\\("), Severity.HIGH,
                    "METHOD_SIGNATURE", "Method signature detected"),
            // SQL SELECT
            new Rule(Pattern.compile("\\bSELECT\\s+.+?\\bFROM\\b", Pattern.CASE_INSENSITIVE), Severity.HIGH,
                    "SQL_QUERY", "SQL SELECT query detected"),
            // Private key material
            new Rule(Pattern.compile("-----BEGIN\\s+(?:RSA\\s+)?PRIVATE\\s+KEY-----"), Severity.CRITICAL,
                    "PRIVATE_KEY", "Private key material detected"),
            // Hardcoded credentials: password is/was/=/:  value (with or without quotes)
            new Rule(Pattern.compile("\\bpassword\\s*(?:is|was|=|:)\\s*[\"']?([^\"'\\s,;]+)[\"']?", Pattern.CASE_INSENSITIVE), Severity.CRITICAL,
                    "HARDCODED_PASSWORD", "Hardcoded password detected"),
            // api_key / secret / token = value
            new Rule(Pattern.compile("\\b(?:api_key|secret|token)\\s*(?:is|was|=|:)\\s*[\"']?([^\"'\\s,;]+)[\"']?", Pattern.CASE_INSENSITIVE), Severity.CRITICAL,
                    "HARDCODED_SECRET", "Hardcoded secret/API key detected"),
            // Known credential prefixes
            new Rule(Pattern.compile("\\bsk_live_[A-Za-z0-9]+"), Severity.CRITICAL,
                    "STRIPE_LIVE_KEY", "Stripe live key detected"),
            new Rule(Pattern.compile("\\bsk_test_[A-Za-z0-9]+"), Severity.CRITICAL,
                    "STRIPE_TEST_KEY", "Stripe test key detected"),
            new Rule(Pattern.compile("\\bAKIA[0-9A-Z]{16}\\b"), Severity.CRITICAL,
                    "AWS_ACCESS_KEY", "AWS access key detected"),
            new Rule(Pattern.compile("\\bghp_[A-Za-z0-9]{20,}"), Severity.CRITICAL,
                    "GITHUB_TOKEN", "GitHub personal access token detected"),
            new Rule(Pattern.compile("\\bxox[baprs]-[A-Za-z0-9-]+"), Severity.CRITICAL,
                    "SLACK_TOKEN", "Slack token detected")
    );

    @Override
    public String getCategory() {
        return "SOURCE_CODE";
    }

    @Override
    public List<Violation> detect(String prompt) {
        List<Violation> violations = new ArrayList<>();
        for (Rule rule : rules) {
            Matcher matcher = rule.pattern().matcher(prompt);
            while (matcher.find()) {
                String matched = matcher.group();
                // For credentials with capture groups, use the captured value
                if (matcher.groupCount() >= 1 && matcher.group(1) != null) {
                    matched = matcher.group(1);
                }
                violations.add(Violation.builder()
                        .type(rule.type())
                        .severity(rule.severity())
                        .description(rule.description())
                        .matchedPattern(ScannerService.maskPattern(matched))
                        .build());
            }
        }
        return violations;
    }

    @Override
    public List<DetectorMatch> detectWithMatches(String prompt) {
        List<DetectorMatch> matches = new ArrayList<>();
        for (Rule rule : rules) {
            Matcher matcher = rule.pattern().matcher(prompt);
            while (matcher.find()) {
                String raw = matcher.group();
                String display = raw;
                if (matcher.groupCount() >= 1 && matcher.group(1) != null) {
                    display = matcher.group(1);
                }
                Violation v = Violation.builder()
                        .type(rule.type())
                        .severity(rule.severity())
                        .description(rule.description())
                        .matchedPattern(ScannerService.maskPattern(display))
                        .build();
                matches.add(new DetectorMatch(v, raw));
            }
        }
        return matches;
    }
}
