package com.itsm.infra.infrastructure.persistence;

import com.itsm.infra.domain.UptimeTarget;
import com.itsm.infra.domain.repository.UptimeTargetRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UptimeTargetJpaRepository extends JpaRepository<UptimeTarget, Long>, UptimeTargetRepository {

    @Override
    Optional<UptimeTarget> findByAssetId(Long assetId);
}
