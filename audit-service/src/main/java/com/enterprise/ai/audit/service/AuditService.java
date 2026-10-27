package com.enterprise.ai.audit.service;

import com.enterprise.ai.audit.entity.AuditEventEntity;
import com.enterprise.ai.audit.entity.ApprovalRecordEntity;
import com.enterprise.ai.audit.repository.AuditEventRepository;
import com.enterprise.ai.common.dto.ApprovalRequest;
import com.enterprise.ai.common.dto.AuditEvent;
import com.enterprise.ai.common.dto.DashboardOverview;
import com.enterprise.ai.common.enums.ApprovalStatus;
import com.enterprise.ai.common.exception.AiGuardianException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Enterprise audit service orchestrating forensic event persistence,
 * human approval workflows, compliance audits, and executive dashboard metrics.
 */
@Service
@Transactional
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository repository) {
        this.repository = repository;
    }

    /**
     * Persists an incoming audit event to the audit store.
     */
    public AuditEventEntity saveEvent(AuditEvent event) {
        if (event == null) {
            throw new AiGuardianException("Audit event payload cannot be null");
        }

        String initialApprovalStatus = "NONE";
        if (Boolean.TRUE.equals(event.getApprovalRequired())) {
            initialApprovalStatus = "PENDING";
        }
        if (event.getApprovalStatus() != null && !event.getApprovalStatus().isBlank()) {
            initialApprovalStatus = event.getApprovalStatus();
        }

        AuditEventEntity entity = AuditEventEntity.builder()
                .requestId(event.getRequestId())
                .userId(event.getUserId())
                .userRole(event.getUserRole())
                .model(event.getModel())
                .promptCategory(event.getPromptCategory())
                .action(event.getAction())
                .riskScore(event.getRiskScore())
                .riskLevel(event.getRiskLevel())
                .tokensIn(event.getTokensIn())
                .tokensOut(event.getTokensOut())
                .costUsd(event.getCostUsd())
                .qualityVerdict(event.getQualityVerdict())
                .hallucinationScore(event.getHallucinationScore())
                .approvalRequired(event.getApprovalRequired())
                .violationsSummary(event.getViolationsSummary())
                .blockReason(event.getBlockReason())
                .prompt(event.getPrompt())
                .timestamp(event.getTimestamp() != null ? event.getTimestamp() : LocalDateTime.now())
                .approvalStatus(initialApprovalStatus)
                .approver(event.getApprover())
                .approvalTimestamp(event.getApprovalTimestamp())
                .build();

        AuditEventEntity saved = repository.save(entity);
        log.debug("Persisted audit event id={} for requestId={}", saved.getId(), saved.getRequestId());
        return saved;
    }

    /**
     * Batch persists multiple audit events in a single transactional batch.
     */
    public List<AuditEvent> saveEventsBatch(List<AuditEvent> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }
        return events.stream()
                .map(this::saveEvent)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves recent events up to specified limit.
     */
    @Transactional(readOnly = true)
    public List<AuditEvent> listEvents(int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(1, Math.min(limit, 1000)));
        return repository.findAllByOrderByTimestampDesc(pageable)
                .getContent()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves paginated audit events.
     */
    @Transactional(readOnly = true)
    public Page<AuditEvent> listEventsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)));
        return repository.findAllByOrderByTimestampDesc(pageable).map(this::toDto);
    }

    /**
     * Retrieves specific event by internal database ID.
     */
    @Transactional(readOnly = true)
    public AuditEvent getEventById(Long id) {
        return repository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new AiGuardianException("Audit event record not found for id: " + id));
    }

    /**
     * Retrieves specific event by unique request correlation ID.
     */
    @Transactional(readOnly = true)
    public AuditEvent getEventByRequestId(String requestId) {
        return repository.findByRequestId(requestId)
                .map(this::toDto)
                .orElseThrow(() -> new AiGuardianException("Audit event record not found for requestId: " + requestId));
    }

    /**
     * Retrieves high and critical risk alerts.
     */
    @Transactional(readOnly = true)
    public List<AuditEvent> getAlerts() {
        return repository.findByRiskLevelInOrderByTimestampDesc(List.of("HIGH", "CRITICAL"))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves audit events for a specific user identifier.
     */
    @Transactional(readOnly = true)
    public List<AuditEvent> getByUser(String userId) {
        return repository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Approval Workflow
    // =========================================================================

    /**
     * Approves a flagged audit event item.
     */
    public AuditEvent approve(Long id, String approver, String reason) {
        AuditEventEntity entity = repository.findById(id)
                .orElseThrow(() -> new AiGuardianException("Audit event not found for approval: " + id));

        entity.setApprovalStatus(ApprovalStatus.APPROVED.name());
        entity.setApprover(approver != null && !approver.isBlank() ? approver : "security-admin");
        entity.setApprovalTimestamp(LocalDateTime.now());
        entity.setApprovalReason(reason != null ? reason : "Authorized by security auditor");

        ApprovalRecordEntity record = entity.getApprovalRecord();
        if (record == null) {
            record = ApprovalRecordEntity.builder()
                    .decision(ApprovalStatus.APPROVED.name())
                    .approver(entity.getApprover())
                    .reason(entity.getApprovalReason())
                    .reviewedAt(entity.getApprovalTimestamp())
                    .build();
            entity.setApprovalRecord(record);
        } else {
            record.setDecision(ApprovalStatus.APPROVED.name());
            record.setApprover(entity.getApprover());
            record.setReason(entity.getApprovalReason());
            record.setReviewedAt(entity.getApprovalTimestamp());
        }

        AuditEventEntity updated = repository.save(entity);
        log.info("Audit event id={} APPROVED by approver={}", id, entity.getApprover());
        return toDto(updated);
    }

    /**
     * Rejects a flagged audit event item.
     */
    public AuditEvent reject(Long id, String approver, String reason) {
        AuditEventEntity entity = repository.findById(id)
                .orElseThrow(() -> new AiGuardianException("Audit event not found for rejection: " + id));

        entity.setApprovalStatus(ApprovalStatus.REJECTED.name());
        entity.setApprover(approver != null && !approver.isBlank() ? approver : "security-admin");
        entity.setApprovalTimestamp(LocalDateTime.now());
        entity.setApprovalReason(reason != null ? reason : "Rejected by security auditor");

        ApprovalRecordEntity record = entity.getApprovalRecord();
        if (record == null) {
            record = ApprovalRecordEntity.builder()
                    .decision(ApprovalStatus.REJECTED.name())
                    .approver(entity.getApprover())
                    .reason(entity.getApprovalReason())
                    .reviewedAt(entity.getApprovalTimestamp())
                    .build();
            entity.setApprovalRecord(record);
        } else {
            record.setDecision(ApprovalStatus.REJECTED.name());
            record.setApprover(entity.getApprover());
            record.setReason(entity.getApprovalReason());
            record.setReviewedAt(entity.getApprovalTimestamp());
        }

        AuditEventEntity updated = repository.save(entity);
        log.info("Audit event id={} REJECTED by approver={}", id, entity.getApprover());
        return toDto(updated);
    }

    /**
     * Processes standard ApprovalRequest entity.
     */
    public AuditEvent processApproval(ApprovalRequest request) {
        if (request == null) {
            throw new AiGuardianException("Approval request cannot be null");
        }
        if (request.getDecision() == ApprovalStatus.APPROVED) {
            return approve(request.getEventId(), request.getApprover(), request.getReason());
        } else if (request.getDecision() == ApprovalStatus.REJECTED) {
            return reject(request.getEventId(), request.getApprover(), request.getReason());
        } else {
            throw new AiGuardianException("Invalid approval decision: " + request.getDecision());
        }
    }

    /**
     * Retrieves all events awaiting human review.
     */
    @Transactional(readOnly = true)
    public List<AuditEvent> getPendingApprovals() {
        return repository.findByApprovalStatusOrderByTimestampDesc(ApprovalStatus.PENDING.name())
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // Dashboard & Metrics
    // =========================================================================

    /**
     * Renders executive overview of operational metrics.
     */
    @Transactional(readOnly = true)
    public DashboardOverview getOverview() {
        Map<String, Long> byRisk = new LinkedHashMap<>();
        for (Object[] row : repository.countByRiskLevel()) {
            if (row[0] != null && row[1] != null) {
                byRisk.put(row[0].toString(), (Long) row[1]);
            }
        }

        Map<String, Long> byAction = new LinkedHashMap<>();
        for (Object[] row : repository.countByAction()) {
            if (row[0] != null && row[1] != null) {
                byAction.put(row[0].toString(), (Long) row[1]);
            }
        }

        return DashboardOverview.builder()
                .totalRequests(repository.countAll())
                .blockedCount(repository.countBlocked())
                .pendingApprovals(repository.countPendingApprovals())
                .totalCost(Math.round(repository.totalCost() * 100.0) / 100.0)
                .eventsByRiskLevel(byRisk)
                .eventsByAction(byAction)
                .build();
    }

    /**
     * Computes usage and cost aggregations by LLM model.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> usageByModel(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : repository.usageByModel(since)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("model", row[0] != null ? row[0] : "unknown");
            m.put("requests", row[1] != null ? row[1] : 0L);
            m.put("costUsd", row[2] != null ? Math.round(((Number) row[2]).doubleValue() * 1000.0) / 1000.0 : 0.0);
            result.add(m);
        }
        return result;
    }

    /**
     * Computes usage frequency by user.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> usageByUser(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : repository.usageByUser(since)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", row[0] != null ? row[0] : "anonymous");
            m.put("requests", row[1] != null ? row[1] : 0L);
            result.add(m);
        }
        return result;
    }

    /**
     * Retrieves distribution by prompt category.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getPromptCategoryDistribution() {
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (Object[] row : repository.countByPromptCategory()) {
            if (row[0] != null && row[1] != null) {
                distribution.put(row[0].toString(), (Long) row[1]);
            }
        }
        return distribution;
    }

    // =========================================================================
    // Reporting & Compliance
    // =========================================================================

    /**
     * Generates comprehensive compliance report covering regulatory frameworks.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getComplianceReport(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<AuditEventEntity> events = repository.findEventsSince(since);

        long total = events.size();
        long blocked = events.stream().filter(e -> "BLOCK".equalsIgnoreCase(e.getAction())).count();
        long allowed = events.stream().filter(e -> "ALLOW".equalsIgnoreCase(e.getAction())).count();
        long reviewed = events.stream().filter(e -> "REVIEW".equalsIgnoreCase(e.getAction())).count();

        Map<String, Long> byRisk = events.stream()
                .filter(e -> e.getRiskLevel() != null)
                .collect(Collectors.groupingBy(AuditEventEntity::getRiskLevel, Collectors.counting()));

        List<Map<String, Object>> violations = events.stream()
                .filter(e -> e.getViolationsSummary() != null && !e.getViolationsSummary().isBlank())
                .map(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("requestId", e.getRequestId());
                    item.put("userId", e.getUserId());
                    item.put("action", e.getAction());
                    item.put("riskLevel", e.getRiskLevel());
                    item.put("violationsSummary", e.getViolationsSummary());
                    item.put("timestamp", e.getTimestamp() != null ? e.getTimestamp().toString() : null);
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("reportPeriodDays", days);
        report.put("generatedAt", LocalDateTime.now().toString());
        report.put("totalEvents", total);
        report.put("allowedCount", allowed);
        report.put("blockedCount", blocked);
        report.put("reviewedCount", reviewed);
        report.put("complianceRate", total > 0 ? Math.round(((double) (total - blocked) / total) * 10000.0) / 100.0 : 100.0);
        report.put("eventsByRiskLevel", byRisk);
        report.put("violationsLog", violations);
        report.put("frameworksAssessed", List.of("NIST AI RMF 1.0", "EU AI Act (2024)", "HIPAA Security Rule", "PCI-DSS v4.0", "SOC 2 Type II"));
        return report;
    }

    /**
     * Generates a forensic CSV export of audit events over the designated period.
     */
    @Transactional(readOnly = true)
    public String generateCsvExport(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<AuditEventEntity> events = repository.findEventsSince(since);

        StringBuilder sb = new StringBuilder();
        sb.append("ID,REQUEST_ID,USER_ID,USER_ROLE,MODEL,ACTION,RISK_SCORE,RISK_LEVEL,TOKENS_IN,TOKENS_OUT,COST_USD,QUALITY_VERDICT,APPROVAL_STATUS,TIMESTAMP\n");

        for (AuditEventEntity e : events) {
            sb.append(e.getId()).append(",")
                    .append(sanitizeCsv(e.getRequestId())).append(",")
                    .append(sanitizeCsv(e.getUserId())).append(",")
                    .append(sanitizeCsv(e.getUserRole())).append(",")
                    .append(sanitizeCsv(e.getModel())).append(",")
                    .append(sanitizeCsv(e.getAction())).append(",")
                    .append(e.getRiskScore() != null ? e.getRiskScore() : 0).append(",")
                    .append(sanitizeCsv(e.getRiskLevel())).append(",")
                    .append(e.getTokensIn() != null ? e.getTokensIn() : 0).append(",")
                    .append(e.getTokensOut() != null ? e.getTokensOut() : 0).append(",")
                    .append(e.getCostUsd() != null ? e.getCostUsd() : 0.0).append(",")
                    .append(sanitizeCsv(e.getQualityVerdict())).append(",")
                    .append(sanitizeCsv(e.getApprovalStatus())).append(",")
                    .append(e.getTimestamp()).append("\n");
        }

        return sb.toString();
    }

    private String sanitizeCsv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    public AuditEvent toDto(AuditEventEntity e) {
        if (e == null) {
            return null;
        }
        return AuditEvent.builder()
                .id(e.getId())
                .requestId(e.getRequestId())
                .userId(e.getUserId())
                .userRole(e.getUserRole())
                .model(e.getModel())
                .promptCategory(e.getPromptCategory())
                .action(e.getAction())
                .riskScore(e.getRiskScore())
                .riskLevel(e.getRiskLevel())
                .tokensIn(e.getTokensIn())
                .tokensOut(e.getTokensOut())
                .costUsd(e.getCostUsd())
                .qualityVerdict(e.getQualityVerdict())
                .hallucinationScore(e.getHallucinationScore())
                .approvalRequired(e.getApprovalRequired())
                .violationsSummary(e.getViolationsSummary())
                .blockReason(e.getBlockReason())
                .prompt(e.getPrompt())
                .timestamp(e.getTimestamp())
                .approvalStatus(e.getApprovalStatus())
                .approver(e.getApprover())
                .approvalTimestamp(e.getApprovalTimestamp())
                .build();
    }
}
