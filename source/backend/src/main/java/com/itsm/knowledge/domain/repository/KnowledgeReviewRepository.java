package com.itsm.knowledge.domain.repository;

import com.itsm.knowledge.domain.KnowledgeReview;

/**
 * 검토·게시 이력 저장소 포트.
 */
public interface KnowledgeReviewRepository {

    KnowledgeReview save(KnowledgeReview review);
}
