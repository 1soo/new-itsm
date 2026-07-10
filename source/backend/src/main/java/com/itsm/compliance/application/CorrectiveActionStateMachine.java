package com.itsm.compliance.application;

import com.itsm.compliance.domain.CorrectiveActionStatus;

import java.util.Map;

/**
 * 시정조치 상태 전이 규칙(API-COMP-008). DETECTED→IN_PROGRESS→RESOLVED 순차만 허용(건너뛰기·역행 불가).
 */
final class CorrectiveActionStateMachine {

    private static final Map<CorrectiveActionStatus, CorrectiveActionStatus> NEXT = Map.of(
            CorrectiveActionStatus.DETECTED, CorrectiveActionStatus.IN_PROGRESS,
            CorrectiveActionStatus.IN_PROGRESS, CorrectiveActionStatus.RESOLVED);

    private CorrectiveActionStateMachine() {
    }

    static boolean isAllowed(CorrectiveActionStatus from, CorrectiveActionStatus to) {
        return NEXT.get(from) == to;
    }
}
