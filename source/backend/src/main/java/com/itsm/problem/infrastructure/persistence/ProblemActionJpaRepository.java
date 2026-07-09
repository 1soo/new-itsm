package com.itsm.problem.infrastructure.persistence;

import com.itsm.problem.domain.ActionStatus;
import com.itsm.problem.domain.ProblemAction;
import com.itsm.problem.domain.repository.ProblemActionRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemActionJpaRepository
        extends JpaRepository<ProblemAction, Long>, ProblemActionRepository {

    @Override
    long countByProblemIdAndStatusNot(Long problemId, ActionStatus status);
}
