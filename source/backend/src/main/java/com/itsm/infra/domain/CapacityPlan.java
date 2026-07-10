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
 * 팀/서비스별 용량 계획. 활용률(demand/capacity)은 저장하지 않는 조회 시점 계산값이다.
 */
@Getter
@Entity
@Table(name = "capacity_plan")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CapacityPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "team_or_service", nullable = false, length = 150)
    private String teamOrService;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal capacity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal demand;

    public CapacityPlan(String teamOrService, BigDecimal capacity, BigDecimal demand) {
        this.teamOrService = teamOrService;
        this.capacity = capacity;
        this.demand = demand;
    }
}
