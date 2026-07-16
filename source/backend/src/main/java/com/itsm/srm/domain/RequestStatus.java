package com.itsm.srm.domain;

/**
 * 서비스 요청 상태. SUBMITTED → VALIDATED → ROUTED → IN_FULFILLMENT → FULFILLED → CLOSED.
 * 승인 게이트는 상태값이 아니라 IN_FULFILLMENT 전이 시도 시 공용 승인 엔진(common.approval)이 판정한다.
 */
public enum RequestStatus {
    SUBMITTED,
    VALIDATED,
    ROUTED,
    IN_FULFILLMENT,
    FULFILLED,
    CLOSED;

    public boolean isTerminal() {
        return this == CLOSED;
    }

    /** 상태 한글 라벨(FE `features/service-request/status.ts`의 STATUS_LABEL과 동일 값). */
    public String label() {
        return switch (this) {
            case SUBMITTED -> "제출됨";
            case VALIDATED -> "검증됨";
            case ROUTED -> "라우팅됨";
            case IN_FULFILLMENT -> "이행 중";
            case FULFILLED -> "이행 완료";
            case CLOSED -> "종료";
        };
    }
}
