package com.itsm.infra.infrastructure.persistence;

import com.itsm.infra.domain.CapacityPlan;
import com.itsm.infra.domain.repository.CapacityPlanRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CapacityPlanJpaRepository extends JpaRepository<CapacityPlan, Long>, CapacityPlanRepository {

    @Override
    List<CapacityPlan> findAll();
}
