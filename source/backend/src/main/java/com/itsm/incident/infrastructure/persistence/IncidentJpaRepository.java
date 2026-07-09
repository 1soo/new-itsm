package com.itsm.incident.infrastructure.persistence;

import com.itsm.incident.domain.Incident;
import com.itsm.incident.domain.IncidentStatus;
import com.itsm.incident.domain.Severity;
import com.itsm.incident.domain.repository.IncidentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface IncidentJpaRepository extends JpaRepository<Incident, Long>, IncidentRepository {

    @Override
    @Query("""
            select distinct i from Incident i
            where i.isDeleted = false
              and (:status is null or i.status = :status)
              and (:severity is null or i.severity = :severity)
              and (:keyword is null or lower(i.summary) like lower(concat('%', cast(:keyword as string), '%')))
              and i.createdAt >= :from and i.createdAt <= :to
              and (:assigneeId is null or exists (
                    select 1 from IncidentResponder r where r.incidentId = i.id and r.userId = :assigneeId))
            """)
    Page<Incident> search(@Param("status") IncidentStatus status,
                          @Param("severity") Severity severity,
                          @Param("assigneeId") Long assigneeId,
                          @Param("keyword") String keyword,
                          @Param("from") OffsetDateTime from,
                          @Param("to") OffsetDateTime to,
                          Pageable pageable);
}
