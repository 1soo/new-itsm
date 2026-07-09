package com.itsm.problem.domain;

/**
 * 우선순위(영향도×긴급도 매트릭스 산정값). 둘 중 하나라도 없으면 미산정(null).
 */
public enum ProblemPriority {
    P1,
    P2,
    P3,
    P4;

    /** 영향도×긴급도 매트릭스. 둘 중 하나라도 null이면 미산정(null). */
    public static ProblemPriority of(Level impact, Level urgency) {
        if (impact == null || urgency == null) {
            return null;
        }
        int score = rank(impact) + rank(urgency);
        return switch (score) {
            case 6 -> P1;
            case 5 -> P2;
            case 4 -> P3;
            default -> P4;
        };
    }

    private static int rank(Level level) {
        return switch (level) {
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }
}
