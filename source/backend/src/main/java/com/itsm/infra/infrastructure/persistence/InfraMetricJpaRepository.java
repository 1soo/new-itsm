package com.itsm.infra.infrastructure.persistence;

import com.itsm.infra.domain.InfraMetric;
import com.itsm.infra.domain.MetricType;
import com.itsm.infra.domain.repository.InfraMetricRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface InfraMetricJpaRepository extends JpaRepository<InfraMetric, Long>, InfraMetricRepository {

    @Override
    @Query("""
            select m from InfraMetric m
            where m.isDeleted = false
              and (:assetId is null or m.assetId = :assetId)
              and (:metricType is null or m.metricType = :metricType)
              and m.measuredAt >= :from
              and m.measuredAt <= :to
            order by m.measuredAt asc
            """)
    List<InfraMetric> search(@Param("assetId") Long assetId, @Param("metricType") MetricType metricType,
                              @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    @Override
    @Query("""
            select avg(m.value) from InfraMetric m
            where m.isDeleted = false
              and (:assetId is null or m.assetId = :assetId)
              and m.metricType = :metricType
              and m.measuredAt >= :from
              and m.measuredAt <= :to
            """)
    Double average(@Param("assetId") Long assetId, @Param("metricType") MetricType metricType,
                    @Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
}
