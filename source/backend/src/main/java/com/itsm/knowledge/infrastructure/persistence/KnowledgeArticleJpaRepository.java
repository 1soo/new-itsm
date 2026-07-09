package com.itsm.knowledge.infrastructure.persistence;

import com.itsm.knowledge.domain.ArticleStatus;
import com.itsm.knowledge.domain.KnowledgeArticle;
import com.itsm.knowledge.domain.repository.KnowledgeArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface KnowledgeArticleJpaRepository extends JpaRepository<KnowledgeArticle, Long>, KnowledgeArticleRepository {

    @Override
    @Query("""
            select distinct a from KnowledgeArticle a
            where a.isDeleted = false
              and (:keyword is null
                   or lower(a.title) like lower(concat('%', cast(:keyword as string), '%'))
                   or lower(a.body) like lower(concat('%', cast(:keyword as string), '%')))
              and (:categoryId is null or a.categoryId = :categoryId)
              and (:labelName is null or a.id in (
                    select al.articleId from ArticleLabel al join KnowledgeLabel l on l.id = al.labelId
                    where al.isDeleted = false and lower(l.name) = lower(cast(:labelName as string))
              ))
              and (:viewerIsGatekeeper = true or a.status = com.itsm.knowledge.domain.ArticleStatus.PUBLISHED
                   or (:viewerAuthorId is not null and a.authorId = :viewerAuthorId))
              and (:status is null or a.status = :status)
            """)
    Page<KnowledgeArticle> search(@Param("keyword") String keyword,
                                  @Param("categoryId") Long categoryId,
                                  @Param("labelName") String labelName,
                                  @Param("status") ArticleStatus status,
                                  @Param("viewerAuthorId") Long viewerAuthorId,
                                  @Param("viewerIsGatekeeper") boolean viewerIsGatekeeper,
                                  Pageable pageable);

    @Override
    List<KnowledgeArticle> findByStatusAndCreatedAtBetween(ArticleStatus status, OffsetDateTime from, OffsetDateTime to);
}
