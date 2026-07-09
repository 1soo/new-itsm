package com.itsm.knowledge.infrastructure.persistence;

import com.itsm.knowledge.domain.ArticleLabel;
import com.itsm.knowledge.domain.repository.ArticleLabelRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleLabelJpaRepository extends JpaRepository<ArticleLabel, Long>, ArticleLabelRepository {

    @Override
    List<ArticleLabel> findByArticleId(Long articleId);

    @Override
    void deleteByArticleId(Long articleId);
}
