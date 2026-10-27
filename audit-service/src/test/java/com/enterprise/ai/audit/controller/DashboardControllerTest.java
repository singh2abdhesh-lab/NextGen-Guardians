package com.enterprise.ai.audit.controller;

import com.enterprise.ai.audit.service.AuditService;
import com.enterprise.ai.common.dto.DashboardOverview;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@DisplayName("Dashboard Controller WebMvc Tests")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @Test
    @DisplayName("GET /api/v1/dashboard/overview should return DashboardOverview")
    void testOverviewEndpoint() throws Exception {
        DashboardOverview overview = DashboardOverview.builder()
                .totalRequests(5000L)
                .blockedCount(120L)
                .pendingApprovals(8L)
                .totalCost(89.20)
                .eventsByRiskLevel(Map.of("LOW", 4500L, "HIGH", 120L))
                .eventsByAction(Map.of("ALLOW", 4880L, "BLOCK", 120L))
                .build();

        when(auditService.getOverview()).thenReturn(overview);

        mockMvc.perform(get("/api/v1/dashboard/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequests").value(5000))
                .andExpect(jsonPath("$.blockedCount").value(120))
                .andExpect(jsonPath("$.pendingApprovals").value(8))
                .andExpect(jsonPath("$.totalCost").value(89.20));
    }

    @Test
    @DisplayName("GET /api/v1/dashboard/usage/models should return model usage list")
    void testUsageByModel() throws Exception {
        when(auditService.usageByModel(30)).thenReturn(List.of(
                Map.of("model", "gpt-4o", "requests", 3000L, "costUsd", 45.0)
        ));

        mockMvc.perform(get("/api/v1/dashboard/usage/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].model").value("gpt-4o"))
                .andExpect(jsonPath("$[0].requests").value(3000));
    }
}
