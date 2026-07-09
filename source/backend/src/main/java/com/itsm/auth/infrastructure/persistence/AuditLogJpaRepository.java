package com.itsm.auth.infrastructure.persistence;

import com.itsm.auth.domain.AuditLog;
import com.itsm.auth.domain.EventType;
import com.itsm.auth.domain.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

/**
 * AuditLogRepository 포트의 Spring Data JPA 구현.
 */
public interface AuditLogJpaRepository extends JpaRepository<AuditLog, Long>, AuditLogRepository {

    @Override
    @Query("""
            select a from AuditLog a
            where (:eventType is null or a.eventType = :eventType)
              and (:actor is null or lower(a.actorEmail) like lower(concat('%', cast(:actor as string), '%')))
              and (:target is null or lower(a.target) like lower(concat('%', cast(:target as string), '%')))
              and a.occurredAt >= :from
              and a.occurredAt <= :to
            """)
    Page<AuditLog> search(@Param("eventType") EventType eventType,
                          @Param("actor") String actor,
                          @Param("target") String target,
                          @Param("from") OffsetDateTime from,
                          @Param("to") OffsetDateTime to,
                          Pageable pageable);
}
