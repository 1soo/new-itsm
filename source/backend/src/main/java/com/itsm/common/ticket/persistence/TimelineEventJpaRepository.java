package com.itsm.common.ticket.persistence;

import com.itsm.common.ticket.TimelineEvent;
import com.itsm.common.ticket.repository.TimelineEventRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * TimelineEventRepository 포트의 Spring Data JPA 구현.
 */
public interface TimelineEventJpaRepository extends JpaRepository<TimelineEvent, Long>, TimelineEventRepository {
}
