package com.enterprise.ai.governance.service;

import com.enterprise.ai.common.util.JsonUtils;
import com.enterprise.ai.governance.dto.AiPolicyDto;
import com.enterprise.ai.governance.dto.PolicyAuditHistoryDto;
import com.enterprise.ai.governance.entity.AiPolicyEntity;
import com.enterprise.ai.governance.entity.PolicyAuditHistoryEntity;
import com.enterprise.ai.governance.repository.AiPolicyRepository;
import com.enterprise.ai.governance.repository.PolicyAuditHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class PolicyManagementService {

    private static final Logger log = LoggerFactory.getLogger(PolicyManagementService.class);

    private final AiPolicyRepository policyRepository;
    private final PolicyAuditHistoryRepository auditHistoryRepository;
    private final NotificationService notificationService;

    public PolicyManagementService(
            AiPolicyRepository policyRepository,
            PolicyAuditHistoryRepository auditHistoryRepository,
            NotificationService notificationService) {
        this.policyRepository = policyRepository;
        this.auditHistoryRepository = auditHistoryRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<AiPolicyDto> getAllPolicies() {
        return policyRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AiPolicyDto> getActivePolicies() {
        return policyRepository.findByEnabledTrueOrderByPriorityAsc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AiPolicyDto getPolicyById(Long id) {
        return policyRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Policy not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public AiPolicyDto getPolicyByKey(String key) {
        return policyRepository.findByPolicyKey(key)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Policy not found with key: " + key));
    }

    @Transactional
    public AiPolicyDto createPolicy(AiPolicyDto dto, String operator) {
        if (policyRepository.existsByPolicyKey(dto.getPolicyKey())) {
            throw new IllegalArgumentException("Policy with key '" + dto.getPolicyKey() + "' already exists");
        }

        AiPolicyEntity entity = toEntity(dto);
        entity.setVersion(1);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setCreatedBy(operator != null ? operator : "secops-admin");
        entity.setUpdatedBy(operator != null ? operator : "secops-admin");

        AiPolicyEntity saved = policyRepository.save(entity);

        recordAudit(saved.getId(), saved.getPolicyKey(), saved.getVersion(), "CREATE",
                operator != null ? operator : "secops-admin",
                "Created initial policy version", null, JsonUtils.toJson(saved));

        notificationService.sendAlert("New AI Policy Created",
                "Policy '" + saved.getName() + "' [" + saved.getPolicyKey() + "] registered by " + operator,
                "INFO", "SECURITY_LEAD");

        return toDto(saved);
    }

    @Transactional
    public AiPolicyDto updatePolicy(Long id, AiPolicyDto dto, String operator) {
        AiPolicyEntity existing = policyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Policy not found with id: " + id));

        String oldJson = JsonUtils.toJson(existing);

        existing.setName(dto.getName());
        existing.setCategory(dto.getCategory());
        existing.setDescription(dto.getDescription());
        existing.setPriority(dto.getPriority());
        existing.setStatus(dto.getStatus() != null ? dto.getStatus() : existing.getStatus());
        existing.setRulesJson(dto.getRulesJson());
        existing.setAction(dto.getAction());
        existing.setApprovalRequired(dto.getApprovalRequired());
        existing.setEffectiveDate(dto.getEffectiveDate());
        existing.setExpiryDate(dto.getExpiryDate());
        existing.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : existing.getEnabled());
        existing.setVersion(existing.getVersion() + 1); // Auto-increment version
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(operator != null ? operator : "secops-admin");

        AiPolicyEntity updated = policyRepository.save(existing);

        recordAudit(updated.getId(), updated.getPolicyKey(), updated.getVersion(), "UPDATE",
                operator != null ? operator : "secops-admin",
                "Updated policy definition to version " + updated.getVersion(),
                oldJson, JsonUtils.toJson(updated));

        return toDto(updated);
    }

    @Transactional
    public AiPolicyDto toggleEnabled(Long id, boolean enabled, String operator) {
        AiPolicyEntity existing = policyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Policy not found with id: " + id));

        String oldJson = JsonUtils.toJson(existing);
        existing.setEnabled(enabled);
        existing.setStatus(enabled ? "ACTIVE" : "SUSPENDED");
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setUpdatedBy(operator != null ? operator : "secops-admin");

        AiPolicyEntity updated = policyRepository.save(existing);

        String action = enabled ? "ENABLE" : "DISABLE";
        recordAudit(updated.getId(), updated.getPolicyKey(), updated.getVersion(), action,
                operator != null ? operator : "secops-admin",
                "Policy status toggled to " + (enabled ? "ENABLED" : "DISABLED"),
                oldJson, JsonUtils.toJson(updated));

        return toDto(updated);
    }

    @Transactional
    public AiPolicyDto rollbackPolicy(Long id, int targetVersion, String operator) {
        AiPolicyEntity current = policyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Policy not found with id: " + id));

        List<PolicyAuditHistoryEntity> historyList = auditHistoryRepository.findByPolicyIdOrderByChangedAtDesc(id);

        PolicyAuditHistoryEntity historical = historyList.stream()
                .filter(h -> h.getVersion() == targetVersion)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Audit snapshot not found for version " + targetVersion));

        String oldJson = JsonUtils.toJson(current);

        AiPolicyEntity snapshot = JsonUtils.fromJson(
                historical.getNewValueJson() != null ? historical.getNewValueJson() : historical.getOldValueJson(),
                AiPolicyEntity.class);

        if (snapshot != null) {
            current.setRulesJson(snapshot.getRulesJson());
            current.setAction(snapshot.getAction());
            current.setApprovalRequired(snapshot.getApprovalRequired());
            current.setPriority(snapshot.getPriority());
            current.setDescription(snapshot.getDescription());
        }

        current.setVersion(current.getVersion() + 1);
        current.setUpdatedAt(LocalDateTime.now());
        current.setUpdatedBy(operator != null ? operator : "secops-admin");

        AiPolicyEntity rolledBack = policyRepository.save(current);

        recordAudit(rolledBack.getId(), rolledBack.getPolicyKey(), rolledBack.getVersion(), "ROLLBACK",
                operator != null ? operator : "secops-admin",
                "Rolled back policy configuration from version " + targetVersion,
                oldJson, JsonUtils.toJson(rolledBack));

        notificationService.sendAlert("Policy Rollback Triggered",
                "Policy " + rolledBack.getPolicyKey() + " rolled back to version " + targetVersion + " by " + operator,
                "WARN", "SECURITY_LEAD");

        return toDto(rolledBack);
    }

    @Transactional
    public void deletePolicy(Long id, String operator) {
        AiPolicyEntity existing = policyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Policy not found with id: " + id));

        recordAudit(existing.getId(), existing.getPolicyKey(), existing.getVersion(), "DELETE",
                operator != null ? operator : "secops-admin",
                "Policy purged from platform",
                JsonUtils.toJson(existing), null);

        policyRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<PolicyAuditHistoryDto> getAuditHistory(Long policyId) {
        return auditHistoryRepository.findByPolicyIdOrderByChangedAtDesc(policyId).stream()
                .map(this::toAuditDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PolicyAuditHistoryDto> getRecentAuditHistory() {
        return auditHistoryRepository.findTop50ByOrderByChangedAtDesc().stream()
                .map(this::toAuditDto)
                .collect(Collectors.toList());
    }

    private void recordAudit(Long policyId, String key, int version, String action, String who,
                             String summary, String oldVal, String newVal) {
        PolicyAuditHistoryEntity audit = PolicyAuditHistoryEntity.builder()
                .policyId(policyId)
                .policyKey(key)
                .version(version)
                .actionType(action)
                .changedBy(who)
                .changedAt(LocalDateTime.now())
                .changeSummary(summary)
                .oldValueJson(oldVal)
                .newValueJson(newVal)
                .build();
        auditHistoryRepository.save(audit);
    }

    public AiPolicyDto toDto(AiPolicyEntity e) {
        return AiPolicyDto.builder()
                .id(e.getId())
                .policyKey(e.getPolicyKey())
                .name(e.getName())
                .category(e.getCategory())
                .description(e.getDescription())
                .priority(e.getPriority())
                .status(e.getStatus())
                .version(e.getVersion())
                .rulesJson(e.getRulesJson())
                .action(e.getAction())
                .approvalRequired(e.getApprovalRequired())
                .effectiveDate(e.getEffectiveDate())
                .expiryDate(e.getExpiryDate())
                .enabled(e.getEnabled())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .createdBy(e.getCreatedBy())
                .updatedBy(e.getUpdatedBy())
                .build();
    }

    public AiPolicyEntity toEntity(AiPolicyDto d) {
        return AiPolicyEntity.builder()
                .id(d.getId())
                .policyKey(d.getPolicyKey())
                .name(d.getName())
                .category(d.getCategory())
                .description(d.getDescription())
                .priority(d.getPriority() != null ? d.getPriority() : 50)
                .status(d.getStatus() != null ? d.getStatus() : "ACTIVE")
                .version(d.getVersion() != null ? d.getVersion() : 1)
                .rulesJson(d.getRulesJson())
                .action(d.getAction() != null ? d.getAction() : "REVIEW")
                .approvalRequired(d.getApprovalRequired() != null ? d.getApprovalRequired() : false)
                .effectiveDate(d.getEffectiveDate())
                .expiryDate(d.getExpiryDate())
                .enabled(d.getEnabled() != null ? d.getEnabled() : true)
                .build();
    }

    public PolicyAuditHistoryDto toAuditDto(PolicyAuditHistoryEntity a) {
        return PolicyAuditHistoryDto.builder()
                .id(a.getId())
                .policyId(a.getPolicyId())
                .policyKey(a.getPolicyKey())
                .version(a.getVersion())
                .actionType(a.getActionType())
                .changedBy(a.getChangedBy())
                .changedAt(a.getChangedAt())
                .changeSummary(a.getChangeSummary())
                .oldValueJson(a.getOldValueJson())
                .newValueJson(a.getNewValueJson())
                .build();
    }
}
