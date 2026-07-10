package com.itsm.infra.domain.repository;

import com.itsm.infra.domain.UptimeTarget;

import java.util.Optional;

/**
 * 자산별 가동률 목표(SLA) 저장소 포트.
 */
public interface UptimeTargetRepository {

    UptimeTarget save(UptimeTarget target);

    Optional<UptimeTarget> findByAssetId(Long assetId);
}
