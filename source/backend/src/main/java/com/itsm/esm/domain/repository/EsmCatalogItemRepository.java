package com.itsm.esm.domain.repository;

import com.itsm.auth.domain.Department;
import com.itsm.esm.domain.EsmCatalogItem;

import java.util.List;
import java.util.Optional;

/**
 * 부서 카탈로그 항목 저장소 포트.
 */
public interface EsmCatalogItemRepository {

    EsmCatalogItem save(EsmCatalogItem item);

    Optional<EsmCatalogItem> findById(Long id);

    List<EsmCatalogItem> search(Department department, String keyword);
}
