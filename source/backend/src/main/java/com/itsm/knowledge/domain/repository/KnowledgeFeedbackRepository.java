package com.itsm.knowledge.domain.repository;

import com.itsm.knowledge.domain.KnowledgeFeedback;

/**
 * 유용성 평가 저장소 포트.
 */
public interface KnowledgeFeedbackRepository {

    KnowledgeFeedback save(KnowledgeFeedback feedback);
}
