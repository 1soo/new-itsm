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
 * 지표 항목(전역) 단위 임계치. metricType당 1건(UNIQUE), upsert로 관리한다.
 */
@Getter
@Entity
@Table(name = "infra_metric_threshold")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InfraMetricThreshold extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, unique = true, length = 20)
    private MetricType metricType;

    @Column(name = "upper_limit", precision = 10, scale = 2)
    private BigDecimal upperLimit;

    @Column(name = "lower_limit", precision = 10, scale = 2)
    private BigDecimal lowerLimit;

    public InfraMetricThreshold(MetricType metricType, BigDecimal upperLimit, BigDecimal lowerLimit) {
        this.metricType = metricType;
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
    }

    public void update(BigDecimal upperLimit, BigDecimal lowerLimit) {
        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;
    }
}
