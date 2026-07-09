package com.itsm.knowledge.domain.repository;

import com.itsm.knowledge.domain.KnowledgeLabel;

import java.util.Optional;

/**
 * 지식 라벨 저장소 포트.
 */
public interface KnowledgeLabelRepository {

    KnowledgeLabel save(KnowledgeLabel label);

    Optional<KnowledgeLabel> findById(Long id);

    Optional<KnowledgeLabel> findByNameIgnoreCase(String name);
}
