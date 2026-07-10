package com.itsm.infra.domain;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 자산별 가동률 목표(SLA). 자산당 1건(UNIQUE), upsert로 관리한다.
 */
@Getter
@Entity
@Table(name = "uptime_target")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UptimeTarget extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false, unique = true)
    private Long assetId;

    @Column(name = "target_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal targetPercentage;

    public UptimeTarget(Long assetId, BigDecimal targetPercentage) {
        this.assetId = assetId;
        this.targetPercentage = targetPercentage;
    }

    public void update(BigDecimal targetPercentage) {
        this.targetPercentage = targetPercentage;
    }
}
