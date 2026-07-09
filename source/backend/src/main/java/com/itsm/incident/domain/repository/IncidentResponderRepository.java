package com.itsm.incident.domain.repository;

import com.itsm.incident.domain.IncidentResponder;

import java.util.List;

/**
 * 대응 역할 배정 저장소 포트.
 */
public interface IncidentResponderRepository {

    IncidentResponder save(IncidentResponder responder);

    List<IncidentResponder> findByIncidentId(Long incidentId);

    boolean existsByIncidentIdAndUserIdAndResponseRole(Long incidentId, Long userId,
                                                       com.itsm.incident.domain.ResponseRole responseRole);
}
