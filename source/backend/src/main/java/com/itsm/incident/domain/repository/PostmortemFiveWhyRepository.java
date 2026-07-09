package com.itsm.incident.domain.repository;

import com.itsm.incident.domain.PostmortemFiveWhy;

import java.util.List;

/**
 * 5 Whys 저장소 포트.
 */
public interface PostmortemFiveWhyRepository {

    PostmortemFiveWhy save(PostmortemFiveWhy fiveWhy);

    List<PostmortemFiveWhy> findByPostmortemIdOrderByStepNoAsc(Long postmortemId);

    void deleteByPostmortemId(Long postmortemId);
}
