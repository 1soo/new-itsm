package com.itsm.incident.infrastructure.persistence;

import com.itsm.incident.domain.IncidentSeverityHistory;
import com.itsm.incident.domain.repository.IncidentSeverityHistoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentSeverityHistoryJpaRepository
        extends JpaRepository<IncidentSeverityHistory, Long>, IncidentSeverityHistoryRepository {
}
