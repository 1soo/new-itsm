package com.itsm.asset.domain.repository;

import com.itsm.asset.domain.AssetLifecycleHistory;

import java.util.List;

/**
 * 자산 생애주기 이력 저장소 포트.
 */
public interface AssetLifecycleHistoryRepository {

    AssetLifecycleHistory save(AssetLifecycleHistory history);

    List<AssetLifecycleHistory> findByAssetIdOrderByChangedAtAsc(Long assetId);
}
