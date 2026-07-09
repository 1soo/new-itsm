package com.itsm.asset.domain;

/**
 * 자산 생애주기 단계(5단계). 순서 강제 없이 임의 전이 허용(설계 문서에 순차 제약 미명시).
 */
public enum AssetStatus {
    PLANNING,
    PROCUREMENT,
    OPERATION,
    MAINTENANCE,
    RETIREMENT
}
