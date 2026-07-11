package com.itsm.common.approval.domain.repository;

import com.itsm.common.approval.domain.ApprovalRequestStep;

import java.util.List;
import java.util.Optional;

/**
 * 승인 인스턴스 차수 스냅샷 저장소 포트.
 */
public interface ApprovalRequestStepRepository {

    ApprovalRequestStep save(ApprovalRequestStep step);

    List<ApprovalRequestStep> findByApprovalRequestIdOrderByStepNoAsc(Long approvalRequestId);

    Optional<ApprovalRequestStep> findByApprovalRequestIdAndStepNo(Long approvalRequestId, short stepNo);
}
