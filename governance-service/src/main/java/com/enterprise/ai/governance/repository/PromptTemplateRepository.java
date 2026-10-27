package com.enterprise.ai.governance.repository;

import com.enterprise.ai.governance.entity.PromptTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplateEntity, Long> {
    Optional<PromptTemplateEntity> findByTemplateKey(String templateKey);
    List<PromptTemplateEntity> findByCategory(String category);
    List<PromptTemplateEntity> findByIsFavoriteTrue();
    List<PromptTemplateEntity> findByTagsContainingIgnoreCase(String tag);
    List<PromptTemplateEntity> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String desc);
    boolean existsByTemplateKey(String templateKey);
}
