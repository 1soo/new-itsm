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

/**
 * 임계치 초과 알림. 지표 등록 시점에 전역 임계치를 초과하면 생성된다(asset_id/metric_type은 조회 성능용 비정규화).
 */
@Getter
@Entity
@Table(name = "infra_metric_alert")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InfraMetricAlert extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_id", nullable = false)
    private Long metricId;

    @Column(name = "asset_id", nullable = false)
    private Long assetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 20)
    private MetricType metricType;

    @Column(name = "breached_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal breachedValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "threshold_type", nullable = false, length = 10)
    private ThresholdType thresholdType;

    @Column(nullable = false)
    private boolean acknowledged = false;

    public InfraMetricAlert(Long metricId, Long assetId, MetricType metricType, BigDecimal breachedValue,
                             ThresholdType thresholdType) {
        this.metricId = metricId;
        this.assetId = assetId;
        this.metricType = metricType;
        this.breachedValue = breachedValue;
        this.thresholdType = thresholdType;
        this.acknowledged = false;
    }

    public void acknowledge() {
        this.acknowledged = true;
    }
}
