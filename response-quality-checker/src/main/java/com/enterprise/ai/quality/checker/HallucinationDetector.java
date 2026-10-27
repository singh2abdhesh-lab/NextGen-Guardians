package com.enterprise.ai.quality.checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates model completion for hallucination indicators, ungrounded assertions,
 * overconfident claims, and fabricated web/citation artifacts.
 */
@Component
public class HallucinationDetector {

    private static final Logger log = LoggerFactory.getLogger(HallucinationDetector.class);

    private static final Pattern FABRICATED_URL_PATTERN = Pattern.compile(
            "https?://(?:www\\.)?[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}/[a-zA-Z0-9/_.-]{22,}",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SUSPICIOUS_CITATION_PATTERN = Pattern.compile(
            "\\b(?:studies show that|research indicates that|according to a \\d{4} study|statistics show that|clinical trials proved that|a Harvard study published in \\d{4})\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OVERCONFIDENCE_PATTERN = Pattern.compile(
            "\\b(?:guaranteed|100%\\s*accurate|absolutely certain|without a doubt|proven fact beyond question|indisputable truth|100%\\s*guaranteed)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern INVENTED_REGULATION_PATTERN = Pattern.compile(
            "\\b(?:Section\\s*\\d{3}[A-Z]?\\s*of the\\s*(?:Universal|Global|Federal)\\s*(?:AI|Cyber)\\s*Directive)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Calculates a normalized hallucination probability score between 0.0 and 1.0.
     *
     * @param response the LLM response text
     * @return hallucination score rounded to 2 decimal places
     */
    public double calculateHallucinationScore(String response) {
        if (response == null || response.isBlank()) {
            return 0.0;
        }

        double score = 0.0;
        List<String> flags = detectHallucinationFlags(response);

        if (FABRICATED_URL_PATTERN.matcher(response).find()) {
            score += 0.40;
        }
        if (SUSPICIOUS_CITATION_PATTERN.matcher(response).find()) {
            score += 0.30;
        }
        if (OVERCONFIDENCE_PATTERN.matcher(response).find()) {
            score += 0.25;
        }
        if (INVENTED_REGULATION_PATTERN.matcher(response).find()) {
            score += 0.35;
        }

        // Bound to [0.0, 1.0]
        double normalizedScore = Math.min(1.0, score);
        double rounded = Math.round(normalizedScore * 100.0) / 100.0;
        log.debug("Hallucination analysis rendered score={} with flags={}", rounded, flags);
        return rounded;
    }

    /**
     * Detects descriptive reasons/markers indicating hallucination.
     *
     * @param response the LLM response text
     * @return list of descriptive hallucination warnings
     */
    public List<String> detectHallucinationFlags(String response) {
        List<String> flags = new ArrayList<>();
        if (response == null || response.isBlank()) {
            return flags;
        }

        Matcher urlMatcher = FABRICATED_URL_PATTERN.matcher(response);
        if (urlMatcher.find()) {
            flags.add("Potentially fabricated or high-entropy unverified URL detected: " + urlMatcher.group());
        }

        Matcher citationMatcher = SUSPICIOUS_CITATION_PATTERN.matcher(response);
        if (citationMatcher.find()) {
            flags.add("Ungrounded or unsourced scientific/statistical citation pattern detected: " + citationMatcher.group());
        }

        Matcher overconfidenceMatcher = OVERCONFIDENCE_PATTERN.matcher(response);
        if (overconfidenceMatcher.find()) {
            flags.add("Uncalibrated absolute assertion / overconfidence claim detected: " + overconfidenceMatcher.group());
        }

        Matcher regMatcher = INVENTED_REGULATION_PATTERN.matcher(response);
        if (regMatcher.find()) {
            flags.add("Potentially hallucinated regulatory citation detected: " + regMatcher.group());
        }

        return flags;
    }
}
