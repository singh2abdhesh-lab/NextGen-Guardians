package com.enterprise.ai.governance.repository;

import com.enterprise.ai.governance.entity.ModelRegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelRegistrationRepository extends JpaRepository<ModelRegistrationEntity, Long> {
    Optional<ModelRegistrationEntity> findByModelName(String modelName);
    List<ModelRegistrationEntity> findByVendor(String vendor);
    List<ModelRegistrationEntity> findByStatus(String status);
    List<ModelRegistrationEntity> findByEnabledTrue();
    boolean existsByModelName(String modelName);
}
