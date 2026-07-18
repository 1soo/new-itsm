package com.itsm.srm.application;

import tools.jackson.databind.ObjectMapper;
import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.repository.RoleRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.form.FormJsonMapper;
import com.itsm.srm.application.dto.CatalogItemDetailResponse;
import com.itsm.srm.application.dto.CatalogItemSummaryResponse;
import com.itsm.srm.application.dto.CreateCatalogItemRequest;
import com.itsm.srm.application.dto.KnowledgeSuggestionResponse;
import com.itsm.srm.application.dto.UpdateCatalogItemRequest;
import com.itsm.srm.domain.ServiceCatalogCategory;
import com.itsm.srm.domain.ServiceCatalogItem;
import com.itsm.srm.domain.repository.ServiceCatalogCategoryRepository;
import com.itsm.srm.domain.repository.ServiceCatalogItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 서비스 카탈로그 유스케이스 (조회는 인증 사용자, 생성/수정은 PROCESS_OWNER).
 * 승인 필요 여부·담당 역할은 더 이상 카탈로그 항목의 속성이 아니다(승인 프로세스 커스텀 기능으로 대체).
 * 동적 양식(formSchema)은 자체 8×n 그리드 스키마(components 배열)를 통째로 JSONB에 저장한다(2026-07-18 유지보수 요청, form.io 완전 제거).
 */
@Service
public class ServiceCatalogService {

    private static final String DEFAULT_FORM_SCHEMA = "{\"components\":[]}";

    private final ServiceCatalogItemRepository catalogItemRepository;
    private final ServiceCatalogCategoryRepository categoryRepository;
    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    public ServiceCatalogService(ServiceCatalogItemRepository catalogItemRepository,
                                 ServiceCatalogCategoryRepository categoryRepository,
                                 RoleRepository roleRepository,
                                 ObjectMapper objectMapper) {
        this.catalogItemRepository = catalogItemRepository;
        this.categoryRepository = categoryRepository;
        this.roleRepository = roleRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<CatalogItemSummaryResponse> list(Long categoryId, String keyword) {
        return catalogItemRepository.search(categoryId, keyword).stream()
                .map(i -> new CatalogItemSummaryResponse(i.getId(), i.getName(), i.getDescription(),
                        i.getCategoryId(), categoryName(i.getCategoryId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogItemDetailResponse detail(Long id) {
        return toDetail(findItem(id));
    }

    @Transactional
    public CatalogItemDetailResponse create(CreateCatalogItemRequest request) {
        assertCategoryExists(request.categoryId());
        ServiceCatalogItem item = catalogItemRepository.save(new ServiceCatalogItem(
                request.name(), request.description(), request.categoryId(),
                request.slaResponseMinutes(), request.slaResolveMinutes(),
                request.assigneeRoleId(), writeSchema(request.formSchema())));
        return toDetail(item);
    }

    @Transactional
    public CatalogItemDetailResponse update(Long id, UpdateCatalogItemRequest request) {
        assertCategoryExists(request.categoryId());
        ServiceCatalogItem item = findItem(id);
        item.update(request.name(), request.description(), request.categoryId(),
                request.slaResponseMinutes(), request.slaResolveMinutes(),
                request.assigneeRoleId(),
                request.formSchema() != null ? writeSchema(request.formSchema()) : null);
        catalogItemRepository.save(item);
        return toDetail(item);
    }

    private void assertCategoryExists(Long categoryId) {
        if (categoryId != null && categoryRepository.findById(categoryId).filter(c -> !c.isDeleted()).isEmpty()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
    }

    /** 지식 기사 추천. KM 도메인 미구축이라 빈 배열 반환(TODO: KM 구축 후 연동). */
    @Transactional(readOnly = true)
    public List<KnowledgeSuggestionResponse> suggestions(Long catalogItemId, String keyword) {
        return List.of();
    }

    private ServiceCatalogItem findItem(Long id) {
        return catalogItemRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATALOG_ITEM_NOT_FOUND));
    }

    private CatalogItemDetailResponse toDetail(ServiceCatalogItem item) {
        String assigneeRoleName = item.getAssigneeRoleId() == null ? null
                : roleRepository.findById(item.getAssigneeRoleId()).map(Role::getRoleName).orElse(null);
        return new CatalogItemDetailResponse(item.getId(), item.getName(), item.getDescription(),
                item.getCategoryId(), categoryName(item.getCategoryId()),
                item.getSlaResponseMinutes(), item.getSlaResolveMinutes(),
                item.getAssigneeRoleId(), assigneeRoleName, readSchema(item.getFormSchema()));
    }

    private String categoryName(Long categoryId) {
        return categoryId == null ? null
                : categoryRepository.findById(categoryId).map(ServiceCatalogCategory::getName).orElse(null);
    }

    private String writeSchema(Map<String, Object> schema) {
        return FormJsonMapper.writeJson(objectMapper, schema, DEFAULT_FORM_SCHEMA, "formSchema 직렬화 실패");
    }

    private Map<String, Object> readSchema(String json) {
        return FormJsonMapper.readJsonMap(objectMapper, json);
    }
}
