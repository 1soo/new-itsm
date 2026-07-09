package com.itsm.change.application;

import com.itsm.change.domain.ChangeStatus;

import java.util.Map;
import java.util.Set;

/**
 * 변경 상태 전이 규칙(API-CHG-004). 6단계 순차 진행.
 * REQUESTED→REVIEW→PLANNING→APPROVAL→IMPLEMENTATION→CLOSED.
 * 순서를 건너뛰거나 되돌리는 전이는 허용하지 않는다(400).
 */
final class ChangeStateMachine {

    private static final Map<ChangeStatus, ChangeStatus> NEXT = Map.of(
            ChangeStatus.REQUESTED, ChangeStatus.REVIEW,
            ChangeStatus.REVIEW, ChangeStatus.PLANNING,
            ChangeStatus.PLANNING, ChangeStatus.APPROVAL,
            ChangeStatus.APPROVAL, ChangeStatus.IMPLEMENTATION,
            ChangeStatus.IMPLEMENTATION, ChangeStatus.CLOSED);

    private ChangeStateMachine() {
    }

    static boolean isAllowed(ChangeStatus from, ChangeStatus to) {
        return NEXT.get(from) == to;
    }

    static Set<ChangeStatus> allowedTargets(ChangeStatus from) {
        ChangeStatus next = NEXT.get(from);
        return next == null ? Set.of() : Set.of(next);
    }
}
