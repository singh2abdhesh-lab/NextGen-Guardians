package com.enterprise.ai.risk.controller;

import com.enterprise.ai.common.dto.RiskAssessRequest;
import com.enterprise.ai.common.dto.RiskScore;
import com.enterprise.ai.common.dto.RiskScore.Action;
import com.enterprise.ai.common.dto.RiskScore.RiskLevel;
import com.enterprise.ai.common.util.JsonUtils;
import com.enterprise.ai.risk.service.RiskScoringService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RiskController.class)
@DisplayName("Risk Controller WebMvc Tests")
class RiskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RiskScoringService riskScoringService;

    @Test
    @DisplayName("POST /api/v1/risk/assess should return 200 with RiskScore")
    void testAssessEndpoint() throws Exception {
        RiskScore mockScore = RiskScore.builder()
                .requestId("req-risk-01")
                .totalScore(65)
                .riskLevel(RiskLevel.HIGH)
                .action(Action.REVIEW)
                .factorScores(Map.of("dataSensitivity", 25, "userRole", 20, "promptCategory", 15, "modelUsed", 5))
                .build();

        when(riskScoringService.assess(any(RiskAssessRequest.class))).thenReturn(mockScore);

        RiskAssessRequest request = RiskAssessRequest.builder()
                .requestId("req-risk-01")
                .dataSensitivity(40)
                .userRole("ENGINEER")
                .promptCategory("CODE")
                .model("gpt-4o")
                .build();

        mockMvc.perform(post("/api/v1/risk/assess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("req-risk-01"))
                .andExpect(jsonPath("$.totalScore").value(65))
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.action").value("REVIEW"));
    }

    @Test
    @DisplayName("GET /api/v1/risk/health should return UP status")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/risk/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("risk-scoring-engine"));
    }
}
