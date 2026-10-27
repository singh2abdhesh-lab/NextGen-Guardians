package com.enterprise.ai.scanner.detector;

import com.enterprise.ai.common.dto.Violation;
import com.enterprise.ai.common.dto.Violation.Severity;
import com.enterprise.ai.scanner.service.ScannerService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects confidential business information: trade secrets, earnings, salary, customer lists, M&A.
 */
public class ConfidentialDetector implements Detector {

    private record Rule(Pattern pattern, Severity severity, String type, String description) {}

    private final List<Rule> rules = List.of(
            new Rule(Pattern.compile("\\btrade\\s+secret", Pattern.CASE_INSENSITIVE),
                    Severity.HIGH, "TRADE_SECRET", "Trade secret reference detected"),
            new Rule(Pattern.compile("\\bearnings\\s+(?:report|data|forecast|call)", Pattern.CASE_INSENSITIVE),
                    Severity.HIGH, "EARNINGS_DATA", "Earnings data reference detected"),
            new Rule(Pattern.compile("\\bsalary\\s+(?:data|information|details|range)", Pattern.CASE_INSENSITIVE),
                    Severity.MEDIUM, "SALARY_DATA", "Salary data reference detected"),
            new Rule(Pattern.compile("\\bcustomer\\s+list", Pattern.CASE_INSENSITIVE),
                    Severity.HIGH, "CUSTOMER_LIST", "Customer list reference detected"),
            new Rule(Pattern.compile("\\b(?:merger|acquisition|M&A)\\s+(?:plan|deal|target)", Pattern.CASE_INSENSITIVE),
                    Severity.HIGH, "MA_PLANS", "M&A plans reference detected"),
            new Rule(Pattern.compile("\\bconfidential\\b", Pattern.CASE_INSENSITIVE),
                    Severity.MEDIUM, "CONFIDENTIAL", "Confidential information reference"),
            new Rule(Pattern.compile("\\bproprietary\\b", Pattern.CASE_INSENSITIVE),
                    Severity.MEDIUM, "PROPRIETARY", "Proprietary information reference")
    );

    @Override
    public String getCategory() {
        return "CONFIDENTIAL";
    }

    @Override
    public List<Violation> detect(String prompt) {
        List<Violation> violations = new ArrayList<>();
        for (Rule rule : rules) {
            Matcher matcher = rule.pattern().matcher(prompt);
            while (matcher.find()) {
                violations.add(Violation.builder()
                        .type(rule.type())
                        .severity(rule.severity())
                        .description(rule.description())
                        .matchedPattern(ScannerService.maskPattern(matcher.group()))
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
                Violation v = Violation.builder()
                        .type(rule.type())
                        .severity(rule.severity())
                        .description(rule.description())
                        .matchedPattern(ScannerService.maskPattern(raw))
                        .build();
                matches.add(new DetectorMatch(v, raw));
            }
        }
        return matches;
    }
}
