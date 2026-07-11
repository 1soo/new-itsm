package com.itsm.srm.application;

import com.itsm.srm.domain.RequestStatus;

import java.util.Map;
import java.util.Set;

/**
 * 서비스 요청 상태 전이 규칙(수동 전이, API-SRM-010).
 * IN_FULFILLMENT 전이는 공용 승인 게이트(common.approval.ApprovalGateService)가 별도로 판정한다.
 */
final class RequestStateMachine {

    private static final Map<RequestStatus, Set<RequestStatus>> ALLOWED = Map.of(
            RequestStatus.SUBMITTED, Set.of(RequestStatus.VALIDATED),
            RequestStatus.VALIDATED, Set.of(RequestStatus.ROUTED),
            RequestStatus.ROUTED, Set.of(RequestStatus.IN_FULFILLMENT),
            RequestStatus.IN_FULFILLMENT, Set.of(RequestStatus.FULFILLED),
            RequestStatus.FULFILLED, Set.of(RequestStatus.CLOSED)
    );

    private RequestStateMachine() {
    }

    static boolean isAllowed(RequestStatus from, RequestStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    static Set<RequestStatus> allowedTargets(RequestStatus from) {
        return ALLOWED.getOrDefault(from, Set.of());
    }
}
