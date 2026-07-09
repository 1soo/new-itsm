package com.itsm.srm.domain;

/**
 * 서비스 요청 상태.
 * SUBMITTED → VALIDATED → ROUTED → (승인필요 시 APPROVAL_PENDING) → IN_FULFILLMENT → FULFILLED → CLOSED.
 * 승인 반려 시 REJECTED(종료).
 */
public enum RequestStatus {
    SUBMITTED,
    VALIDATED,
    ROUTED,
    APPROVAL_PENDING,
    IN_FULFILLMENT,
    FULFILLED,
    CLOSED,
    REJECTED;

    public boolean isTerminal() {
        return this == CLOSED || this == REJECTED;
    }
}
