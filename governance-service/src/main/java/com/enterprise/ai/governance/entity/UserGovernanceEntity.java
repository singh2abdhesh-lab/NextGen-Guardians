package com.enterprise.ai.governance.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gov_user_governance", indexes = {
        @Index(name = "idx_usr_id", columnList = "user_id", unique = true),
        @Index(name = "idx_usr_dept", columnList = "department"),
        @Index(name = "idx_usr_role", columnList = "user_role")
})
public class UserGovernanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "user_role", nullable = false, length = 50)
    private String userRole;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(name = "custom_daily_token_limit")
    private Long customDailyTokenLimit;

    @Column(name = "is_restricted")
    private Boolean isRestricted;

    @Column(name = "restriction_reason", length = 500)
    private String restrictionReason;

    @Column(name = "can_bypass_review")
    private Boolean canBypassReview;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserGovernanceEntity() {}

    public UserGovernanceEntity(Long id, String userId, String userRole, String department,
                                Long customDailyTokenLimit, Boolean isRestricted, String restrictionReason,
                                Boolean canBypassReview, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userRole = userRole;
        this.department = department;
        this.customDailyTokenLimit = customDailyTokenLimit;
        this.isRestricted = isRestricted;
        this.restrictionReason = restrictionReason;
        this.canBypassReview = canBypassReview;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void onPrePersist() {
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (isRestricted == null) isRestricted = false;
        if (canBypassReview == null) canBypassReview = false;
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public Long getCustomDailyTokenLimit() { return customDailyTokenLimit; }
    public void setCustomDailyTokenLimit(Long customDailyTokenLimit) { this.customDailyTokenLimit = customDailyTokenLimit; }
    public Boolean getIsRestricted() { return isRestricted; }
    public void setIsRestricted(Boolean isRestricted) { this.isRestricted = isRestricted; }
    public String getRestrictionReason() { return restrictionReason; }
    public void setRestrictionReason(String restrictionReason) { this.restrictionReason = restrictionReason; }
    public Boolean getCanBypassReview() { return canBypassReview; }
    public void setCanBypassReview(Boolean canBypassReview) { this.canBypassReview = canBypassReview; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static UserGovernanceEntityBuilder builder() {
        return new UserGovernanceEntityBuilder();
    }

    public static class UserGovernanceEntityBuilder {
        private Long id;
        private String userId;
        private String userRole;
        private String department;
        private Long customDailyTokenLimit;
        private Boolean isRestricted;
        private String restrictionReason;
        private Boolean canBypassReview;
        private LocalDateTime updatedAt;

        public UserGovernanceEntityBuilder id(Long id) { this.id = id; return this; }
        public UserGovernanceEntityBuilder userId(String userId) { this.userId = userId; return this; }
        public UserGovernanceEntityBuilder userRole(String userRole) { this.userRole = userRole; return this; }
        public UserGovernanceEntityBuilder department(String department) { this.department = department; return this; }
        public UserGovernanceEntityBuilder customDailyTokenLimit(Long customDailyTokenLimit) { this.customDailyTokenLimit = customDailyTokenLimit; return this; }
        public UserGovernanceEntityBuilder isRestricted(Boolean isRestricted) { this.isRestricted = isRestricted; return this; }
        public UserGovernanceEntityBuilder restrictionReason(String restrictionReason) { this.restrictionReason = restrictionReason; return this; }
        public UserGovernanceEntityBuilder canBypassReview(Boolean canBypassReview) { this.canBypassReview = canBypassReview; return this; }
        public UserGovernanceEntityBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public UserGovernanceEntity build() {
            return new UserGovernanceEntity(id, userId, userRole, department, customDailyTokenLimit, isRestricted, restrictionReason, canBypassReview, updatedAt);
        }
    }
}
