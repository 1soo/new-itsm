package com.itsm.common.approval.domain.repository;

import com.itsm.common.approval.domain.ApprovalDecision;

import java.util.List;
import java.util.Optional;

/**
 * 역할별 승인/반려 결정 기록 저장소 포트(append-only).
 */
public interface ApprovalDecisionRepository {

    ApprovalDecision save(ApprovalDecision decision);

    List<ApprovalDecision> findByStepId(Long stepId);

    Optional<ApprovalDecision> findByStepIdAndRoleId(Long stepId, Long roleId);

    boolean existsByStepIdAndRoleId(Long stepId, Long roleId);
}
