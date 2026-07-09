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

import java.time.OffsetDateTime;

/**
 * 지식 기사. 제목/본문·상태(DRAFT/IN_REVIEW/PUBLISHED)·분류·유용성/조회 집계를 관리한다.
 */
@Getter
@Entity
@Table(name = "knowledge_article")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnowledgeArticle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ArticleStatus status;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "helpful_count", nullable = false)
    private int helpfulCount;

    @Column(name = "not_helpful_count", nullable = false)
    private int notHelpfulCount;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    public KnowledgeArticle(String title, String body, Long categoryId, Long authorId) {
        this.title = title;
        this.body = body;
        this.categoryId = categoryId;
        this.authorId = authorId;
        this.status = ArticleStatus.DRAFT;
        this.helpfulCount = 0;
        this.notHelpfulCount = 0;
        this.viewCount = 0;
    }

    public void updateContent(String title, String body, Long categoryId) {
        if (title != null) {
            this.title = title;
        }
        if (body != null) {
            this.body = body;
        }
        if (categoryId != null) {
            this.categoryId = categoryId;
        }
    }

    public void changeStatus(ArticleStatus status) {
        this.status = status;
    }

    public void publish() {
        this.status = ArticleStatus.PUBLISHED;
        this.publishedAt = OffsetDateTime.now();
    }

    public void incrementView() {
        this.viewCount++;
    }

    public void recordFeedback(boolean helpful) {
        if (helpful) {
            this.helpfulCount++;
        } else {
            this.notHelpfulCount++;
        }
    }

    public boolean isPublished() {
        return this.status == ArticleStatus.PUBLISHED;
    }
}
