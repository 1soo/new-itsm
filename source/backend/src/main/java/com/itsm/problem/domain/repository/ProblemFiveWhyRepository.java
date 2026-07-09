package com.itsm.problem.domain.repository;

import com.itsm.problem.domain.ProblemFiveWhy;

import java.util.List;

/**
 * 5 Whys 저장소 포트.
 */
public interface ProblemFiveWhyRepository {

    ProblemFiveWhy save(ProblemFiveWhy fiveWhy);

    List<ProblemFiveWhy> findByProblemIdOrderByStepNoAsc(Long problemId);

    void deleteByProblemId(Long problemId);
}
