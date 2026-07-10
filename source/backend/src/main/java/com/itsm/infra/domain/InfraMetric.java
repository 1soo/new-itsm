package com.itsm.infra.domain;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 인프라 지표 레코드(수동 입력). 자산·지표 항목·측정 시각·값의 단순 시계열 append 데이터.
 */
@Getter
@Entity
@Table(name = "infra_metric")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InfraMetric extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 20)
    private MetricType metricType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "measured_at", nullable = false)
    private OffsetDateTime measuredAt;

    public InfraMetric(Long assetId, MetricType metricType, BigDecimal value, OffsetDateTime measuredAt) {
        this.assetId = assetId;
        this.metricType = metricType;
        this.value = value;
        this.measuredAt = measuredAt;
    }
}
