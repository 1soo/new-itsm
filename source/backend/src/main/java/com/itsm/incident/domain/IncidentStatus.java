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
}
