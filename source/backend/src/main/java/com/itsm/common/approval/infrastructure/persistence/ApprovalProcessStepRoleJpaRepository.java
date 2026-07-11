package com.itsm.common.approval.infrastructure.persistence;

import com.itsm.common.approval.domain.ApprovalProcessStepRole;
import com.itsm.common.approval.domain.repository.ApprovalProcessStepRoleRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * ApprovalProcessStepRoleRepository 포트의 Spring Data JPA 구현.
 */
public interface ApprovalProcessStepRoleJpaRepository
        extends JpaRepository<ApprovalProcessStepRole, Long>, ApprovalProcessStepRoleRepository {

    @Override
    List<ApprovalProcessStepRole> findByStepId(Long stepId);

    @Override
    List<ApprovalProcessStepRole> findByStepIdIn(Collection<Long> stepIds);

    @Override
    void deleteByStepIdIn(Collection<Long> stepIds);
}
