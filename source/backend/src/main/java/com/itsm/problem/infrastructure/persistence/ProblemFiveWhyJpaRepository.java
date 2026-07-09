package com.itsm.problem.infrastructure.persistence;

import com.itsm.problem.domain.ProblemFiveWhy;
import com.itsm.problem.domain.repository.ProblemFiveWhyRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProblemFiveWhyJpaRepository
        extends JpaRepository<ProblemFiveWhy, Long>, ProblemFiveWhyRepository {

    @Override
    @Modifying
    @Query("delete from ProblemFiveWhy w where w.problemId = :problemId")
    void deleteByProblemId(@Param("problemId") Long problemId);
}
