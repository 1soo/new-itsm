package com.itsm.asset.domain.repository;

import com.itsm.asset.domain.AssetAttribute;

import java.util.List;

/**
 * 자산 속성(EAV) 저장소 포트.
 */
public interface AssetAttributeRepository {

    AssetAttribute save(AssetAttribute attribute);

    List<AssetAttribute> findByAssetId(Long assetId);

    void deleteByAssetId(Long assetId);
}
