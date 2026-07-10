package com.itsm.esm.domain.repository;

import com.itsm.esm.domain.EsmChecklistTemplateTask;

import java.util.List;

/**
 * 카탈로그 항목 체크리스트 하위 작업 템플릿 저장소 포트.
 */
public interface EsmChecklistTemplateTaskRepository {

    EsmChecklistTemplateTask save(EsmChecklistTemplateTask task);

    List<EsmChecklistTemplateTask> findByCatalogItemIdOrderBySortOrderAsc(Long catalogItemId);

    void deleteByCatalogItemId(Long catalogItemId);
}
