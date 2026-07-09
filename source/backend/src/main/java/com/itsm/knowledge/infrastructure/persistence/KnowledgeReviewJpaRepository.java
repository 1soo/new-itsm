package com.itsm.knowledge.infrastructure.persistence;

import com.itsm.knowledge.domain.KnowledgeReview;
import com.itsm.knowledge.domain.repository.KnowledgeReviewRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeReviewJpaRepository
        extends JpaRepository<KnowledgeReview, Long>, KnowledgeReviewRepository {
}
