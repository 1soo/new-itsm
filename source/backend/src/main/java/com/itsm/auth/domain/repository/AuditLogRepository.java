package com.itsm.auth.domain.repository;

import com.itsm.auth.domain.AuditLog;
import com.itsm.auth.domain.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

/**
 * 감사 로그 저장소 포트.
 */
public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    Page<AuditLog> search(EventType eventType, String actor, String target,
                          OffsetDateTime from, OffsetDateTime to, Pageable pageable);
}
