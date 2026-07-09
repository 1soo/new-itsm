package com.itsm.srm.domain.repository;

import com.itsm.srm.domain.CatalogFormField;

import java.util.List;

/**
 * 카탈로그 동적 양식 필드 저장소 포트.
 */
public interface CatalogFormFieldRepository {

    CatalogFormField save(CatalogFormField field);

    List<CatalogFormField> findByCatalogItemIdOrderBySortOrderAsc(Long catalogItemId);

    void deleteByCatalogItemId(Long catalogItemId);
}
