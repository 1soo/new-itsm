package com.itsm.asset.domain.repository;

import com.itsm.asset.domain.Asset;
import com.itsm.asset.domain.AssetStatus;
import com.itsm.asset.domain.AssetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 자산 저장소 포트.
 */
public interface AssetRepository {

    Asset save(Asset asset);

    Optional<Asset> findById(Long id);

    Page<Asset> search(AssetType type, AssetStatus status, String owner, String keyword,
                       boolean filterExpiring, LocalDate expiringThreshold, Pageable pageable);

    long countByAssetKeyStartingWith(String prefix);

    List<Asset> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);
}
