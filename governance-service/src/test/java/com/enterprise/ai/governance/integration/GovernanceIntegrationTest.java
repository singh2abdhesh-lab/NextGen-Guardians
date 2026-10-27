package com.enterprise.ai.governance.integration;

import com.enterprise.ai.governance.GovernanceServiceApplication;
import com.enterprise.ai.governance.dto.AiPolicyDto;
import com.enterprise.ai.governance.dto.PolicySimulationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = GovernanceServiceApplication.class)
@AutoConfigureMockMvc
@DisplayName("Governance Service End-to-End API Integration Tests")
class GovernanceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/governance/health should return UP status")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/governance/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("governance-service"));
    }

    @Test
    @DisplayName("GET /api/v1/governance/overview should return baseline control center metrics")
    void testOverviewEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/governance/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPolicies").isNumber())
                .andExpect(jsonPath("$.totalModels").isNumber())
                .andExpect(jsonPath("$.totalDepartments").isNumber());
    }

    @Test
    @DisplayName("GET /api/v1/governance/policies should return seeded policies")
    void testListPolicies() throws Exception {
        mockMvc.perform(get("/api/v1/governance/policies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].policyKey").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/governance/simulator/run should evaluate prompt against policies")
    void testPolicySimulationRun() throws Exception {
        PolicySimulationRequest request = PolicySimulationRequest.builder()
                .userId("engineer@enterprise.ai")
                .department("Engineering")
                .model("gpt-4o")
                .prompt("Please analyze this code snippet for vulnerabilities")
                .build();

        mockMvc.perform(post("/api/v1/governance/simulator/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expectedDecision").isNotEmpty())
                .andExpect(jsonPath("$.riskScore").isNumber())
                .andExpect(jsonPath("$.latencyMs").isNumber());
    }
}
