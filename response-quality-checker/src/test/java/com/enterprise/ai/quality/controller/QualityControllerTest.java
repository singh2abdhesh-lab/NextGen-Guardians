package com.enterprise.ai.quality.controller;

import com.enterprise.ai.common.dto.QualityCheckRequest;
import com.enterprise.ai.common.dto.QualityResult;
import com.enterprise.ai.common.dto.QualityResult.Verdict;
import com.enterprise.ai.common.util.JsonUtils;
import com.enterprise.ai.quality.service.QualityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QualityController.class)
@DisplayName("Quality Controller WebMvc Tests")
class QualityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QualityService qualityService;

    @Test
    @DisplayName("POST /api/v1/quality/check should return 200 with QualityResult")
    void testCheckEndpoint() throws Exception {
        QualityResult mockResult = QualityResult.builder()
                .requestId("req-ctrl-1")
                .verdict(Verdict.PASS)
                .hallucinationScore(0.0)
                .policyViolation(false)
                .policyViolations(Collections.emptyList())
                .approvalRequired(false)
                .build();

        when(qualityService.check(any(QualityCheckRequest.class))).thenReturn(mockResult);

        QualityCheckRequest request = QualityCheckRequest.builder()
                .requestId("req-ctrl-1")
                .prompt("What is reactive programming?")
                .response("Reactive programming is a declarative programming paradigm concerned with data streams and the propagation of change.")
                .build();

        mockMvc.perform(post("/api/v1/quality/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("req-ctrl-1"))
                .andExpect(jsonPath("$.verdict").value("PASS"))
                .andExpect(jsonPath("$.hallucinationScore").value(0.0))
                .andExpect(jsonPath("$.policyViolation").value(false))
                .andExpect(jsonPath("$.approvalRequired").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/quality/check with blank payload should return 400 Bad Request")
    void testCheckValidationFailure() throws Exception {
        QualityCheckRequest invalidRequest = QualityCheckRequest.builder()
                .requestId("req-bad-1")
                .prompt("") // @NotBlank failure
                .response("Valid response")
                .build();

        mockMvc.perform(post("/api/v1/quality/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/quality/health should return UP status and service identifier")
    void testServiceHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/quality/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("response-quality-checker"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    @Test
    @DisplayName("GET /health root probe should return UP status")
    void testRootHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("response-quality-checker"));
    }
}
