package com.itsm.infra.domain.repository;

import com.itsm.infra.domain.InfraMetricThreshold;
import com.itsm.infra.domain.MetricType;

import java.util.List;
import java.util.Optional;

/**
 * 지표 항목(전역) 임계치 저장소 포트.
 */
public interface InfraMetricThresholdRepository {

    InfraMetricThreshold save(InfraMetricThreshold threshold);

    Optional<InfraMetricThreshold> findByMetricType(MetricType metricType);

    List<InfraMetricThreshold> findAll();
}
