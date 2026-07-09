package com.itsm.common.ticket.repository;

import com.itsm.common.ticket.TicketLink;
import com.itsm.common.ticket.TicketType;

import java.util.List;

/**
 * 다형 티켓 링크 저장소 포트.
 */
public interface TicketLinkRepository {

    TicketLink save(TicketLink link);

    List<TicketLink> findBySourceTypeAndSourceId(TicketType sourceType, Long sourceId);

    boolean existsBySourceTypeAndSourceIdAndTargetTypeAndTargetId(
            TicketType sourceType, Long sourceId, TicketType targetType, Long targetId);
}
