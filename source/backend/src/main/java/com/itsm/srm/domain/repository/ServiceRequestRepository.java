package com.itsm.srm.domain.repository;

import com.itsm.srm.domain.RequestStatus;
import com.itsm.srm.domain.ServiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 서비스 요청 저장소 포트.
 */
public interface ServiceRequestRepository {

    ServiceRequest save(ServiceRequest request);

    Optional<ServiceRequest> findById(Long id);

    Page<ServiceRequest> search(Long requesterId, Long queueId, RequestStatus status,
                                OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    long countByTicketKeyStartingWith(String prefix);

    List<ServiceRequest> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);

    /** 큐의 미종료(CLOSED/REJECTED 제외) 요청 건수. */
    long countOpenByQueueId(Long queueId);
}
