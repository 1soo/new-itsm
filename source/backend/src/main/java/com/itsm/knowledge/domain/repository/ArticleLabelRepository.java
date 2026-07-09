package com.itsm.knowledge.domain.repository;

import com.itsm.knowledge.domain.ArticleLabel;

import java.util.List;

/**
 * 기사-라벨 매핑 저장소 포트.
 */
public interface ArticleLabelRepository {

    ArticleLabel save(ArticleLabel articleLabel);

    List<ArticleLabel> findByArticleId(Long articleId);

    void deleteByArticleId(Long articleId);
}
