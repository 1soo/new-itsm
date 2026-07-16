package com.itsm.incident.domain;

/**
 * 인시던트 상태. NEW → IN_PROGRESS → RESOLVED → CLOSED.
 */
public enum IncidentStatus {
    NEW,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    public boolean isTerminal() {
        return this == CLOSED;
    }

    /** 상태 한글 라벨(FE `features/incident/status.ts`의 STATUS_LABEL과 동일 값). */
    public String label() {
        return switch (this) {
            case NEW -> "신규";
            case IN_PROGRESS -> "대응중";
            case RESOLVED -> "해결";
            case CLOSED -> "종료";
        };
    }
}
