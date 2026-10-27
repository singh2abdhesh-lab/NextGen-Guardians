package com.enterprise.ai.risk.engine;

import com.enterprise.ai.common.constant.SecurityConstants;
import com.enterprise.ai.common.dto.RiskAssessRequest;
import com.enterprise.ai.common.dto.RiskScore.RiskLevel;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thread-safe Weighted Risk Calculator aggregating individual factor scores into composite risk.
 */
@Component
public class RiskCalculator {

    private final List<FactorScorer> factorScorers;

    public RiskCalculator(List<FactorScorer> factorScorers) {
        this.factorScorers = factorScorers != null ? factorScorers : Collections.emptyList();
    }

    public CalculatedRisk compute(RiskAssessRequest request) {
        Map<String, Integer> factorScores = new LinkedHashMap<>();
        int totalScore = 0;

        for (FactorScorer scorer : factorScorers) {
            int points = scorer.calculateWeightedPoints(request);
            factorScores.put(scorer.getFactorName(), points);
            totalScore += points;
        }

        // Bound between 0 and 100
        totalScore = Math.max(SecurityConstants.SCORE_MIN, Math.min(SecurityConstants.SCORE_MAX, totalScore));

        // Determine Risk Level according to defined thresholds
        RiskLevel level;
        if (totalScore >= SecurityConstants.RISK_SCORE_CRITICAL_THRESHOLD) {
            level = RiskLevel.CRITICAL;
        } else if (totalScore >= SecurityConstants.RISK_SCORE_HIGH_THRESHOLD) {
            level = RiskLevel.HIGH;
        } else if (totalScore >= SecurityConstants.RISK_SCORE_MEDIUM_THRESHOLD) {
            level = RiskLevel.MEDIUM;
        } else {
            level = RiskLevel.LOW;
        }

        return new CalculatedRisk(totalScore, level, factorScores);
    }

    public record CalculatedRisk(int totalScore, RiskLevel riskLevel, Map<String, Integer> factorScores) {}
}
