package com.itsm.knowledge.domain;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 검토·게시 승인/반려 이력.
 */
@Getter
@Entity
@Table(name = "knowledge_review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeReview extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false)
    private Long articleId;

    @Column(name = "gatekeeper_id", nullable = false)
    private Long gatekeeperId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReviewDecision decision;

    @Column(length = 500)
    private String reason;

    public KnowledgeReview(Long articleId, Long gatekeeperId, ReviewDecision decision, String reason) {
        this.articleId = articleId;
        this.gatekeeperId = gatekeeperId;
        this.decision = decision;
        this.reason = reason;
    }
}
