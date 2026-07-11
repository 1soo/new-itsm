package com.itsm.common.approval.domain;

/**
 * 차수 결정 방식. 역할이 2개 이상일 때만 의미가 있다(1개면 무관).
 */
public enum DecisionMode {
    AND,
    OR
}
