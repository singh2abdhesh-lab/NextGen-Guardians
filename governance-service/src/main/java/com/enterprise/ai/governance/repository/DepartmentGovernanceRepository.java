package com.enterprise.ai.governance.repository;

import com.enterprise.ai.governance.entity.DepartmentGovernanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartmentGovernanceRepository extends JpaRepository<DepartmentGovernanceEntity, Long> {
    Optional<DepartmentGovernanceEntity> findByDepartmentNameIgnoreCase(String departmentName);
    Optional<DepartmentGovernanceEntity> findByDepartmentCodeIgnoreCase(String departmentCode);
    boolean existsByDepartmentCodeIgnoreCase(String departmentCode);
}
