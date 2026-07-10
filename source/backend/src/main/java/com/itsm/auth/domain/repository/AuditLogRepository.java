package com.itsm.auth.domain.repository;

import com.itsm.auth.domain.AuditLog;
import com.itsm.auth.domain.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 감사 로그 저장소 포트.
 */
public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    Page<AuditLog> search(EventType eventType, String actor, String target,
                          OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    /** 다중 이벤트 타입 조회(예: 컴플라이언스 전용 감사 로그, API-COMP-009). */
    List<AuditLog> findByEventTypeInAndOccurredAtBetweenOrderByOccurredAtDesc(
            Collection<EventType> eventTypes, OffsetDateTime from, OffsetDateTime to);
}
