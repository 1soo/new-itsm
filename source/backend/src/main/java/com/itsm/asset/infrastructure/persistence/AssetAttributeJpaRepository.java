package com.itsm.asset.infrastructure.persistence;

import com.itsm.asset.domain.AssetAttribute;
import com.itsm.asset.domain.repository.AssetAttributeRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetAttributeJpaRepository
        extends JpaRepository<AssetAttribute, Long>, AssetAttributeRepository {

    @Override
    List<AssetAttribute> findByAssetId(Long assetId);

    @Override
    void deleteByAssetId(Long assetId);
}
