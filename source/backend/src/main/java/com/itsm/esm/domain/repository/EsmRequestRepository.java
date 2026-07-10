package com.itsm.esm.domain.repository;

import com.itsm.auth.domain.Department;
import com.itsm.esm.domain.EsmRequest;
import com.itsm.esm.domain.EsmRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 부서 요청 저장소 포트.
 */
public interface EsmRequestRepository {

    EsmRequest save(EsmRequest request);

    Optional<EsmRequest> findById(Long id);

    Optional<EsmRequest> findByChecklistId(Long checklistId);

    Page<EsmRequest> search(Long requesterId, Department department, EsmRequestStatus status,
                            OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    long countByTicketKeyStartingWith(String prefix);

    List<EsmRequest> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);
}
