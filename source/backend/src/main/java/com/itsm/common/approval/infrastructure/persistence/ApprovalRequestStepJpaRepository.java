package com.itsm.common.approval.infrastructure.persistence;

import com.itsm.common.approval.domain.ApprovalRequestStep;
import com.itsm.common.approval.domain.repository.ApprovalRequestStepRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * ApprovalRequestStepRepository 포트의 Spring Data JPA 구현.
 */
public interface ApprovalRequestStepJpaRepository
        extends JpaRepository<ApprovalRequestStep, Long>, ApprovalRequestStepRepository {

    @Override
    List<ApprovalRequestStep> findByApprovalRequestIdOrderByStepNoAsc(Long approvalRequestId);

    @Override
    Optional<ApprovalRequestStep> findByApprovalRequestIdAndStepNo(Long approvalRequestId, short stepNo);
}
