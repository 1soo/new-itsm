package com.itsm.knowledge.infrastructure.persistence;

import com.itsm.knowledge.domain.KnowledgeCategory;
import com.itsm.knowledge.domain.repository.KnowledgeCategoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface KnowledgeCategoryJpaRepository
        extends JpaRepository<KnowledgeCategory, Long>, KnowledgeCategoryRepository {

    @Override
    @Query("select c from KnowledgeCategory c where c.isDeleted = false")
    List<KnowledgeCategory> findActive();
}
