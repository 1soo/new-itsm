package com.itsm.knowledge.domain;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기사 유용성 평가.
 */
@Getter
@Entity
@Table(name = "knowledge_feedback")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeFeedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private boolean helpful;

    @Column(length = 500)
    private String comment;

    public KnowledgeFeedback(Long articleId, Long userId, boolean helpful, String comment) {
        this.articleId = articleId;
        this.userId = userId;
        this.helpful = helpful;
        this.comment = comment;
    }
}
