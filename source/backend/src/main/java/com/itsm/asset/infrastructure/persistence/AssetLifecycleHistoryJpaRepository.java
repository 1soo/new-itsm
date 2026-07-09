package com.itsm.asset.infrastructure.persistence;

import com.itsm.asset.domain.AssetLifecycleHistory;
import com.itsm.asset.domain.repository.AssetLifecycleHistoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetLifecycleHistoryJpaRepository
        extends JpaRepository<AssetLifecycleHistory, Long>, AssetLifecycleHistoryRepository {

    @Override
    List<AssetLifecycleHistory> findByAssetIdOrderByChangedAtAsc(Long assetId);
}
