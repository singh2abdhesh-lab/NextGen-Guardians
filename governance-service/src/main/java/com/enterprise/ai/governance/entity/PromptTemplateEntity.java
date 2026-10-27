package com.enterprise.ai.governance.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gov_prompt_templates", indexes = {
        @Index(name = "idx_tmpl_key", columnList = "template_key", unique = true),
        @Index(name = "idx_tmpl_cat", columnList = "category"),
        @Index(name = "idx_tmpl_fav", columnList = "is_favorite")
})
public class PromptTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_key", nullable = false, length = 64)
    private String templateKey;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(length = 1000)
    private String description;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String variables;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "approval_status", length = 30)
    private String approvalStatus;

    @Column(length = 255)
    private String tags;

    @Column(name = "is_favorite")
    private Boolean isFavorite;

    @Column(name = "usage_count")
    private Long usageCount;

    @Column(length = 100)
    private String author;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public PromptTemplateEntity() {}

    public PromptTemplateEntity(Long id, String templateKey, String title, String category, String description,
                                String content, String variables, Integer version, String approvalStatus,
                                String tags, Boolean isFavorite, Long usageCount, String author,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.templateKey = templateKey;
        this.title = title;
        this.category = category;
        this.description = description;
        this.content = content;
        this.variables = variables;
        this.version = version;
        this.approvalStatus = approvalStatus;
        this.tags = tags;
        this.isFavorite = isFavorite;
        this.usageCount = usageCount;
        this.author = author;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (version == null) version = 1;
        if (approvalStatus == null) approvalStatus = "APPROVED";
        if (isFavorite == null) isFavorite = false;
        if (usageCount == null) usageCount = 0L;
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTemplateKey() { return templateKey; }
    public void setTemplateKey(String templateKey) { this.templateKey = templateKey; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getVariables() { return variables; }
    public void setVariables(String variables) { this.variables = variables; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Boolean getIsFavorite() { return isFavorite; }
    public void setIsFavorite(Boolean isFavorite) { this.isFavorite = isFavorite; }
    public Long getUsageCount() { return usageCount; }
    public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static PromptTemplateEntityBuilder builder() {
        return new PromptTemplateEntityBuilder();
    }

    public static class PromptTemplateEntityBuilder {
        private Long id;
        private String templateKey;
        private String title;
        private String category;
        private String description;
        private String content;
        private String variables;
        private Integer version;
        private String approvalStatus;
        private String tags;
        private Boolean isFavorite;
        private Long usageCount;
        private String author;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public PromptTemplateEntityBuilder id(Long id) { this.id = id; return this; }
        public PromptTemplateEntityBuilder templateKey(String templateKey) { this.templateKey = templateKey; return this; }
        public PromptTemplateEntityBuilder title(String title) { this.title = title; return this; }
        public PromptTemplateEntityBuilder category(String category) { this.category = category; return this; }
        public PromptTemplateEntityBuilder description(String description) { this.description = description; return this; }
        public PromptTemplateEntityBuilder content(String content) { this.content = content; return this; }
        public PromptTemplateEntityBuilder variables(String variables) { this.variables = variables; return this; }
        public PromptTemplateEntityBuilder version(Integer version) { this.version = version; return this; }
        public PromptTemplateEntityBuilder approvalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; return this; }
        public PromptTemplateEntityBuilder tags(String tags) { this.tags = tags; return this; }
        public PromptTemplateEntityBuilder isFavorite(Boolean isFavorite) { this.isFavorite = isFavorite; return this; }
        public PromptTemplateEntityBuilder usageCount(Long usageCount) { this.usageCount = usageCount; return this; }
        public PromptTemplateEntityBuilder author(String author) { this.author = author; return this; }
        public PromptTemplateEntityBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PromptTemplateEntityBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public PromptTemplateEntity build() {
            return new PromptTemplateEntity(id, templateKey, title, category, description, content, variables,
                    version, approvalStatus, tags, isFavorite, usageCount, author, createdAt, updatedAt);
        }
    }
}
