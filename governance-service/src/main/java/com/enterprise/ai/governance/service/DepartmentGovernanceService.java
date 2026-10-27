package com.enterprise.ai.governance.service;

import com.enterprise.ai.governance.dto.DepartmentGovernanceDto;
import com.enterprise.ai.governance.entity.DepartmentGovernanceEntity;
import com.enterprise.ai.governance.repository.DepartmentGovernanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class DepartmentGovernanceService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentGovernanceService.class);

    private final DepartmentGovernanceRepository departmentRepository;
    private final NotificationService notificationService;

    public DepartmentGovernanceService(
            DepartmentGovernanceRepository departmentRepository,
            NotificationService notificationService) {
        this.departmentRepository = departmentRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<DepartmentGovernanceDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentGovernanceDto getById(Long id) {
        return departmentRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Department governance not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public DepartmentGovernanceDto getByName(String name) {
        return departmentRepository.findByDepartmentNameIgnoreCase(name)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Department not found with name: " + name));
    }

    @Transactional
    public DepartmentGovernanceDto saveDepartment(DepartmentGovernanceDto dto) {
        DepartmentGovernanceEntity entity;
        if (dto.getId() != null) {
            entity = departmentRepository.findById(dto.getId())
                    .orElse(toEntity(dto));
        } else {
            entity = toEntity(dto);
        }

        entity.setDepartmentName(dto.getDepartmentName());
        entity.setDepartmentCode(dto.getDepartmentCode());
        entity.setDailyTokenLimit(dto.getDailyTokenLimit());
        entity.setMonthlyCostLimitUsd(dto.getMonthlyCostLimitUsd());
        entity.setAllowedModels(dto.getAllowedModels());
        entity.setBlockedModels(dto.getBlockedModels());
        entity.setAllowedPromptCategories(dto.getAllowedPromptCategories());
        entity.setRestrictedCategories(dto.getRestrictedCategories());
        entity.setBusinessHoursOnly(dto.getBusinessHoursOnly());
        entity.setBusinessHoursStart(dto.getBusinessHoursStart());
        entity.setBusinessHoursEnd(dto.getBusinessHoursEnd());
        entity.setUpdatedAt(LocalDateTime.now());

        return toDto(departmentRepository.save(entity));
    }

    @Transactional
    public DepartmentGovernanceDto toggleEmergencyOverride(Long id, boolean active, String reason, int durationHours) {
        DepartmentGovernanceEntity existing = departmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Department not found with id: " + id));

        existing.setEmergencyOverrideActive(active);
        existing.setEmergencyOverrideReason(active ? reason : null);
        existing.setEmergencyOverrideUntil(active ? LocalDateTime.now().plusHours(durationHours > 0 ? durationHours : 4) : null);
        existing.setUpdatedAt(LocalDateTime.now());

        DepartmentGovernanceEntity updated = departmentRepository.save(existing);

        notificationService.sendAlert("Emergency Override Toggled",
                "Department " + updated.getDepartmentName() + " emergency override set to " + active + " (Reason: " + reason + ")",
                active ? "CRITICAL" : "INFO", "SECURITY_LEAD");

        return toDto(updated);
    }

    @Transactional
    public void recordSpend(String departmentName, double costUsd) {
        departmentRepository.findByDepartmentNameIgnoreCase(departmentName).ifPresent(dept -> {
            double newSpend = (dept.getCurrentMonthlySpendUsd() != null ? dept.getCurrentMonthlySpendUsd() : 0.0) + costUsd;
            dept.setCurrentMonthlySpendUsd(newSpend);
            dept.setUpdatedAt(LocalDateTime.now());
            departmentRepository.save(dept);

            // Check budget alert
            if (dept.getMonthlyCostLimitUsd() != null && dept.getMonthlyCostLimitUsd() > 0) {
                double pct = (newSpend / dept.getMonthlyCostLimitUsd()) * 100;
                if (pct >= 100) {
                    notificationService.sendAlert("Budget Ceiling Exceeded",
                            "Department " + dept.getDepartmentName() + " has reached 100% of its monthly budget ($" + newSpend + " / $" + dept.getMonthlyCostLimitUsd() + ")",
                            "CRITICAL", "FINANCE");
                } else if (pct >= 80) {
                    notificationService.sendAlert("Budget Warning (80%)",
                            "Department " + dept.getDepartmentName() + " has utilized " + Math.round(pct) + "% of its monthly AI quota",
                            "WARN", "FINANCE");
                }
            }
        });
    }

    public boolean isWithinBusinessHours(DepartmentGovernanceDto dept) {
        if (!Boolean.TRUE.equals(dept.getBusinessHoursOnly())) {
            return true;
        }
        try {
            LocalTime now = LocalTime.now();
            LocalTime start = LocalTime.parse(dept.getBusinessHoursStart() != null ? dept.getBusinessHoursStart() : "08:00");
            LocalTime end = LocalTime.parse(dept.getBusinessHoursEnd() != null ? dept.getBusinessHoursEnd() : "18:00");
            return !now.isBefore(start) && !now.isAfter(end);
        } catch (Exception e) {
            return true;
        }
    }

    public DepartmentGovernanceDto toDto(DepartmentGovernanceEntity e) {
        return DepartmentGovernanceDto.builder()
                .id(e.getId())
                .departmentName(e.getDepartmentName())
                .departmentCode(e.getDepartmentCode())
                .dailyTokenLimit(e.getDailyTokenLimit())
                .monthlyCostLimitUsd(e.getMonthlyCostLimitUsd())
                .currentMonthlySpendUsd(e.getCurrentMonthlySpendUsd())
                .allowedModels(e.getAllowedModels())
                .blockedModels(e.getBlockedModels())
                .allowedPromptCategories(e.getAllowedPromptCategories())
                .restrictedCategories(e.getRestrictedCategories())
                .businessHoursOnly(e.getBusinessHoursOnly())
                .businessHoursStart(e.getBusinessHoursStart())
                .businessHoursEnd(e.getBusinessHoursEnd())
                .emergencyOverrideActive(e.getEmergencyOverrideActive())
                .emergencyOverrideReason(e.getEmergencyOverrideReason())
                .emergencyOverrideUntil(e.getEmergencyOverrideUntil())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public DepartmentGovernanceEntity toEntity(DepartmentGovernanceDto d) {
        return DepartmentGovernanceEntity.builder()
                .id(d.getId())
                .departmentName(d.getDepartmentName())
                .departmentCode(d.getDepartmentCode())
                .dailyTokenLimit(d.getDailyTokenLimit() != null ? d.getDailyTokenLimit() : 5_000_000L)
                .monthlyCostLimitUsd(d.getMonthlyCostLimitUsd() != null ? d.getMonthlyCostLimitUsd() : 2500.0)
                .currentMonthlySpendUsd(d.getCurrentMonthlySpendUsd() != null ? d.getCurrentMonthlySpendUsd() : 0.0)
                .allowedModels(d.getAllowedModels())
                .blockedModels(d.getBlockedModels())
                .allowedPromptCategories(d.getAllowedPromptCategories())
                .restrictedCategories(d.getRestrictedCategories())
                .businessHoursOnly(d.getBusinessHoursOnly() != null ? d.getBusinessHoursOnly() : false)
                .businessHoursStart(d.getBusinessHoursStart() != null ? d.getBusinessHoursStart() : "08:00")
                .businessHoursEnd(d.getBusinessHoursEnd() != null ? d.getBusinessHoursEnd() : "18:00")
                .emergencyOverrideActive(d.getEmergencyOverrideActive() != null ? d.getEmergencyOverrideActive() : false)
                .emergencyOverrideReason(d.getEmergencyOverrideReason())
                .emergencyOverrideUntil(d.getEmergencyOverrideUntil())
                .build();
    }
}
