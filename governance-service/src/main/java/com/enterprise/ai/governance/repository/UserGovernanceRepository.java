package com.enterprise.ai.governance.repository;

import com.enterprise.ai.governance.entity.UserGovernanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGovernanceRepository extends JpaRepository<UserGovernanceEntity, Long> {
    Optional<UserGovernanceEntity> findByUserId(String userId);
    List<UserGovernanceEntity> findByDepartment(String department);
    List<UserGovernanceEntity> findByIsRestrictedTrue();
    boolean existsByUserId(String userId);
}
