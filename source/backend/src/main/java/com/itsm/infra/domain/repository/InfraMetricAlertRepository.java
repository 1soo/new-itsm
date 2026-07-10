package com.itsm.infra.domain.repository;

import com.itsm.infra.domain.InfraMetricAlert;

import java.util.List;
import java.util.Optional;

/**
 * 임계치 초과 알림 저장소 포트.
 */
public interface InfraMetricAlertRepository {

    InfraMetricAlert save(InfraMetricAlert alert);

    Optional<InfraMetricAlert> findById(Long id);

    List<InfraMetricAlert> search(Long assetId, Boolean acknowledged);
}
