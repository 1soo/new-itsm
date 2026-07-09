package com.itsm.knowledge.infrastructure.persistence;

import com.itsm.knowledge.domain.KnowledgeLabel;
import com.itsm.knowledge.domain.repository.KnowledgeLabelRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KnowledgeLabelJpaRepository extends JpaRepository<KnowledgeLabel, Long>, KnowledgeLabelRepository {

    @Override
    Optional<KnowledgeLabel> findByNameIgnoreCase(String name);
}
