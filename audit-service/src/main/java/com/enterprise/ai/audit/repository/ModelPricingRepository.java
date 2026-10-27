package com.enterprise.ai.audit.repository;

import com.enterprise.ai.audit.entity.ModelPricingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for AI model pricing schedules.
 */
@Repository
public interface ModelPricingRepository extends JpaRepository<ModelPricingEntity, Long> {

    Optional<ModelPricingEntity> findByModelNameAndActiveTrue(String modelName);

    List<ModelPricingEntity> findAllByActiveTrue();
}
