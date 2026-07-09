package com.itsm.srm.domain.repository;

import com.itsm.srm.domain.ServiceCatalogItem;

import java.util.List;
import java.util.Optional;

/**
 * 서비스 카탈로그 항목 저장소 포트.
 */
public interface ServiceCatalogItemRepository {

    ServiceCatalogItem save(ServiceCatalogItem item);

    Optional<ServiceCatalogItem> findById(Long id);

    List<ServiceCatalogItem> search(String category, String keyword);
}
