package com.enterprise.ai.governance.repository;

import com.enterprise.ai.governance.entity.AiPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiPolicyRepository extends JpaRepository<AiPolicyEntity, Long> {
    Optional<AiPolicyEntity> findByPolicyKey(String policyKey);
    List<AiPolicyEntity> findByCategory(String category);
    List<AiPolicyEntity> findByStatus(String status);
    List<AiPolicyEntity> findByEnabledTrueOrderByPriorityAsc();
    boolean existsByPolicyKey(String policyKey);
}
