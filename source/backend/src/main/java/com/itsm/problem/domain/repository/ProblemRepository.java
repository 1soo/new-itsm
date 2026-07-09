package com.itsm.problem.domain.repository;

import com.itsm.problem.domain.Problem;
import com.itsm.problem.domain.ProblemOrigin;
import com.itsm.problem.domain.ProblemPriority;
import com.itsm.problem.domain.ProblemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * 문제 저장소 포트.
 */
public interface ProblemRepository {

    Problem save(Problem problem);

    Optional<Problem> findById(Long id);

    Page<Problem> search(ProblemStatus status, ProblemPriority priority, ProblemOrigin origin,
                         String assignee, OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    long countByTicketKeyStartingWith(String prefix);
}
