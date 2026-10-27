package com.enterprise.ai.risk.engine;

import com.enterprise.ai.common.dto.RiskAssessRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("User Role Factor Scorer Tests")
class UserRoleFactorScorerTest {

    private UserRoleFactorScorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new UserRoleFactorScorer();
    }

    @Test
    @DisplayName("Should assign 15 raw score to ADMIN with 25% weight")
    void testAdminRole() {
        RiskAssessRequest request = RiskAssessRequest.builder().userRole("ADMIN").build();
        assertEquals(15, scorer.calculateRawScore(request));
        assertEquals(4, scorer.calculateWeightedPoints(request)); // round(15 * 0.25) = 4
    }

    @Test
    @DisplayName("Should assign 80 raw score to INTERN with 25% weight")
    void testInternRole() {
        RiskAssessRequest request = RiskAssessRequest.builder().userRole("INTERN").build();
        assertEquals(80, scorer.calculateRawScore(request));
        assertEquals(20, scorer.calculateWeightedPoints(request)); // round(80 * 0.25) = 20
    }

    @Test
    @DisplayName("Should assign 45 raw score to ENGINEER")
    void testEngineerRole() {
        RiskAssessRequest request = RiskAssessRequest.builder().userRole("ENGINEER").build();
        assertEquals(45, scorer.calculateRawScore(request));
        assertEquals(11, scorer.calculateWeightedPoints(request)); // round(45 * 0.25) = 11
    }
}
