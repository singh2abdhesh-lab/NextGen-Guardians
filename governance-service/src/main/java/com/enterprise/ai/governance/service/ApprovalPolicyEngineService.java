package com.enterprise.ai.governance.service;

import com.enterprise.ai.governance.dto.ApprovalPolicyDto;
import com.enterprise.ai.governance.entity.ApprovalPolicyEntity;
import com.enterprise.ai.governance.repository.ApprovalPolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApprovalPolicyEngineService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalPolicyEngineService.class);

    private final ApprovalPolicyRepository approvalPolicyRepository;

    public ApprovalPolicyEngineService(ApprovalPolicyRepository approvalPolicyRepository) {
        this.approvalPolicyRepository = approvalPolicyRepository;
    }

    @Transactional(readOnly = true)
    public List<ApprovalPolicyDto> getAllApprovalPolicies() {
        return approvalPolicyRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApprovalPolicyDto getById(Long id) {
        return approvalPolicyRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Approval policy not found for id: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<ApprovalPolicyDto> findEffectivePolicy(String triggerCategory) {
        return approvalPolicyRepository.findByTriggerCategory(triggerCategory).stream()
                .filter(ApprovalPolicyEntity::getEnabled)
                .findFirst()
                .map(this::toDto);
    }

    @Transactional
    public ApprovalPolicyDto saveApprovalPolicy(ApprovalPolicyDto dto) {
        ApprovalPolicyEntity entity;
        if (dto.getId() != null) {
            entity = approvalPolicyRepository.findById(dto.getId()).orElse(toEntity(dto));
        } else {
            entity = toEntity(dto);
        }

        entity.setPolicyKey(dto.getPolicyKey());
        entity.setName(dto.getName());
        entity.setTriggerCategory(dto.getTriggerCategory());
        entity.setWorkflowType(dto.getWorkflowType());
        entity.setPrimaryApproverRole(dto.getPrimaryApproverRole());
        entity.setEscalationApproverRole(dto.getEscalationApproverRole());
        entity.setAutoExpiryHours(dto.getAutoExpiryHours());
        entity.setDelegatedTo(dto.getDelegatedTo());
        entity.setDescription(dto.getDescription());
        entity.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        entity.setUpdatedAt(LocalDateTime.now());

        return toDto(approvalPolicyRepository.save(entity));
    }

    @Transactional
    public ApprovalPolicyDto togglePolicy(Long id, boolean enabled) {
        ApprovalPolicyEntity policy = approvalPolicyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Approval policy not found with id: " + id));
        policy.setEnabled(enabled);
        policy.setUpdatedAt(LocalDateTime.now());
        return toDto(approvalPolicyRepository.save(policy));
    }

    @Transactional
    public void deletePolicy(Long id) {
        approvalPolicyRepository.deleteById(id);
    }

    public ApprovalPolicyDto toDto(ApprovalPolicyEntity e) {
        return ApprovalPolicyDto.builder()
                .id(e.getId())
                .policyKey(e.getPolicyKey())
                .name(e.getName())
                .triggerCategory(e.getTriggerCategory())
                .workflowType(e.getWorkflowType())
                .primaryApproverRole(e.getPrimaryApproverRole())
                .escalationApproverRole(e.getEscalationApproverRole())
                .autoExpiryHours(e.getAutoExpiryHours())
                .delegatedTo(e.getDelegatedTo())
                .description(e.getDescription())
                .enabled(e.getEnabled())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public ApprovalPolicyEntity toEntity(ApprovalPolicyDto d) {
        return ApprovalPolicyEntity.builder()
                .id(d.getId())
                .policyKey(d.getPolicyKey())
                .name(d.getName())
                .triggerCategory(d.getTriggerCategory())
                .workflowType(d.getWorkflowType() != null ? d.getWorkflowType() : "SINGLE_APPROVAL")
                .primaryApproverRole(d.getPrimaryApproverRole())
                .escalationApproverRole(d.getEscalationApproverRole())
                .autoExpiryHours(d.getAutoExpiryHours() != null ? d.getAutoExpiryHours() : 24)
                .delegatedTo(d.getDelegatedTo())
                .description(d.getDescription())
                .enabled(d.getEnabled() != null ? d.getEnabled() : true)
                .build();
    }
}
