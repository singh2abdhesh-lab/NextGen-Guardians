package com.enterprise.ai.audit.repository;

import com.enterprise.ai.audit.entity.ApprovalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for human approval lifecycle records.
 */
@Repository
public interface ApprovalRecordRepository extends JpaRepository<ApprovalRecordEntity, Long> {

    Optional<ApprovalRecordEntity> findByAuditEventId(Long auditEventId);

    List<ApprovalRecordEntity> findByDecision(String decision);

    List<ApprovalRecordEntity> findByApprover(String approver);

    List<ApprovalRecordEntity> findAllByOrderByReviewedAtDesc();
}
