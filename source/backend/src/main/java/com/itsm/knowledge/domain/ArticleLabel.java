package com.itsm.knowledge.domain;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기사-라벨 매핑(N:M).
 */
@Getter
@Entity
@Table(name = "article_label", uniqueConstraints = @UniqueConstraint(columnNames = {"article_id", "label_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleLabel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "label_id", nullable = false)
    private Long labelId;

    public ArticleLabel(Long articleId, Long labelId) {
        this.articleId = articleId;
        this.labelId = labelId;
    }
}
