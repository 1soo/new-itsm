package com.itsm.infra.domain.repository;

import com.itsm.infra.domain.InfraMetric;
import com.itsm.infra.domain.MetricType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 인프라 지표 저장소 포트.
 */
public interface InfraMetricRepository {

    InfraMetric save(InfraMetric metric);

    Optional<InfraMetric> findById(Long id);

    /**
     * from/to는 호출측에서 null을 원거리 상한/하한(EPOCH·FAR_FUTURE)으로 치환해 항상 값을 전달한다
     * (nullable timestamptz 파라미터의 "IS NULL" 비교는 PostgreSQL이 타입을 추론하지 못해 오류가 난다).
     */
    List<InfraMetric> search(Long assetId, MetricType metricType, OffsetDateTime from, OffsetDateTime to);

    /**
     * (assetId nullable, metricType 고정) 구간 평균값. 데이터 없으면 null. from/to 처리는 search와 동일.
     */
    Double average(Long assetId, MetricType metricType, OffsetDateTime from, OffsetDateTime to);
}
