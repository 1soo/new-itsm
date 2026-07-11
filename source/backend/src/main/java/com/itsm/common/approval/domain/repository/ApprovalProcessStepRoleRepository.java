package com.itsm.common.approval.domain.repository;

import com.itsm.common.approval.domain.ApprovalProcessStepRole;

import java.util.Collection;
import java.util.List;

/**
 * 승인 프로세스 차수별 승인 역할 저장소 포트.
 */
public interface ApprovalProcessStepRoleRepository {

    ApprovalProcessStepRole save(ApprovalProcessStepRole entity);

    List<ApprovalProcessStepRole> findByStepId(Long stepId);

    List<ApprovalProcessStepRole> findByStepIdIn(Collection<Long> stepIds);

    void deleteByStepIdIn(Collection<Long> stepIds);
}
