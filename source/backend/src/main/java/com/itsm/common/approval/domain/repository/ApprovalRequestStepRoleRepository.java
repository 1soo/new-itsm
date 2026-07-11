package com.itsm.common.approval.domain.repository;

import com.itsm.common.approval.domain.ApprovalRequestStepRole;

import java.util.List;

/**
 * 승인 인스턴스 차수별 필요 역할 스냅샷 저장소 포트.
 */
public interface ApprovalRequestStepRoleRepository {

    ApprovalRequestStepRole save(ApprovalRequestStepRole entity);

    List<ApprovalRequestStepRole> findByStepId(Long stepId);
}
