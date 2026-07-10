package com.itsm.esm.infrastructure.persistence;

import com.itsm.auth.domain.Department;
import com.itsm.esm.domain.EsmRequest;
import com.itsm.esm.domain.EsmRequestStatus;
import com.itsm.esm.domain.repository.EsmRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface EsmRequestJpaRepository extends JpaRepository<EsmRequest, Long>, EsmRequestRepository {

    @Override
    Optional<EsmRequest> findByChecklistId(Long checklistId);

    @Override
    List<EsmRequest> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);

    @Override
    @Query("""
            select r from EsmRequest r
            where r.isDeleted = false
              and (:requesterId is null or r.requesterId = :requesterId)
              and (:department is null or r.department = :department)
              and (:status is null or r.status = :status)
              and r.createdAt >= :from and r.createdAt <= :to
            """)
    Page<EsmRequest> search(@Param("requesterId") Long requesterId,
                            @Param("department") Department department,
                            @Param("status") EsmRequestStatus status,
                            @Param("from") OffsetDateTime from,
                            @Param("to") OffsetDateTime to,
                            Pageable pageable);
}
