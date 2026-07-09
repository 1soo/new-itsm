package com.itsm.common.ticket.repository;

import com.itsm.common.ticket.TicketType;
import com.itsm.common.ticket.TimelineEvent;

import java.util.Collection;
import java.util.List;

/**
 * 타임라인 이벤트 저장소 포트.
 */
public interface TimelineEventRepository {

    TimelineEvent save(TimelineEvent event);

    List<TimelineEvent> findByTicketTypeAndTicketIdOrderByOccurredAtAsc(TicketType ticketType, Long ticketId);

    List<TimelineEvent> findByTicketTypeAndEventTypeAndTicketIdIn(TicketType ticketType, String eventType, Collection<Long> ticketIds);
}
