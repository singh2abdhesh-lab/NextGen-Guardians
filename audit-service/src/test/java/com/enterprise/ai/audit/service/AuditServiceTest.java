package com.enterprise.ai.audit.service;

import com.enterprise.ai.audit.entity.AuditEventEntity;
import com.enterprise.ai.audit.repository.AuditEventRepository;
import com.enterprise.ai.common.dto.ApprovalRequest;
import com.enterprise.ai.common.dto.AuditEvent;
import com.enterprise.ai.common.dto.DashboardOverview;
import com.enterprise.ai.common.enums.ApprovalStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Audit Service Unit Tests")
class AuditServiceTest {

    @Mock
    private AuditEventRepository repository;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(repository);
    }

    @Test
    @DisplayName("saveEvent should persist and set PENDING when approval is required")
    void testSaveEventWithApprovalRequired() {
        AuditEvent event = AuditEvent.builder()
                .requestId("req-save-1")
                .userId("user-1")
                .action("REVIEW")
                .riskScore(75)
                .riskLevel("HIGH")
                .approvalRequired(true)
                .build();

        AuditEventEntity savedEntity = AuditEventEntity.builder()
                .id(101L)
                .requestId("req-save-1")
                .userId("user-1")
                .action("REVIEW")
                .approvalRequired(true)
                .approvalStatus("PENDING")
                .build();

        when(repository.save(any(AuditEventEntity.class))).thenReturn(savedEntity);

        AuditEventEntity result = auditService.saveEvent(event);

        assertNotNull(result);
        assertEquals(101L, result.getId());
        assertEquals("PENDING", result.getApprovalStatus());
        verify(repository, times(1)).save(any(AuditEventEntity.class));
    }

    @Test
    @DisplayName("approve should transition status to APPROVED and record approver details")
    void testApproveEvent() {
        AuditEventEntity existing = AuditEventEntity.builder()
                .id(202L)
                .requestId("req-app-1")
                .approvalStatus("PENDING")
                .build();

        when(repository.findById(202L)).thenReturn(Optional.of(existing));
        when(repository.save(any(AuditEventEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AuditEvent result = auditService.approve(202L, "security.lead@enterprise.ai", "Risk verified within policy");

        assertNotNull(result);
        assertEquals("APPROVED", result.getApprovalStatus());
        assertEquals("security.lead@enterprise.ai", result.getApprover());
        assertNotNull(result.getApprovalTimestamp());
    }

    @Test
    @DisplayName("reject should transition status to REJECTED")
    void testRejectEvent() {
        AuditEventEntity existing = AuditEventEntity.builder()
                .id(303L)
                .requestId("req-rej-1")
                .approvalStatus("PENDING")
                .build();

        when(repository.findById(303L)).thenReturn(Optional.of(existing));
        when(repository.save(any(AuditEventEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        AuditEvent result = auditService.reject(303L, "admin", "Critical policy violation");

        assertNotNull(result);
        assertEquals("REJECTED", result.getApprovalStatus());
        assertEquals("admin", result.getApprover());
    }

    @Test
    @DisplayName("processApproval should route correctly from ApprovalRequest DTO")
    void testProcessApprovalDto() {
        AuditEventEntity existing = AuditEventEntity.builder()
                .id(404L)
                .requestId("req-dto-1")
                .approvalStatus("PENDING")
                .build();

        when(repository.findById(404L)).thenReturn(Optional.of(existing));
        when(repository.save(any(AuditEventEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ApprovalRequest request = ApprovalRequest.builder()
                .eventId(404L)
                .decision(ApprovalStatus.APPROVED)
                .approver("auditor.jane")
                .reason("Legitimate developer prompt")
                .build();

        AuditEvent result = auditService.processApproval(request);
        assertEquals("APPROVED", result.getApprovalStatus());
        assertEquals("auditor.jane", result.getApprover());
    }

    @Test
    @DisplayName("getOverview should aggregate dashboard totals accurately")
    void testGetOverview() {
        when(repository.countAll()).thenReturn(1500L);
        when(repository.countBlocked()).thenReturn(75L);
        when(repository.countPendingApprovals()).thenReturn(5L);
        when(repository.totalCost()).thenReturn(42.50);
        when(repository.countByRiskLevel()).thenReturn(List.of(
                new Object[]{"LOW", 1200L},
                new Object[]{"MEDIUM", 225L},
                new Object[]{"HIGH", 75L}
        ));
        when(repository.countByAction()).thenReturn(List.of(
                new Object[]{"ALLOW", 1420L},
                new Object[]{"BLOCK", 75L},
                new Object[]{"REVIEW", 5L}
        ));

        DashboardOverview overview = auditService.getOverview();

        assertNotNull(overview);
        assertEquals(1500L, overview.getTotalRequests());
        assertEquals(75L, overview.getBlockedCount());
        assertEquals(5L, overview.getPendingApprovals());
        assertEquals(42.50, overview.getTotalCost());
        assertEquals(1200L, overview.getEventsByRiskLevel().get("LOW"));
        assertEquals(1420L, overview.getEventsByAction().get("ALLOW"));
    }

    @Test
    @DisplayName("getComplianceReport should calculate compliance rate and framework summary")
    void testGetComplianceReport() {
        AuditEventEntity e1 = AuditEventEntity.builder()
                .requestId("req-c1")
                .action("ALLOW")
                .riskLevel("LOW")
                .timestamp(LocalDateTime.now())
                .build();
        AuditEventEntity e2 = AuditEventEntity.builder()
                .requestId("req-c2")
                .action("BLOCK")
                .riskLevel("CRITICAL")
                .violationsSummary("PROMPT_INJECTION")
                .timestamp(LocalDateTime.now())
                .build();

        when(repository.findEventsSince(any(LocalDateTime.class))).thenReturn(List.of(e1, e2));

        Map<String, Object> report = auditService.getComplianceReport(30);

        assertNotNull(report);
        assertEquals(2, report.get("totalEvents"));
        assertEquals(1L, report.get("allowedCount"));
        assertEquals(1L, report.get("blockedCount"));
        assertEquals(50.0, report.get("complianceRate"));
        assertTrue(report.containsKey("frameworksAssessed"));
    }

    @Test
    @DisplayName("generateCsvExport should produce valid CSV header and rows")
    void testGenerateCsvExport() {
        AuditEventEntity e1 = AuditEventEntity.builder()
                .id(1L)
                .requestId("req-csv-1")
                .userId("developer@corp.com")
                .userRole("ENGINEER")
                .model("gpt-4o")
                .action("ALLOW")
                .riskScore(15)
                .riskLevel("LOW")
                .tokensIn(100)
                .tokensOut(50)
                .costUsd(0.002)
                .qualityVerdict("PASS")
                .approvalStatus("NONE")
                .timestamp(LocalDateTime.now())
                .build();

        when(repository.findEventsSince(any(LocalDateTime.class))).thenReturn(List.of(e1));

        String csv = auditService.generateCsvExport(7);

        assertNotNull(csv);
        assertTrue(csv.startsWith("ID,REQUEST_ID,USER_ID"));
        assertTrue(csv.contains("req-csv-1"));
        assertTrue(csv.contains("developer@corp.com"));
    }
}
