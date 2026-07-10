package com.itsm.esm.domain.repository;

import com.itsm.esm.domain.EsmCatalogFormField;

import java.util.List;

/**
 * 부서 카탈로그 동적 양식 필드 저장소 포트.
 */
public interface EsmCatalogFormFieldRepository {

    EsmCatalogFormField save(EsmCatalogFormField field);

    List<EsmCatalogFormField> findByCatalogItemIdOrderBySortOrderAsc(Long catalogItemId);

    void deleteByCatalogItemId(Long catalogItemId);
}
