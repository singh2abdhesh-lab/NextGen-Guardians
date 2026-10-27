package com.enterprise.ai.scanner.detector;

import com.enterprise.ai.common.dto.Violation;
import java.util.List;

/**
 * Base interface for all prompt detectors.
 */
public interface Detector {
    String getCategory();
    List<Violation> detect(String prompt);
    List<DetectorMatch> detectWithMatches(String prompt);
}
