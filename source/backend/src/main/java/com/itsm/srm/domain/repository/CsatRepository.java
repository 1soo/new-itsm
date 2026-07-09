package com.itsm.srm.domain.repository;

import com.itsm.srm.domain.Csat;

import java.util.List;
import java.util.Optional;

/**
 * CSAT 저장소 포트.
 */
public interface CsatRepository {

    Csat save(Csat csat);

    boolean existsByServiceRequestId(Long serviceRequestId);

    Optional<Csat> findByServiceRequestId(Long serviceRequestId);

    List<Csat> findByServiceRequestIdIn(List<Long> serviceRequestIds);
}
