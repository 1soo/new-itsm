package com.itsm.common.approval.infrastructure.persistence;

import com.itsm.common.approval.domain.ApprovalDecision;
import com.itsm.common.approval.domain.repository.ApprovalDecisionRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * ApprovalDecisionRepository 포트의 Spring Data JPA 구현.
 */
public interface ApprovalDecisionJpaRepository extends JpaRepository<ApprovalDecision, Long>, ApprovalDecisionRepository {

    @Override
    List<ApprovalDecision> findByStepId(Long stepId);

    @Override
    Optional<ApprovalDecision> findByStepIdAndRoleId(Long stepId, Long roleId);

    @Override
    boolean existsByStepIdAndRoleId(Long stepId, Long roleId);
}
