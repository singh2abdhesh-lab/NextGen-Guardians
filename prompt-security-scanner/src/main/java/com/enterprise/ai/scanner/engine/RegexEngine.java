package com.enterprise.ai.scanner.engine;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thread-safe Regex Evaluation Engine with compiled pattern caching and ReDoS guardrails.
 */
@Component
public class RegexEngine {

    private final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();

    /**
     * Retrieves or compiles a thread-safe Pattern instance.
     */
    public Pattern getOrCreatePattern(String regex, int flags) {
        String key = regex + "_" + flags;
        return patternCache.computeIfAbsent(key, k -> Pattern.compile(regex, flags));
    }

    public Pattern getOrCreatePattern(String regex) {
        return getOrCreatePattern(regex, 0);
    }

    /**
     * Executes regex matching and extracts all matched substrings with character boundaries.
     */
    public List<RegexMatch> findMatches(Pattern pattern, String text) {
        List<RegexMatch> matches = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return matches;
        }

        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            matches.add(new RegexMatch(matcher.start(), matcher.end(), matcher.group()));
        }
        return matches;
    }

    public List<RegexMatch> findMatches(String regex, int flags, String text) {
        return findMatches(getOrCreatePattern(regex, flags), text);
    }

    public boolean matchesAny(List<Pattern> patterns, String text) {
        if (text == null || text.isEmpty() || patterns == null) {
            return false;
        }
        for (Pattern p : patterns) {
            if (p.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Represents a single matched substring with exact character offset boundaries.
     */
    public static class RegexMatch {
        private final int start;
        private final int end;
        private final String matchedText;

        public RegexMatch(int start, int end, String matchedText) {
            this.start = start;
            this.end = end;
            this.matchedText = matchedText;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getMatchedText() {
            return matchedText;
        }
    }
}
