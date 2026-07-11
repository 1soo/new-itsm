package com.itsm.common.approval.infrastructure.persistence;

import com.itsm.common.approval.domain.ApprovalProcessStep;
import com.itsm.common.approval.domain.repository.ApprovalProcessStepRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * ApprovalProcessStepRepository 포트의 Spring Data JPA 구현.
 */
public interface ApprovalProcessStepJpaRepository
        extends JpaRepository<ApprovalProcessStep, Long>, ApprovalProcessStepRepository {

    @Override
    List<ApprovalProcessStep> findByApprovalProcessIdOrderByStepNoAsc(Long approvalProcessId);

    @Override
    void deleteByApprovalProcessId(Long approvalProcessId);
}
