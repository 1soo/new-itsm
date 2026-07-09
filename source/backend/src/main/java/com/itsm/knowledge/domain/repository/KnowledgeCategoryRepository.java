package com.itsm.knowledge.domain.repository;

import com.itsm.knowledge.domain.KnowledgeCategory;

import java.util.List;
import java.util.Optional;

/**
 * 지식 카테고리 저장소 포트.
 */
public interface KnowledgeCategoryRepository {

    KnowledgeCategory save(KnowledgeCategory category);

    Optional<KnowledgeCategory> findById(Long id);

    List<KnowledgeCategory> findActive();
}
