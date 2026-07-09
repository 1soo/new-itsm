package com.itsm.incident.domain.repository;

import com.itsm.incident.domain.Incident;
import com.itsm.incident.domain.IncidentStatus;
import com.itsm.incident.domain.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 인시던트 저장소 포트.
 */
public interface IncidentRepository {

    Incident save(Incident incident);

    Optional<Incident> findById(Long id);

    Page<Incident> search(IncidentStatus status, Severity severity, Long assigneeId, String keyword,
                          OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    long countByTicketKeyStartingWith(String prefix);

    List<Incident> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);
}
