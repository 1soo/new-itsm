package com.itsm.problem.domain.repository;

import com.itsm.problem.domain.ProblemAction;

import java.util.List;
import java.util.Optional;

/**
 * 후속 조치 저장소 포트.
 */
public interface ProblemActionRepository {

    ProblemAction save(ProblemAction action);

    Optional<ProblemAction> findById(Long id);

    List<ProblemAction> findByProblemId(Long problemId);

    long countByProblemIdAndStatusNot(Long problemId, com.itsm.problem.domain.ActionStatus status);
}
