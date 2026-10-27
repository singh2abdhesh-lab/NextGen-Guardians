package com.enterprise.ai.audit.repository;

import com.enterprise.ai.audit.entity.ApprovalRecordEntity;
import com.enterprise.ai.audit.entity.AuditEventEntity;
import com.enterprise.ai.audit.entity.ModelPricingEntity;
import com.enterprise.ai.audit.entity.ViolationEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Database Layer and JPA Relational Mapping Tests")
class AuditEventRepositoryTest {

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private ViolationRepository violationRepository;

    @Autowired
    private ApprovalRecordRepository approvalRecordRepository;

    @Autowired
    private ModelPricingRepository modelPricingRepository;

    @Test
    @DisplayName("Persisting AuditEventEntity with ViolationEntity cascade relationship")
    void testAuditEventWithViolationsCascade() {
        AuditEventEntity event = AuditEventEntity.builder()
                .requestId("req-rel-01")
                .userId("developer@enterprise.corp")
                .userRole("ENGINEER")
                .model("gpt-4o")
                .promptCategory("CODE")
                .action("BLOCK")
                .riskScore(85)
                .riskLevel("CRITICAL")
                .timestamp(LocalDateTime.now())
                .approvalStatus("NONE")
                .build();

        ViolationEntity v1 = ViolationEntity.builder()
                .violationType("SECRET_LEAK")
                .severity("CRITICAL")
                .description("AWS Secret Access Key detected")
                .matchedText("AKIAIOSFODNN7EXAMPLE")
                .build();

        ViolationEntity v2 = ViolationEntity.builder()
                .violationType("PROMPT_INJECTION")
                .severity("HIGH")
                .description("Jailbreak attempt")
                .build();

        event.addViolation(v1);
        event.addViolation(v2);

        AuditEventEntity saved = auditEventRepository.save(event);
        assertNotNull(saved.getId());

        List<ViolationEntity> violations = violationRepository.findByAuditEventId(saved.getId());
        assertEquals(2, violations.size());
        assertEquals("SECRET_LEAK", violations.get(0).getViolationType());
    }

    @Test
    @DisplayName("Persisting AuditEventEntity with One-to-One ApprovalRecordEntity")
    void testAuditEventWithApprovalRecord() {
        AuditEventEntity event = AuditEventEntity.builder()
                .requestId("req-appr-01")
                .userId("intern@enterprise.corp")
                .userRole("INTERN")
                .action("REVIEW")
                .approvalStatus("PENDING")
                .approvalRequired(true)
                .timestamp(LocalDateTime.now())
                .build();

        ApprovalRecordEntity record = ApprovalRecordEntity.builder()
                .decision("APPROVED")
                .approver("secops.lead@enterprise.corp")
                .reason("Authorized after prompt inspection")
                .reviewedAt(LocalDateTime.now())
                .build();

        event.setApprovalRecord(record);

        AuditEventEntity saved = auditEventRepository.save(event);
        assertNotNull(saved.getId());

        Optional<ApprovalRecordEntity> savedRecord = approvalRecordRepository.findByAuditEventId(saved.getId());
        assertTrue(savedRecord.isPresent());
        assertEquals("APPROVED", savedRecord.get().getDecision());
        assertEquals("secops.lead@enterprise.corp", savedRecord.get().getApprover());
    }

    @Test
    @DisplayName("Model pricing table contains initial seed data")
    void testModelPricingSeeded() {
        Optional<ModelPricingEntity> gpt4o = modelPricingRepository.findByModelNameAndActiveTrue("gpt-4o");
        assertTrue(gpt4o.isPresent());
        assertEquals(0.0025, gpt4o.get().getCostPer1kInput());
        assertEquals(0.0100, gpt4o.get().getCostPer1kOutput());

        List<ModelPricingEntity> allActive = modelPricingRepository.findAllByActiveTrue();
        assertFalse(allActive.isEmpty());
        assertTrue(allActive.size() >= 4);
    }

    @Test
    @DisplayName("Aggregation queries compute statistics accurately")
    void testAggregationQueries() {
        AuditEventEntity e1 = AuditEventEntity.builder()
                .requestId("req-agg-1")
                .userId("u1")
                .userRole("ENGINEER")
                .model("gpt-4o")
                .action("ALLOW")
                .riskScore(10)
                .riskLevel("LOW")
                .costUsd(0.05)
                .timestamp(LocalDateTime.now())
                .build();

        AuditEventEntity e2 = AuditEventEntity.builder()
                .requestId("req-agg-2")
                .userId("u2")
                .userRole("ANALYST")
                .model("gpt-4o")
                .action("BLOCK")
                .riskScore(90)
                .riskLevel("CRITICAL")
                .costUsd(0.00)
                .timestamp(LocalDateTime.now())
                .build();

        auditEventRepository.saveAll(List.of(e1, e2));

        assertEquals(2L, auditEventRepository.countAll());
        assertEquals(1L, auditEventRepository.countBlocked());
        assertTrue(auditEventRepository.totalCost() >= 0.05);

        List<Object[]> riskCounts = auditEventRepository.countByRiskLevel();
        assertFalse(riskCounts.isEmpty());
    }
}
