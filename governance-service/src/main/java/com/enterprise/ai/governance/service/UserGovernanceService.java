package com.enterprise.ai.governance.service;

import com.enterprise.ai.governance.dto.UserGovernanceDto;
import com.enterprise.ai.governance.entity.UserGovernanceEntity;
import com.enterprise.ai.governance.repository.UserGovernanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserGovernanceService {

    private static final Logger log = LoggerFactory.getLogger(UserGovernanceService.class);

    private final UserGovernanceRepository userRepository;
    private final NotificationService notificationService;

    public UserGovernanceService(
            UserGovernanceRepository userRepository,
            NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<UserGovernanceDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserGovernanceDto getById(Long id) {
        return userRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("User governance profile not found for id: " + id));
    }

    @Transactional(readOnly = true)
    public UserGovernanceDto getByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .map(this::toDto)
                .orElse(null);
    }

    @Transactional
    public UserGovernanceDto saveUser(UserGovernanceDto dto) {
        UserGovernanceEntity entity;
        if (dto.getId() != null) {
            entity = userRepository.findById(dto.getId()).orElse(toEntity(dto));
        } else {
            entity = userRepository.findByUserId(dto.getUserId()).orElse(toEntity(dto));
        }

        entity.setUserId(dto.getUserId());
        entity.setUserRole(dto.getUserRole());
        entity.setDepartment(dto.getDepartment());
        entity.setCustomDailyTokenLimit(dto.getCustomDailyTokenLimit());
        entity.setIsRestricted(dto.getIsRestricted());
        entity.setRestrictionReason(dto.getRestrictionReason());
        entity.setCanBypassReview(dto.getCanBypassReview());
        entity.setUpdatedAt(LocalDateTime.now());

        return toDto(userRepository.save(entity));
    }

    @Transactional
    public UserGovernanceDto toggleRestriction(Long id, boolean restricted, String reason) {
        UserGovernanceEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User governance not found with id: " + id));

        user.setIsRestricted(restricted);
        user.setRestrictionReason(restricted ? reason : null);
        user.setUpdatedAt(LocalDateTime.now());

        UserGovernanceEntity saved = userRepository.save(user);

        notificationService.sendAlert("User Governance Restricted",
                "User '" + saved.getUserId() + "' restriction status changed to " + restricted + " (Reason: " + reason + ")",
                restricted ? "CRITICAL" : "INFO", "SECURITY_LEAD");

        return toDto(saved);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public UserGovernanceDto toDto(UserGovernanceEntity e) {
        return UserGovernanceDto.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .userRole(e.getUserRole())
                .department(e.getDepartment())
                .customDailyTokenLimit(e.getCustomDailyTokenLimit())
                .isRestricted(e.getIsRestricted())
                .restrictionReason(e.getRestrictionReason())
                .canBypassReview(e.getCanBypassReview())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public UserGovernanceEntity toEntity(UserGovernanceDto d) {
        return UserGovernanceEntity.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .userRole(d.getUserRole() != null ? d.getUserRole() : "ENGINEER")
                .department(d.getDepartment() != null ? d.getDepartment() : "Engineering")
                .customDailyTokenLimit(d.getCustomDailyTokenLimit())
                .isRestricted(d.getIsRestricted() != null ? d.getIsRestricted() : false)
                .restrictionReason(d.getRestrictionReason())
                .canBypassReview(d.getCanBypassReview() != null ? d.getCanBypassReview() : false)
                .build();
    }
}
