package com.enterprise.ai.governance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Schema(description = "Enterprise Reusable Prompt Template")
public class PromptTemplateDto {

    private Long id;

    @NotBlank(message = "Template key cannot be blank")
    @Schema(description = "Unique template identifier", example = "TMPL-FIN-001")
    private String templateKey;

    @NotBlank(message = "Title cannot be blank")
    @Schema(description = "Template display title", example = "Executive Quarterly Summary")
    private String title;

    @NotBlank(message = "Category cannot be blank")
    @Schema(description = "Template category", example = "FINANCE")
    private String category;

    private String description;

    @NotBlank(message = "Content cannot be blank")
    @Schema(description = "Template body with {variable} placeholders", example = "Summarize the earnings from {quarter} for {company}.")
    private String content;

    @Schema(description = "Comma-delimited variables required by template", example = "quarter,company")
    private String variables;

    private Integer version;

    @Schema(description = "Approval: APPROVED, PENDING, REJECTED", example = "APPROVED")
    private String approvalStatus;

    @Schema(description = "Comma-delimited tags", example = "finance,executive,earnings")
    private String tags;

    private Boolean isFavorite;
    private Long usageCount;
    private String author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PromptTemplateDto() {}

    public PromptTemplateDto(Long id, String templateKey, String title, String category, String description,
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

    public static PromptTemplateDtoBuilder builder() {
        return new PromptTemplateDtoBuilder();
    }

    public static class PromptTemplateDtoBuilder {
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

        public PromptTemplateDtoBuilder id(Long id) { this.id = id; return this; }
        public PromptTemplateDtoBuilder templateKey(String templateKey) { this.templateKey = templateKey; return this; }
        public PromptTemplateDtoBuilder title(String title) { this.title = title; return this; }
        public PromptTemplateDtoBuilder category(String category) { this.category = category; return this; }
        public PromptTemplateDtoBuilder description(String description) { this.description = description; return this; }
        public PromptTemplateDtoBuilder content(String content) { this.content = content; return this; }
        public PromptTemplateDtoBuilder variables(String variables) { this.variables = variables; return this; }
        public PromptTemplateDtoBuilder version(Integer version) { this.version = version; return this; }
        public PromptTemplateDtoBuilder approvalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; return this; }
        public PromptTemplateDtoBuilder tags(String tags) { this.tags = tags; return this; }
        public PromptTemplateDtoBuilder isFavorite(Boolean isFavorite) { this.isFavorite = isFavorite; return this; }
        public PromptTemplateDtoBuilder usageCount(Long usageCount) { this.usageCount = usageCount; return this; }
        public PromptTemplateDtoBuilder author(String author) { this.author = author; return this; }
        public PromptTemplateDtoBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public PromptTemplateDtoBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public PromptTemplateDto build() {
            return new PromptTemplateDto(id, templateKey, title, category, description, content, variables,
                    version, approvalStatus, tags, isFavorite, usageCount, author, createdAt, updatedAt);
        }
    }
}
