package com.itsm.knowledge.domain;

/**
 * 지식 기사 상태. DRAFT→IN_REVIEW→PUBLISHED. 반려 시 IN_REVIEW→DRAFT로 복귀.
 */
public enum ArticleStatus {
    DRAFT,
    IN_REVIEW,
    PUBLISHED
}
