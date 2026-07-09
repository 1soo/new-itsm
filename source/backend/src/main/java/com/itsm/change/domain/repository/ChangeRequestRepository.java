package com.itsm.change.domain.repository;

import com.itsm.change.domain.ChangeRequest;
import com.itsm.change.domain.ChangeRisk;
import com.itsm.change.domain.ChangeStatus;
import com.itsm.change.domain.ChangeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 변경 요청 저장소 포트.
 */
public interface ChangeRequestRepository {

    ChangeRequest save(ChangeRequest change);

    Optional<ChangeRequest> findById(Long id);

    Page<ChangeRequest> search(ChangeType type, ChangeStatus status, ChangeRisk risk,
                               OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    long countByTicketKeyStartingWith(String prefix);

    List<ChangeRequest> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);

    List<ChangeRequest> findSchedule(ChangeType type, OffsetDateTime from, OffsetDateTime to);
}
