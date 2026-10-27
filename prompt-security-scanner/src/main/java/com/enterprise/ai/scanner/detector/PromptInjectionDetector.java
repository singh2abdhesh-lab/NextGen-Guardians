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
 * Detector for Prompt Injections, Jailbreak Overrides, Delimiter Hijacking, and System Prompt Leaks.
 */
@Component
public class PromptInjectionDetector implements Detector {

    private final RegexEngine regexEngine;

    private record InjectionRule(Pattern pattern, Severity severity, String type, String description) {}

    private final List<InjectionRule> rules = List.of(
            new InjectionRule(Pattern.compile("ignore\\s+(?:(?:all|any)\\s+)?(?:previous|prior)?\\s*instructions?", Pattern.CASE_INSENSITIVE),
                    Severity.CRITICAL, "PROMPT_INJECTION_OVERRIDE", "Instruction override attempt detected"),
            new InjectionRule(Pattern.compile("disregard\\s+(?:all|all\\s+prior|all\\s+previous)\\s+instructions?", Pattern.CASE_INSENSITIVE),
                    Severity.CRITICAL, "PROMPT_INJECTION_OVERRIDE", "Instruction disregard attempt detected"),
            new InjectionRule(Pattern.compile("forget\\s+(?:everything|all\\s+prior\\s+context|all\\s+previous)", Pattern.CASE_INSENSITIVE),
                    Severity.CRITICAL, "PROMPT_INJECTION_RESET", "Context purge / memory reset attempt detected"),
            new InjectionRule(Pattern.compile("\\b(?:DAN|developer|sudo|god)\\s+mode\\b", Pattern.CASE_INSENSITIVE),
                    Severity.CRITICAL, "PROMPT_INJECTION_JAILBREAK", "Known jailbreak persona mode (DAN/Developer/Sudo) detected"),
            new InjectionRule(Pattern.compile("bypass\\s+(?:all\\s+)?(?:your\\s+)?(?:safety|content|security)\\s+(?:rules|filters|guardrails)", Pattern.CASE_INSENSITIVE),
                    Severity.CRITICAL, "PROMPT_INJECTION_BYPASS", "Explicit safety guardrail bypass instruction detected"),
            new InjectionRule(Pattern.compile("(?:reveal|expose|dump|output|print)\\s+(?:your\\s+)?(?:system\\s+prompt|initial\\s+instructions|secret\\s+instructions)", Pattern.CASE_INSENSITIVE),
                    Severity.HIGH, "PROMPT_INJECTION_LEAK", "System prompt extraction attempt detected"),
            new InjectionRule(Pattern.compile("(?:\\[/?(?:INST|SYS|SYSTEM)\\]|<(?:/?system|/?instruction)>|```(?:system|sys))", Pattern.CASE_INSENSITIVE),
                    Severity.HIGH, "PROMPT_INJECTION_DELIMITER", "LLM special delimiter injection detected"),
            new InjectionRule(Pattern.compile("(?:drop\\s+table|delete\\s+from|union\\s+select)\\s+[a-zA-Z0-9_]+.*--", Pattern.CASE_INSENSITIVE),
                    Severity.CRITICAL, "PROMPT_INJECTION_SQL", "SQL injection instruction vector identified")
    );

    public PromptInjectionDetector() {
        this.regexEngine = new RegexEngine();
    }

    public PromptInjectionDetector(RegexEngine regexEngine) {
        this.regexEngine = regexEngine;
    }

    @Override
    public String getCategory() {
        return "MALICIOUS_PROMPT";
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

        for (InjectionRule rule : rules) {
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
