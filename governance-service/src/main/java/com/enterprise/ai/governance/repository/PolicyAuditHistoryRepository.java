package com.enterprise.ai.governance.repository;

import com.enterprise.ai.governance.entity.PolicyAuditHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyAuditHistoryRepository extends JpaRepository<PolicyAuditHistoryEntity, Long> {
    List<PolicyAuditHistoryEntity> findByPolicyIdOrderByChangedAtDesc(Long policyId);
    List<PolicyAuditHistoryEntity> findByPolicyKeyOrderByChangedAtDesc(String policyKey);
    List<PolicyAuditHistoryEntity> findTop50ByOrderByChangedAtDesc();
}
