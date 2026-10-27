package com.enterprise.ai.governance.service;

import com.enterprise.ai.governance.dto.PromptTemplateDto;
import com.enterprise.ai.governance.entity.PromptTemplateEntity;
import com.enterprise.ai.governance.repository.PromptTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PromptTemplateService {

    private static final Logger log = LoggerFactory.getLogger(PromptTemplateService.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}");

    private final PromptTemplateRepository templateRepository;

    public PromptTemplateService(PromptTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateDto> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateDto> getFavorites() {
        return templateRepository.findByIsFavoriteTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateDto> getByCategory(String category) {
        return templateRepository.findByCategory(category).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PromptTemplateDto> searchTemplates(String query) {
        if (query == null || query.isBlank()) {
            return getAllTemplates();
        }
        return templateRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PromptTemplateDto getTemplateById(Long id) {
        return templateRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Template not found with id: " + id));
    }

    @Transactional
    public PromptTemplateDto createTemplate(PromptTemplateDto dto, String author) {
        if (templateRepository.existsByTemplateKey(dto.getTemplateKey())) {
            throw new IllegalArgumentException("Template with key '" + dto.getTemplateKey() + "' already exists");
        }

        PromptTemplateEntity entity = toEntity(dto);
        entity.setVariables(extractVariables(dto.getContent()));
        entity.setVersion(1);
        entity.setAuthor(author != null ? author : "system");
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUsageCount(0L);

        return toDto(templateRepository.save(entity));
    }

    @Transactional
    public PromptTemplateDto updateTemplate(Long id, PromptTemplateDto dto) {
        PromptTemplateEntity existing = templateRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Template not found with id: " + id));

        existing.setTitle(dto.getTitle());
        existing.setCategory(dto.getCategory());
        existing.setDescription(dto.getDescription());
        existing.setContent(dto.getContent());
        existing.setVariables(extractVariables(dto.getContent()));
        existing.setTags(dto.getTags());
        existing.setVersion(existing.getVersion() + 1); // auto-increment version
        existing.setApprovalStatus(dto.getApprovalStatus() != null ? dto.getApprovalStatus() : existing.getApprovalStatus());
        existing.setUpdatedAt(LocalDateTime.now());

        return toDto(templateRepository.save(existing));
    }

    @Transactional
    public PromptTemplateDto toggleFavorite(Long id) {
        PromptTemplateEntity existing = templateRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Template not found with id: " + id));

        existing.setIsFavorite(!Boolean.TRUE.equals(existing.getIsFavorite()));
        existing.setUpdatedAt(LocalDateTime.now());
        return toDto(templateRepository.save(existing));
    }

    @Transactional
    public PromptTemplateDto incrementUsage(Long id) {
        PromptTemplateEntity existing = templateRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Template not found with id: " + id));

        existing.setUsageCount(existing.getUsageCount() + 1);
        return toDto(templateRepository.save(existing));
    }

    @Transactional
    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    @Transactional
    public List<PromptTemplateDto> importTemplates(List<PromptTemplateDto> dtos, String author) {
        List<PromptTemplateDto> savedList = new ArrayList<>();
        for (PromptTemplateDto dto : dtos) {
            if (!templateRepository.existsByTemplateKey(dto.getTemplateKey())) {
                savedList.add(createTemplate(dto, author));
            }
        }
        return savedList;
    }

    private String extractVariables(String content) {
        if (content == null) return "";
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        List<String> vars = new ArrayList<>();
        while (matcher.find()) {
            String v = matcher.group(1);
            if (!vars.contains(v)) {
                vars.add(v);
            }
        }
        return String.join(",", vars);
    }

    public PromptTemplateDto toDto(PromptTemplateEntity e) {
        return PromptTemplateDto.builder()
                .id(e.getId())
                .templateKey(e.getTemplateKey())
                .title(e.getTitle())
                .category(e.getCategory())
                .description(e.getDescription())
                .content(e.getContent())
                .variables(e.getVariables())
                .version(e.getVersion())
                .approvalStatus(e.getApprovalStatus())
                .tags(e.getTags())
                .isFavorite(e.getIsFavorite())
                .usageCount(e.getUsageCount())
                .author(e.getAuthor())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public PromptTemplateEntity toEntity(PromptTemplateDto d) {
        return PromptTemplateEntity.builder()
                .id(d.getId())
                .templateKey(d.getTemplateKey())
                .title(d.getTitle())
                .category(d.getCategory())
                .description(d.getDescription())
                .content(d.getContent())
                .variables(d.getVariables())
                .version(d.getVersion() != null ? d.getVersion() : 1)
                .approvalStatus(d.getApprovalStatus() != null ? d.getApprovalStatus() : "APPROVED")
                .tags(d.getTags())
                .isFavorite(d.getIsFavorite() != null ? d.getIsFavorite() : false)
                .usageCount(d.getUsageCount() != null ? d.getUsageCount() : 0L)
                .author(d.getAuthor())
                .build();
    }
}
