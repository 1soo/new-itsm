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

    List<ServiceCatalogItem> search(Long categoryId, String keyword);

    /** 지정 카테고리를 참조하는 카탈로그 항목 수(카테고리 목록 itemCount, 삭제 시 CATEGORY_IN_USE 판정). */
    long countByCategoryId(Long categoryId);
}
