package com.enterprise.ai.governance.repository;

import com.enterprise.ai.governance.entity.ApprovalPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalPolicyRepository extends JpaRepository<ApprovalPolicyEntity, Long> {
    Optional<ApprovalPolicyEntity> findByPolicyKey(String policyKey);
    List<ApprovalPolicyEntity> findByTriggerCategory(String triggerCategory);
    List<ApprovalPolicyEntity> findByEnabledTrue();
    boolean existsByPolicyKey(String policyKey);
}
