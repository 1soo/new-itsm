package com.itsm.incident.domain.repository;

import com.itsm.incident.domain.Postmortem;

import java.util.Optional;

/**
 * 포스트모템 저장소 포트.
 */
public interface PostmortemRepository {

    Postmortem save(Postmortem postmortem);

    Optional<Postmortem> findByIncidentId(Long incidentId);
}
