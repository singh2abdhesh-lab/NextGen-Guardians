package com.enterprise.ai.audit.repository;

import com.enterprise.ai.audit.entity.AuditEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEventEntity, Long> {

    Optional<AuditEventEntity> findByRequestId(String requestId);

    List<AuditEventEntity> findByUserIdOrderByTimestampDesc(String userId);

    Page<AuditEventEntity> findByUserIdOrderByTimestampDesc(String userId, Pageable pageable);

    List<AuditEventEntity> findByRiskLevelInOrderByTimestampDesc(List<String> riskLevels);

    Page<AuditEventEntity> findByRiskLevelInOrderByTimestampDesc(List<String> riskLevels, Pageable pageable);

    List<AuditEventEntity> findByApprovalStatusOrderByTimestampDesc(String approvalStatus);

    Page<AuditEventEntity> findByApprovalStatusOrderByTimestampDesc(String approvalStatus, Pageable pageable);

    List<AuditEventEntity> findAllByOrderByTimestampDesc();

    Page<AuditEventEntity> findAllByOrderByTimestampDesc(Pageable pageable);

    @Query("SELECT e FROM AuditEventEntity e WHERE e.timestamp >= :since ORDER BY e.timestamp DESC")
    List<AuditEventEntity> findEventsSince(@Param("since") LocalDateTime since);

    @Query("SELECT e FROM AuditEventEntity e WHERE e.timestamp BETWEEN :startDate AND :endDate ORDER BY e.timestamp DESC")
    List<AuditEventEntity> findEventsBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Dashboard aggregation queries
    @Query("SELECT COUNT(e) FROM AuditEventEntity e")
    long countAll();

    @Query("SELECT COUNT(e) FROM AuditEventEntity e WHERE e.action = 'BLOCK'")
    long countBlocked();

    @Query("SELECT COUNT(e) FROM AuditEventEntity e WHERE e.approvalRequired = true AND e.approvalStatus = 'PENDING'")
    long countPendingApprovals();

    @Query("SELECT COALESCE(SUM(e.costUsd), 0) FROM AuditEventEntity e")
    double totalCost();

    @Query("SELECT COALESCE(SUM(e.tokensIn), 0) FROM AuditEventEntity e")
    long totalTokensIn();

    @Query("SELECT COALESCE(SUM(e.tokensOut), 0) FROM AuditEventEntity e")
    long totalTokensOut();

    @Query("SELECT e.riskLevel, COUNT(e) FROM AuditEventEntity e WHERE e.riskLevel IS NOT NULL GROUP BY e.riskLevel")
    List<Object[]> countByRiskLevel();

    @Query("SELECT e.action, COUNT(e) FROM AuditEventEntity e WHERE e.action IS NOT NULL GROUP BY e.action")
    List<Object[]> countByAction();

    @Query("SELECT e.promptCategory, COUNT(e) FROM AuditEventEntity e WHERE e.promptCategory IS NOT NULL GROUP BY e.promptCategory")
    List<Object[]> countByPromptCategory();

    @Query("SELECT e.model, COUNT(e), COALESCE(SUM(e.costUsd), 0) FROM AuditEventEntity e WHERE e.timestamp >= :since GROUP BY e.model")
    List<Object[]> usageByModel(@Param("since") LocalDateTime since);

    @Query("SELECT e.userId, COUNT(e) FROM AuditEventEntity e WHERE e.timestamp >= :since GROUP BY e.userId")
    List<Object[]> usageByUser(@Param("since") LocalDateTime since);
}
