package com.itsm.incident.infrastructure.persistence;

import com.itsm.incident.domain.IncidentResponder;
import com.itsm.incident.domain.repository.IncidentResponderRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentResponderJpaRepository
        extends JpaRepository<IncidentResponder, Long>, IncidentResponderRepository {
}
