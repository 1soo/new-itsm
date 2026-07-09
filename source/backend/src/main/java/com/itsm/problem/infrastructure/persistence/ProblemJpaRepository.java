package com.itsm.problem.infrastructure.persistence;

import com.itsm.problem.domain.Problem;
import com.itsm.problem.domain.ProblemOrigin;
import com.itsm.problem.domain.ProblemPriority;
import com.itsm.problem.domain.ProblemStatus;
import com.itsm.problem.domain.repository.ProblemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface ProblemJpaRepository extends JpaRepository<Problem, Long>, ProblemRepository {

    @Override
    @Query("""
            select p from Problem p
            where p.isDeleted = false
              and (:status is null or p.status = :status)
              and (:priority is null or p.priority = :priority)
              and (:origin is null or p.origin = :origin)
              and (:assignee is null or p.createdBy = :assignee)
              and p.createdAt >= :from and p.createdAt <= :to
            """)
    Page<Problem> search(@Param("status") ProblemStatus status,
                         @Param("priority") ProblemPriority priority,
                         @Param("origin") ProblemOrigin origin,
                         @Param("assignee") String assignee,
                         @Param("from") OffsetDateTime from,
                         @Param("to") OffsetDateTime to,
                         Pageable pageable);
}
