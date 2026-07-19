package com.itsm.esm.application;

import tools.jackson.databind.ObjectMapper;
import com.itsm.auth.domain.Department;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.form.FormJsonMapper;
import com.itsm.common.security.SecurityUtils;
import com.itsm.esm.application.dto.CatalogItemDetailResponse;
import com.itsm.esm.application.dto.CatalogItemSummaryResponse;
import com.itsm.esm.application.dto.ChecklistTemplateTaskDto;
import com.itsm.esm.application.dto.CreateCatalogItemRequest;
import com.itsm.esm.application.dto.UpdateCatalogItemRequest;
import com.itsm.esm.domain.EsmCatalogItem;
import com.itsm.esm.domain.EsmChecklistTemplateTask;
import com.itsm.esm.domain.repository.EsmCatalogItemRepository;
import com.itsm.esm.domain.repository.EsmChecklistTemplateTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 부서 카탈로그 유스케이스(API-ESM-001~004). 조회는 인증 사용자 전반, 생성/수정은 PROCESS_OWNER 전용.
 * 동적 양식(formSchema)은 SRM과 완전히 동일한 자체 8×n 그리드 스키마({components,labels})를 통째로
 * JSONB에 저장한다(2026-07-19 유지보수 요청, 레거시 EAV 폐기, 공용 FormJsonMapper 재사용).
 */
@Service
public class EsmCatalogService {

    private static final String PROCESS_OWNER = "PROCESS_OWNER";
    private static final String DEFAULT_FORM_SCHEMA = "{\"components\":[],\"labels\":[]}";

    private final EsmCatalogItemRepository catalogItemRepository;
    private final EsmChecklistTemplateTaskRepository templateTaskRepository;
    private final ObjectMapper objectMapper;

    public EsmCatalogService(EsmCatalogItemRepository catalogItemRepository,
                             EsmChecklistTemplateTaskRepository templateTaskRepository,
                             ObjectMapper objectMapper) {
        this.catalogItemRepository = catalogItemRepository;
        this.templateTaskRepository = templateTaskRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<CatalogItemSummaryResponse> list(Department department, String keyword) {
        return catalogItemRepository.search(department, keyword).stream()
                .map(i -> new CatalogItemSummaryResponse(i.getId(), i.getName(), i.getDescription(),
                        i.getDepartment(), i.getChecklistTemplateType()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogItemDetailResponse detail(Long id) {
        return toDetail(findItem(id));
    }

    @Transactional
    public CatalogItemDetailResponse create(CreateCatalogItemRequest request) {
        requireProcessOwner();
        requireNotIt(request.department());
        EsmCatalogItem item = catalogItemRepository.save(new EsmCatalogItem(
                request.name(), request.description(), request.department(), request.checklistTemplateType(),
                writeSchema(request.formSchema())));
        saveTemplateTasks(item.getId(), request.checklistTemplate());
        return toDetail(item);
    }

    @Transactional
    public CatalogItemDetailResponse update(Long id, UpdateCatalogItemRequest request) {
        requireProcessOwner();
        requireNotIt(request.department());
        EsmCatalogItem item = findItem(id);
        item.update(request.name(), request.description(), request.department(), request.checklistTemplateType(),
                request.formSchema() != null ? writeSchema(request.formSchema()) : null);
        catalogItemRepository.save(item);
        if (request.checklistTemplate() != null) {
            templateTaskRepository.deleteByCatalogItemId(id);
            saveTemplateTasks(id, request.checklistTemplate());
        }
        return toDetail(item);
    }

    private void saveTemplateTasks(Long itemId, List<ChecklistTemplateTaskDto> tasks) {
        if (tasks == null) {
            return;
        }
        int order = 0;
        for (ChecklistTemplateTaskDto t : tasks) {
            templateTaskRepository.save(new EsmChecklistTemplateTask(itemId, t.department(), t.taskDescription(), order++));
        }
    }

    private EsmCatalogItem findItem(Long id) {
        return catalogItemRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.ESM_CATALOG_ITEM_NOT_FOUND));
    }

    private CatalogItemDetailResponse toDetail(EsmCatalogItem item) {
        List<ChecklistTemplateTaskDto> template = templateTaskRepository
                .findByCatalogItemIdOrderBySortOrderAsc(item.getId()).stream()
                .map(t -> new ChecklistTemplateTaskDto(t.getDepartment(), t.getTaskDescription()))
                .toList();
        return new CatalogItemDetailResponse(item.getId(), item.getName(), item.getDescription(),
                item.getDepartment(), item.getChecklistTemplateType(), template, readSchema(item.getFormSchema()));
    }

    private void requireProcessOwner() {
        if (!SecurityUtils.hasRole(PROCESS_OWNER)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    /** ESM 카탈로그는 IT 부서를 다루지 않는다(IT는 기존 SRM 유지, esm_catalog_item DB CHECK와 일치). */
    private void requireNotIt(Department department) {
        if (department == Department.IT) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "ESM 카탈로그는 IT 부서를 사용할 수 없습니다(SRM 이용).");
        }
    }

    private String writeSchema(Map<String, Object> schema) {
        return FormJsonMapper.writeJson(objectMapper, schema, DEFAULT_FORM_SCHEMA, "formSchema 직렬화 실패");
    }

    private Map<String, Object> readSchema(String json) {
        return FormJsonMapper.readJsonMap(objectMapper, json);
    }
}
