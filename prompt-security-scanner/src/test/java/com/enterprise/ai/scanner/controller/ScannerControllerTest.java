package com.enterprise.ai.scanner.controller;

import com.enterprise.ai.common.dto.RedactResult;
import com.enterprise.ai.common.dto.ScanRequest;
import com.enterprise.ai.common.dto.ScanResult;
import com.enterprise.ai.common.util.JsonUtils;
import com.enterprise.ai.scanner.service.ScannerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScannerController.class)
@DisplayName("Scanner Controller WebMvc Tests")
class ScannerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScannerService scannerService;

    @Test
    @DisplayName("POST /api/v1/scan should return 200 with ScanResult")
    void testScanEndpoint() throws Exception {
        ScanResult mockResult = ScanResult.builder()
                .requestId("req-test")
                .blocked(false)
                .sensitivityScore(10)
                .detectedCategories(List.of("PII"))
                .violations(List.of())
                .build();

        when(scannerService.scan(anyString(), anyString())).thenReturn(mockResult);

        ScanRequest request = new ScanRequest("req-test", "Clean test prompt");

        mockMvc.perform(post("/api/v1/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("req-test"))
                .andExpect(jsonPath("$.blocked").value(false))
                .andExpect(jsonPath("$.sensitivityScore").value(10));
    }

    @Test
    @DisplayName("POST /api/v1/scan/redact should return 200 with RedactResult")
    void testRedactEndpoint() throws Exception {
        RedactResult mockRedact = RedactResult.builder()
                .redactedPrompt("My email is [REDACTED_EMAIL]")
                .violations(List.of())
                .build();

        when(scannerService.redact(anyString(), anyString())).thenReturn(mockRedact);

        ScanRequest request = new ScanRequest("req-test", "My email is user@corp.com");

        mockMvc.perform(post("/api/v1/scan/redact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redactedPrompt").value("My email is [REDACTED_EMAIL]"));
    }

    @Test
    @DisplayName("GET /api/v1/scan/health should return UP status")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/scan/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("prompt-security-scanner"));
    }
}
