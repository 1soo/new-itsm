package com.itsm.common.ticket;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 티켓 공용 타임라인 이벤트(상태 변경·배정·업데이트 이력).
 */
@Getter
@Entity
@Table(name = "timeline_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimelineEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type", nullable = false, length = 20)
    private TicketType ticketType;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(columnDefinition = "text")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Visibility visibility;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    private TimelineEvent(TicketType ticketType, Long ticketId, String eventType, String message, Visibility visibility) {
        this.ticketType = ticketType;
        this.ticketId = ticketId;
        this.eventType = eventType;
        this.message = message;
        this.visibility = visibility;
        this.occurredAt = OffsetDateTime.now();
    }

    public static TimelineEvent of(TicketType ticketType, Long ticketId, String eventType, String message) {
        return new TimelineEvent(ticketType, ticketId, eventType, message, Visibility.INTERNAL);
    }
}
