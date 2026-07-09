package com.itsm.common.ticket.persistence;

import com.itsm.common.ticket.TicketLink;
import com.itsm.common.ticket.repository.TicketLinkRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * TicketLinkRepository 포트의 Spring Data JPA 구현.
 */
public interface TicketLinkJpaRepository extends JpaRepository<TicketLink, Long>, TicketLinkRepository {
}
