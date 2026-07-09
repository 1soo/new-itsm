package com.itsm.asset.domain.repository;

import com.itsm.asset.domain.ConfigurationItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 구성 항목(CI) 저장소 포트.
 */
public interface ConfigurationItemRepository {

    ConfigurationItem save(ConfigurationItem ci);

    Optional<ConfigurationItem> findById(Long id);

    Page<ConfigurationItem> search(String keyword, String type, Pageable pageable);

    List<ConfigurationItem> findByAssetId(Long assetId);
}
