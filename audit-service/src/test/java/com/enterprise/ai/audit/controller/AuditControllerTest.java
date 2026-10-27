package com.enterprise.ai.audit.controller;

import com.enterprise.ai.audit.entity.AuditEventEntity;
import com.enterprise.ai.audit.service.AuditService;
import com.enterprise.ai.common.dto.ApprovalRequest;
import com.enterprise.ai.common.dto.AuditEvent;
import com.enterprise.ai.common.enums.ApprovalStatus;
import com.enterprise.ai.common.util.JsonUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditController.class)
@DisplayName("Audit Controller WebMvc Tests")
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @Test
    @DisplayName("POST /api/v1/audit/events (REST fallback receiver) should ingest event")
    void testCreateEventFallback() throws Exception {
        AuditEvent event = AuditEvent.builder()
                .requestId("req-post-1")
                .userId("tester")
                .action("ALLOW")
                .build();

        AuditEventEntity savedEntity = AuditEventEntity.builder()
                .id(1L)
                .requestId("req-post-1")
                .action("ALLOW")
                .build();

        when(auditService.saveEvent(any(AuditEvent.class))).thenReturn(savedEntity);
        when(auditService.toDto(any(AuditEventEntity.class))).thenReturn(event);

        mockMvc.perform(post("/api/v1/audit/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("req-post-1"));
    }

    @Test
    @DisplayName("GET /api/v1/audit/events should return list of events")
    void testListEvents() throws Exception {
        AuditEvent e1 = AuditEvent.builder().id(1L).requestId("req-1").build();
        when(auditService.listEvents(100)).thenReturn(List.of(e1));

        mockMvc.perform(get("/api/v1/audit/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requestId").value("req-1"));
    }

    @Test
    @DisplayName("POST /api/v1/audit/events/{id}/approve should record approval")
    void testApproveEndpoint() throws Exception {
        AuditEvent approved = AuditEvent.builder()
                .id(55L)
                .approvalStatus("APPROVED")
                .approver("admin")
                .build();

        when(auditService.approve(eq(55L), eq("admin"), any())).thenReturn(approved);

        mockMvc.perform(post("/api/v1/audit/events/55/approve?approver=admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"))
                .andExpect(jsonPath("$.approver").value("admin"));
    }

    @Test
    @DisplayName("POST /api/v1/audit/approvals should process ApprovalRequest body")
    void testProcessApproval() throws Exception {
        ApprovalRequest request = ApprovalRequest.builder()
                .eventId(88L)
                .decision(ApprovalStatus.APPROVED)
                .approver("auditor.joe")
                .reason("Checked safe")
                .build();

        AuditEvent result = AuditEvent.builder()
                .id(88L)
                .approvalStatus("APPROVED")
                .approver("auditor.joe")
                .build();

        when(auditService.processApproval(any(ApprovalRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/audit/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtils.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"));
    }

    @Test
    @DisplayName("GET /api/v1/audit/health should return UP status")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/audit/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("audit-service"));
    }

    @Test
    @DisplayName("GET /health root probe should return UP status")
    void testRootHealth() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
