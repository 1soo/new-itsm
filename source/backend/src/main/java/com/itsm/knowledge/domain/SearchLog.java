package com.itsm.knowledge.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

/**
 * 검색 로그(무결과 지표). append-only.
 */
@Getter
@Entity
@Table(name = "search_log")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String keyword;

    @Column(name = "result_count", nullable = false)
    private int resultCount;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "searched_at", nullable = false)
    private OffsetDateTime searchedAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public SearchLog(String keyword, int resultCount, Long userId) {
        this.keyword = keyword;
        this.resultCount = resultCount;
        this.userId = userId;
        this.searchedAt = OffsetDateTime.now();
    }
}
