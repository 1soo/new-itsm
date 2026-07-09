package com.itsm.problem.domain;

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
 * 알려진 오류(KEDB). 문제 1:N, 제목 키워드 검색 대상.
 */
@Getter
@Entity
@Table(name = "known_error")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KnownError extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(name = "root_cause", length = 1000)
    private String rootCause;

    @Column(columnDefinition = "text")
    private String workaround;

    public KnownError(Long problemId, String title, String rootCause, String workaround) {
        this.problemId = problemId;
        this.title = title;
        this.rootCause = rootCause;
        this.workaround = workaround;
    }
}
