package com.enterprise.ai.audit.repository;

import com.enterprise.ai.audit.entity.ViolationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for querying fine-grained audit violation details.
 */
@Repository
public interface ViolationRepository extends JpaRepository<ViolationEntity, Long> {

    List<ViolationEntity> findByAuditEventId(Long auditEventId);

    List<ViolationEntity> findByViolationType(String violationType);

    List<ViolationEntity> findBySeverity(String severity);

    @Query("SELECT v.violationType, COUNT(v) FROM ViolationEntity v GROUP BY v.violationType")
    List<Object[]> countByViolationType();

    @Query("SELECT v.severity, COUNT(v) FROM ViolationEntity v GROUP BY v.severity")
    List<Object[]> countBySeverity();

    @Query("SELECT v FROM ViolationEntity v WHERE v.auditEvent.id IN :eventIds")
    List<ViolationEntity> findByAuditEventIds(@Param("eventIds") List<Long> eventIds);
}
