package com.itsm.compliance.domain.repository;

import com.itsm.compliance.domain.CorrectiveAction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 시정조치 저장소 포트.
 */
public interface CorrectiveActionRepository {

    CorrectiveAction save(CorrectiveAction action);

    Optional<CorrectiveAction> findById(Long id);

    List<CorrectiveAction> findByRequirementId(Long requirementId);

    /** 목록 조회 시 준수상태 배치 계산용(N+1 방지). */
    List<CorrectiveAction> findByRequirementIdIn(Collection<Long> requirementIds);
}
