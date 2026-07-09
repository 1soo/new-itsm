package com.itsm.problem.application;

import com.itsm.problem.domain.ProblemStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 문제 상태 전이 규칙(API-PRB-004). 6단계 순차 진행.
 * DETECTION→CLASSIFICATION→INVESTIGATION→KNOWN_ERROR→WORKAROUND→RESOLVED_CLOSED.
 * 순서를 건너뛰거나 되돌리는 전이는 허용하지 않는다(400).
 */
final class ProblemStateMachine {

    private static final List<ProblemStatus> ORDER = List.of(
            ProblemStatus.DETECTION,
            ProblemStatus.CLASSIFICATION,
            ProblemStatus.INVESTIGATION,
            ProblemStatus.KNOWN_ERROR,
            ProblemStatus.WORKAROUND,
            ProblemStatus.RESOLVED_CLOSED);

    private static final Map<ProblemStatus, ProblemStatus> NEXT = Map.of(
            ProblemStatus.DETECTION, ProblemStatus.CLASSIFICATION,
            ProblemStatus.CLASSIFICATION, ProblemStatus.INVESTIGATION,
            ProblemStatus.INVESTIGATION, ProblemStatus.KNOWN_ERROR,
            ProblemStatus.KNOWN_ERROR, ProblemStatus.WORKAROUND,
            ProblemStatus.WORKAROUND, ProblemStatus.RESOLVED_CLOSED);

    private ProblemStateMachine() {
    }

    static boolean isAllowed(ProblemStatus from, ProblemStatus to) {
        return NEXT.get(from) == to;
    }

    static Set<ProblemStatus> allowedTargets(ProblemStatus from) {
        ProblemStatus next = NEXT.get(from);
        return next == null ? Set.of() : Set.of(next);
    }

    static List<ProblemStatus> order() {
        return ORDER;
    }
}
