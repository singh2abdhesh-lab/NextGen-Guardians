package com.enterprise.ai.audit.controller;

import com.enterprise.ai.audit.service.AuditService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComplianceController.class)
@DisplayName("Compliance Controller WebMvc Tests")
class ComplianceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @Test
    @DisplayName("GET /api/v1/compliance/report should return compliance overview")
    void testComplianceReportEndpoint() throws Exception {
        when(auditService.getComplianceReport(30)).thenReturn(Map.of(
                "totalEvents", 1000,
                "complianceRate", 98.5,
                "frameworksAssessed", List.of("NIST AI RMF", "EU AI Act")
        ));

        mockMvc.perform(get("/api/v1/compliance/report?days=30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents").value(1000))
                .andExpect(jsonPath("$.complianceRate").value(98.5));
    }

    @Test
    @DisplayName("GET /api/v1/compliance/export/csv should stream CSV file with attachment header")
    void testCsvExportEndpoint() throws Exception {
        String csvMock = "ID,REQUEST_ID,USER_ID\n1,\"req-01\",\"alice\"\n";
        when(auditService.generateCsvExport(30)).thenReturn(csvMock);

        mockMvc.perform(get("/api/v1/compliance/export/csv?days=30"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"ai-guardian-audit-trail.csv\""))
                .andExpect(content().contentTypeCompatibleWith(MediaType.parseMediaType("text/csv")))
                .andExpect(content().string(csvMock));
    }

    @Test
    @DisplayName("GET /api/v1/compliance/frameworks should return supported frameworks")
    void testFrameworksEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/compliance/frameworks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].framework").exists())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }
}
