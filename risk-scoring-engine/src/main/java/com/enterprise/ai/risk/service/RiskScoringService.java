package com.enterprise.ai.risk.service;

import com.enterprise.ai.common.dto.RiskAssessRequest;
import com.enterprise.ai.common.dto.RiskScore;
import com.enterprise.ai.common.dto.RiskScore.Action;
import com.enterprise.ai.risk.engine.RiskCalculator;
import com.enterprise.ai.risk.engine.RiskCalculator.CalculatedRisk;
import com.enterprise.ai.risk.engine.RiskRuleEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service orchestrating weighted multi-factor risk scoring and rule-based action enforcement.
 */
@Service
public class RiskScoringService {

    private static final Logger log = LoggerFactory.getLogger(RiskScoringService.class);

    private final RiskCalculator riskCalculator;
    private final RiskRuleEngine riskRuleEngine;

    public RiskScoringService(RiskCalculator riskCalculator, RiskRuleEngine riskRuleEngine) {
        this.riskCalculator = riskCalculator;
        this.riskRuleEngine = riskRuleEngine;
    }

    /**
     * Fallback constructor initializing default engine components if needed.
     */
    public RiskScoringService() {
        this.riskCalculator = new RiskCalculator(List.of());
        this.riskRuleEngine = new RiskRuleEngine();
    }

    /**
     * Assesses risk based on weighted factors:
     * - Data sensitivity: 40%
     * - User role: 25%
     * - Prompt category: 20%
     * - Model used: 15%
     */
    public RiskScore assess(RiskAssessRequest request) {
        if (request == null) {
            log.warn("Null RiskAssessRequest received, returning default LOW risk score");
            return RiskScore.builder().build();
        }

        CalculatedRisk calculated = riskCalculator.compute(request);
        Action action = riskRuleEngine.evaluateAction(request, calculated.totalScore(), calculated.riskLevel());

        log.debug("Risk assessment for request '{}': Total Score = {}, Level = {}, Action = {}, Factors = {}",
                request.getRequestId(), calculated.totalScore(), calculated.riskLevel(), action, calculated.factorScores());

        return RiskScore.builder()
                .requestId(request.getRequestId())
                .totalScore(calculated.totalScore())
                .riskLevel(calculated.riskLevel())
                .action(action)
                .factorScores(calculated.factorScores())
                .build();
    }
}
