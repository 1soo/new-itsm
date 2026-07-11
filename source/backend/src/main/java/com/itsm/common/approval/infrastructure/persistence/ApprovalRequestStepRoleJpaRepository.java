package com.itsm.common.approval.infrastructure.persistence;

import com.itsm.common.approval.domain.ApprovalRequestStepRole;
import com.itsm.common.approval.domain.repository.ApprovalRequestStepRoleRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ApprovalRequestStepRoleRepository 포트의 Spring Data JPA 구현.
 */
public interface ApprovalRequestStepRoleJpaRepository
        extends JpaRepository<ApprovalRequestStepRole, Long>, ApprovalRequestStepRoleRepository {

    @Override
    List<ApprovalRequestStepRole> findByStepId(Long stepId);
}
