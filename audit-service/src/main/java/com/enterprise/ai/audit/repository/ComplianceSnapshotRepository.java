package com.enterprise.ai.audit.repository;

import com.enterprise.ai.audit.entity.ComplianceSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Spring Data JPA repository for compliance framework snapshots.
 */
@Repository
public interface ComplianceSnapshotRepository extends JpaRepository<ComplianceSnapshotEntity, Long> {

    List<ComplianceSnapshotEntity> findByFrameworkOrderBySnapshotDateDesc(String framework);

    List<ComplianceSnapshotEntity> findBySnapshotDateBetweenOrderBySnapshotDateDesc(LocalDate startDate, LocalDate endDate);

    List<ComplianceSnapshotEntity> findTop10ByOrderBySnapshotDateDesc();
}
