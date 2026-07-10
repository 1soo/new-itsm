package com.itsm.infra.infrastructure.persistence;

import com.itsm.infra.domain.InfraMetricThreshold;
import com.itsm.infra.domain.MetricType;
import com.itsm.infra.domain.repository.InfraMetricThresholdRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InfraMetricThresholdJpaRepository
        extends JpaRepository<InfraMetricThreshold, Long>, InfraMetricThresholdRepository {

    @Override
    Optional<InfraMetricThreshold> findByMetricType(MetricType metricType);

    @Override
    List<InfraMetricThreshold> findAll();
}
