package com.itsm.incident.application;

import com.itsm.incident.domain.IncidentStatus;

import java.util.Map;
import java.util.Set;

/**
 * 인시던트 상태 전이 규칙(API-INC-005). NEW→IN_PROGRESS→RESOLVED→CLOSED.
 */
final class IncidentStateMachine {

    private static final Map<IncidentStatus, Set<IncidentStatus>> ALLOWED = Map.of(
            IncidentStatus.NEW, Set.of(IncidentStatus.IN_PROGRESS),
            IncidentStatus.IN_PROGRESS, Set.of(IncidentStatus.RESOLVED),
            IncidentStatus.RESOLVED, Set.of(IncidentStatus.CLOSED)
    );

    private IncidentStateMachine() {
    }

    static boolean isAllowed(IncidentStatus from, IncidentStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    static Set<IncidentStatus> allowedTargets(IncidentStatus from) {
        return ALLOWED.getOrDefault(from, Set.of());
    }
}
