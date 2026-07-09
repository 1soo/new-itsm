package com.itsm.incident.domain.repository;

import com.itsm.incident.domain.IncidentSeverityHistory;

/**
 * 심각도 변경 이력 저장소 포트.
 */
public interface IncidentSeverityHistoryRepository {

    IncidentSeverityHistory save(IncidentSeverityHistory history);
}
