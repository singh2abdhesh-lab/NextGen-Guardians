package com.enterprise.ai.governance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Schema(description = "User AI Governance Profile")
public class UserGovernanceDto {

    private Long id;

    @NotBlank(message = "User ID cannot be blank")
    @Schema(description = "Corporate user identifier or email", example = "alex.dev@enterprise.ai")
    private String userId;

    @NotBlank(message = "User role cannot be blank")
    @Schema(description = "Role title", example = "ENGINEER")
    private String userRole;

    @NotBlank(message = "Department cannot be blank")
    @Schema(description = "Assigned department", example = "Engineering")
    private String department;

    @Schema(description = "Optional user token override", example = "1000000")
    private Long customDailyTokenLimit;

    @Schema(description = "Account AI access restriction status")
    private Boolean isRestricted;

    private String restrictionReason;
    private Boolean canBypassReview;
    private LocalDateTime updatedAt;

    public UserGovernanceDto() {}

    public UserGovernanceDto(Long id, String userId, String userRole, String department,
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

    public static UserGovernanceDtoBuilder builder() {
        return new UserGovernanceDtoBuilder();
    }

    public static class UserGovernanceDtoBuilder {
        private Long id;
        private String userId;
        private String userRole;
        private String department;
        private Long customDailyTokenLimit;
        private Boolean isRestricted;
        private String restrictionReason;
        private Boolean canBypassReview;
        private LocalDateTime updatedAt;

        public UserGovernanceDtoBuilder id(Long id) { this.id = id; return this; }
        public UserGovernanceDtoBuilder userId(String userId) { this.userId = userId; return this; }
        public UserGovernanceDtoBuilder userRole(String userRole) { this.userRole = userRole; return this; }
        public UserGovernanceDtoBuilder department(String department) { this.department = department; return this; }
        public UserGovernanceDtoBuilder customDailyTokenLimit(Long customDailyTokenLimit) { this.customDailyTokenLimit = customDailyTokenLimit; return this; }
        public UserGovernanceDtoBuilder isRestricted(Boolean isRestricted) { this.isRestricted = isRestricted; return this; }
        public UserGovernanceDtoBuilder restrictionReason(String restrictionReason) { this.restrictionReason = restrictionReason; return this; }
        public UserGovernanceDtoBuilder canBypassReview(Boolean canBypassReview) { this.canBypassReview = canBypassReview; return this; }
        public UserGovernanceDtoBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public UserGovernanceDto build() {
            return new UserGovernanceDto(id, userId, userRole, department, customDailyTokenLimit, isRestricted, restrictionReason, canBypassReview, updatedAt);
        }
    }
}
