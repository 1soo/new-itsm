package com.itsm.srm.domain.repository;

import com.itsm.srm.domain.ServiceCatalogCategory;

import java.util.List;
import java.util.Optional;

/**
 * 서비스 카탈로그 카테고리 저장소 포트.
 */
public interface ServiceCatalogCategoryRepository {

    ServiceCatalogCategory save(ServiceCatalogCategory category);

    Optional<ServiceCatalogCategory> findById(Long id);

    List<ServiceCatalogCategory> findAllOrderBySortOrderAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
