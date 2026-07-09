package com.itsm.knowledge.infrastructure.persistence;

import com.itsm.knowledge.domain.KnowledgeFeedback;
import com.itsm.knowledge.domain.repository.KnowledgeFeedbackRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeFeedbackJpaRepository
        extends JpaRepository<KnowledgeFeedback, Long>, KnowledgeFeedbackRepository {
}
