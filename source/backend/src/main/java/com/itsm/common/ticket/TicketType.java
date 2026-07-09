package com.itsm.common.ticket;

/**
 * 다형(polymorphic) 티켓 유형. comment/timeline_event/approval/ticket_link 공용.
 */
public enum TicketType {
    SERVICE_REQUEST,
    INCIDENT,
    PROBLEM,
    CHANGE,
    ASSET,
    CI,
    KNOWLEDGE
}
