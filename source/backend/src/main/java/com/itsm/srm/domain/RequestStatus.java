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
}
