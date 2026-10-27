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
 * Detector for Corporate Confidential Information, Trade Secrets, M&A Intelligence, and Salary Data.
 */
@Component
public class ConfidentialDataDetector implements Detector {

    private final RegexEngine regexEngine;

    private record ConfidentialRule(Pattern pattern, Severity severity, String type, String description) {}

    private final List<ConfidentialRule> rules = List.of(
            new ConfidentialRule(Pattern.compile("\\b(?:strictly\\s+confidential|internal\\s+only|proprietary\\s+and\\s+confidential|do\\s+not\\s+distribute)\\b", Pattern.CASE_INSENSITIVE),
                    Severity.CRITICAL, "CONFIDENTIAL_MARKING", "Explicit corporate confidential classification banner detected"),
            new ConfidentialRule(Pattern.compile("\\btrade\\s+secrets?\\b", Pattern.CASE_INSENSITIVE),
                    Severity.HIGH, "CONFIDENTIAL_TRADE_SECRET", "Proprietary trade secret reference identified"),
            new ConfidentialRule(Pattern.compile("\\b(?:quarterly\\s+)?earnings\\s+(?:report|data|forecast|call|release)\\b", Pattern.CASE_INSENSITIVE),
                    Severity.HIGH, "CONFIDENTIAL_EARNINGS_DATA", "Unreleased earnings or quarterly financial projections detected"),
            new ConfidentialRule(Pattern.compile("\\b(?:salary|payroll|compensation)\\s+(?:data|information|details|range|grid|table)\\b", Pattern.CASE_INSENSITIVE),
                    Severity.HIGH, "CONFIDENTIAL_COMPENSATION_DATA", "Personnel payroll or executive compensation data detected"),
            new ConfidentialRule(Pattern.compile("\\bcustomer\\s+(?:list|database|export|records)\\b", Pattern.CASE_INSENSITIVE),
                    Severity.HIGH, "CONFIDENTIAL_CUSTOMER_LIST", "Customer database or CRM client registry reference identified"),
            new ConfidentialRule(Pattern.compile("\\b(?:merger|acquisition|M&A)\\s+(?:plan|deal|target|term\\s+sheet|synergies)\\b", Pattern.CASE_INSENSITIVE),
                    Severity.HIGH, "CONFIDENTIAL_MA_INTEL", "Merger, acquisition, or buyout term sheet discussion detected"),
            new ConfidentialRule(Pattern.compile("\\b(?:under\\s+NDA|non-disclosure\\s+agreement|covered\\s+by\\s+NDA)\\b", Pattern.CASE_INSENSITIVE),
                    Severity.MEDIUM, "CONFIDENTIAL_NDA", "Non-Disclosure Agreement restriction identified")
    );

    public ConfidentialDataDetector() {
        this.regexEngine = new RegexEngine();
    }

    public ConfidentialDataDetector(RegexEngine regexEngine) {
        this.regexEngine = regexEngine;
    }

    @Override
    public String getCategory() {
        return "CONFIDENTIAL";
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

        for (ConfidentialRule rule : rules) {
            for (RegexMatch m : regexEngine.findMatches(rule.pattern(), prompt)) {
                Violation v = Violation.builder()
                        .type(rule.type())
                        .severity(rule.severity())
                        .description(rule.description())
                        .matchedPattern(SanitizationUtils.maskMiddle(m.getMatchedText()))
                        .build();
                matches.add(new DetectorMatch(v, m.getMatchedText(), m.getStart(), m.getEnd()));
            }
        }

        return matches;
    }
}
