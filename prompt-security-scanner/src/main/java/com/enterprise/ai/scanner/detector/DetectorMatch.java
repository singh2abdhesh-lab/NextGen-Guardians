package com.enterprise.ai.scanner.detector;

import com.enterprise.ai.common.dto.Violation;

/**
 * Holds a security violation plus the raw matched text and character offsets for redaction.
 */
public class DetectorMatch {
    private Violation violation;
    private String rawMatch;
    private int startOffset = -1;
    private int endOffset = -1;

    public DetectorMatch() {
    }

    public DetectorMatch(Violation violation, String rawMatch) {
        this.violation = violation;
        this.rawMatch = rawMatch;
    }

    public DetectorMatch(Violation violation, String rawMatch, int startOffset, int endOffset) {
        this.violation = violation;
        this.rawMatch = rawMatch;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public Violation getViolation() {
        return violation;
    }

    public void setViolation(Violation violation) {
        this.violation = violation;
    }

    public String getRawMatch() {
        return rawMatch;
    }

    public void setRawMatch(String rawMatch) {
        this.rawMatch = rawMatch;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }
}
