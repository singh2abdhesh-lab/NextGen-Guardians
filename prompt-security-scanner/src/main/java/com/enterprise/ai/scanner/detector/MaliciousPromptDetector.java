package com.enterprise.ai.scanner.detector;

import com.enterprise.ai.common.dto.Violation;
import com.enterprise.ai.common.dto.Violation.Severity;
import com.enterprise.ai.scanner.service.ScannerService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects malicious prompt injection and jailbreak attempts.
 */
public class MaliciousPromptDetector implements Detector {

    private record Rule(Pattern pattern, Severity severity, String type, String description) {}

    private final List<Rule> rules = List.of(
            new Rule(Pattern.compile("ignore\\s+(?:all|any|previous)\\s+instructions", Pattern.CASE_INSENSITIVE),
                    Severity.CRITICAL, "INSTRUCTION_OVERRIDE", "Instruction override attempt"),
            new Rule(Pattern.compile("disregard\\s+all", Pattern.CASE_INSENSITIVE),
                    Severity.CRITICAL, "INSTRUCTION_OVERRIDE", "Instruction disregard attempt"),
            new Rule(Pattern.compile("you\\s+are\\s+now", Pattern.CASE_INSENSITIVE),
                    Severity.HIGH, "IDENTITY_MANIPULATION", "Identity manipulation attempt"),
            new Rule(Pattern.compile("\\bDAN\\s+mode\\b", Pattern.CASE_INSENSITIVE),
                    Severity.CRITICAL, "JAILBREAK", "DAN mode jailbreak attempt"),
            new Rule(Pattern.compile("\\bdeveloper\\s+mode\\b", Pattern.CASE_INSENSITIVE),
                    Severity.CRITICAL, "JAILBREAK", "Developer mode jailbreak attempt"),
            new Rule(Pattern.compile("\\bsudo\\s+mode\\b", Pattern.CASE_INSENSITIVE),
                    Severity.CRITICAL, "JAILBREAK", "Sudo mode jailbreak attempt"),
            new Rule(Pattern.compile("bypass\\s+your\\s+safety\\s+rules", Pattern.CASE_INSENSITIVE),
                    Severity.CRITICAL, "SAFETY_BYPASS", "Safety bypass attempt"),
            new Rule(Pattern.compile("reveal\\s+your\\s+system\\s+prompt", Pattern.CASE_INSENSITIVE),
                    Severity.HIGH, "SYSTEM_PROMPT_LEAK", "System prompt leak attempt")
    );

    @Override
    public String getCategory() {
        return "MALICIOUS_PROMPT";
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
