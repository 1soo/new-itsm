package com.itsm.knowledge.domain.repository;

import com.itsm.knowledge.domain.ArticleStatus;
import com.itsm.knowledge.domain.KnowledgeArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 지식 기사 저장소 포트.
 */
public interface KnowledgeArticleRepository {

    KnowledgeArticle save(KnowledgeArticle article);

    Optional<KnowledgeArticle> findById(Long id);

    /**
     * 검색/목록. viewerIsGatekeeper=true면 전 상태 조회, 아니면 PUBLISHED 또는 본인(viewerAuthorId) 작성 기사만 대상.
     * status 파라미터는 위 가시 범위 내에서 추가로 좁힌다.
     */
    Page<KnowledgeArticle> search(String keyword, Long categoryId, String labelName, ArticleStatus status,
                                  Long viewerAuthorId, boolean viewerIsGatekeeper, Pageable pageable);

    List<KnowledgeArticle> findByStatusAndCreatedAtBetween(ArticleStatus status, OffsetDateTime from, OffsetDateTime to);
}
