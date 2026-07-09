package com.itsm.srm.infrastructure.persistence;

import com.itsm.srm.domain.RequestStatus;
import com.itsm.srm.domain.ServiceRequest;
import com.itsm.srm.domain.repository.ServiceRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface ServiceRequestJpaRepository extends JpaRepository<ServiceRequest, Long>, ServiceRequestRepository {

    @Override
    @Query("""
            select r from ServiceRequest r
            where r.isDeleted = false
              and (:requesterId is null or r.requesterId = :requesterId)
              and (:queueId is null or r.queueId = :queueId)
              and (:status is null or r.status = :status)
              and r.createdAt >= :from and r.createdAt <= :to
            """)
    Page<ServiceRequest> search(@Param("requesterId") Long requesterId,
                                @Param("queueId") Long queueId,
                                @Param("status") RequestStatus status,
                                @Param("from") OffsetDateTime from,
                                @Param("to") OffsetDateTime to,
                                Pageable pageable);

    @Override
    @Query("""
            select count(r) from ServiceRequest r
            where r.isDeleted = false and r.queueId = :queueId
              and r.status not in (com.itsm.srm.domain.RequestStatus.CLOSED, com.itsm.srm.domain.RequestStatus.REJECTED)
            """)
    long countOpenByQueueId(@Param("queueId") Long queueId);
}
