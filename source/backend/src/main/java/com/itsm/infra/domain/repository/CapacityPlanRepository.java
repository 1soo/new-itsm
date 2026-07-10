package com.itsm.infra.domain.repository;

import com.itsm.infra.domain.CapacityPlan;

import java.util.List;

/**
 * 팀/서비스별 용량 계획 저장소 포트.
 */
public interface CapacityPlanRepository {

    CapacityPlan save(CapacityPlan plan);

    List<CapacityPlan> findAll();
}
