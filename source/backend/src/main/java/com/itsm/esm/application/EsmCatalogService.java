package com.itsm.esm.application;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.itsm.auth.domain.Department;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import com.itsm.esm.application.dto.CatalogItemDetailResponse;
import com.itsm.esm.application.dto.CatalogItemSummaryResponse;
import com.itsm.esm.application.dto.ChecklistTemplateTaskDto;
import com.itsm.esm.application.dto.CreateCatalogItemRequest;
import com.itsm.esm.application.dto.FormFieldDto;
import com.itsm.esm.application.dto.UpdateCatalogItemRequest;
import com.itsm.esm.domain.EsmCatalogFormField;
import com.itsm.esm.domain.EsmCatalogItem;
import com.itsm.esm.domain.EsmChecklistTemplateTask;
import com.itsm.esm.domain.repository.EsmCatalogFormFieldRepository;
import com.itsm.esm.domain.repository.EsmCatalogItemRepository;
import com.itsm.esm.domain.repository.EsmChecklistTemplateTaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 부서 카탈로그 유스케이스(API-ESM-001~004). 조회는 인증 사용자 전반, 생성/수정은 PROCESS_OWNER 전용.
 */
@Service
public class EsmCatalogService {

    private static final String PROCESS_OWNER = "PROCESS_OWNER";

    private final EsmCatalogItemRepository catalogItemRepository;
    private final EsmCatalogFormFieldRepository formFieldRepository;
    private final EsmChecklistTemplateTaskRepository templateTaskRepository;
    private final ObjectMapper objectMapper;

    public EsmCatalogService(EsmCatalogItemRepository catalogItemRepository,
                             EsmCatalogFormFieldRepository formFieldRepository,
                             EsmChecklistTemplateTaskRepository templateTaskRepository,
                             ObjectMapper objectMapper) {
        this.catalogItemRepository = catalogItemRepository;
        this.formFieldRepository = formFieldRepository;
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
                request.name(), request.description(), request.department(), request.checklistTemplateType()));
        saveTemplateTasks(item.getId(), request.checklistTemplate());
        saveFields(item.getId(), request.formSchema());
        return toDetail(item);
    }

    @Transactional
    public CatalogItemDetailResponse update(Long id, UpdateCatalogItemRequest request) {
        requireProcessOwner();
        requireNotIt(request.department());
        EsmCatalogItem item = findItem(id);
        item.update(request.name(), request.description(), request.department(), request.checklistTemplateType());
        catalogItemRepository.save(item);
        if (request.checklistTemplate() != null) {
            templateTaskRepository.deleteByCatalogItemId(id);
            saveTemplateTasks(id, request.checklistTemplate());
        }
        if (request.formSchema() != null) {
            formFieldRepository.deleteByCatalogItemId(id);
            saveFields(id, request.formSchema());
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

    private void saveFields(Long itemId, List<FormFieldDto> schema) {
        if (schema == null) {
            return;
        }
        int order = 0;
        for (FormFieldDto f : schema) {
            formFieldRepository.save(new EsmCatalogFormField(
                    itemId, f.key(), f.label(), f.type(), f.required(), writeOptions(f.options()), order++));
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
        List<FormFieldDto> schema = formFieldRepository.findByCatalogItemIdOrderBySortOrderAsc(item.getId()).stream()
                .map(f -> new FormFieldDto(f.getFieldKey(), f.getLabel(), f.getFieldType(), f.isRequired(),
                        readOptions(f.getOptions())))
                .toList();
        return new CatalogItemDetailResponse(item.getId(), item.getName(), item.getDescription(),
                item.getDepartment(), item.getChecklistTemplateType(), template, schema);
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

    private String writeOptions(List<String> options) {
        if (options == null || options.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(options);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "options 직렬화 실패");
        }
    }

    private List<String> readOptions(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
