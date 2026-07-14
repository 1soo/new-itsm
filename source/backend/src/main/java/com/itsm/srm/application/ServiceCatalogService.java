package com.itsm.srm.application;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.srm.application.dto.CatalogItemDetailResponse;
import com.itsm.srm.application.dto.CatalogItemSummaryResponse;
import com.itsm.srm.application.dto.CreateCatalogItemRequest;
import com.itsm.srm.application.dto.FormFieldDto;
import com.itsm.srm.application.dto.KnowledgeSuggestionResponse;
import com.itsm.srm.application.dto.UpdateCatalogItemRequest;
import com.itsm.srm.domain.CatalogFormField;
import com.itsm.srm.domain.ServiceCatalogItem;
import com.itsm.srm.domain.repository.CatalogFormFieldRepository;
import com.itsm.srm.domain.repository.ServiceCatalogItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 서비스 카탈로그 유스케이스 (조회는 인증 사용자, 생성/수정은 PROCESS_OWNER).
 * 승인 필요 여부·담당 역할은 더 이상 카탈로그 항목의 속성이 아니다(승인 프로세스 커스텀 기능으로 대체).
 */
@Service
public class ServiceCatalogService {

    private final ServiceCatalogItemRepository catalogItemRepository;
    private final CatalogFormFieldRepository formFieldRepository;
    private final ObjectMapper objectMapper;

    public ServiceCatalogService(ServiceCatalogItemRepository catalogItemRepository,
                                 CatalogFormFieldRepository formFieldRepository,
                                 ObjectMapper objectMapper) {
        this.catalogItemRepository = catalogItemRepository;
        this.formFieldRepository = formFieldRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<CatalogItemSummaryResponse> list(String category, String keyword) {
        return catalogItemRepository.search(category, keyword).stream()
                .map(i -> new CatalogItemSummaryResponse(i.getId(), i.getName(), i.getDescription(), i.getCategory()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CatalogItemDetailResponse detail(Long id) {
        return toDetail(findItem(id));
    }

    @Transactional
    public CatalogItemDetailResponse create(CreateCatalogItemRequest request) {
        ServiceCatalogItem item = catalogItemRepository.save(new ServiceCatalogItem(
                request.name(), request.description(), null,
                request.queueId(), request.slaResponseMinutes(), request.slaResolveMinutes()));
        saveFields(item.getId(), request.formSchema());
        return toDetail(item);
    }

    @Transactional
    public CatalogItemDetailResponse update(Long id, UpdateCatalogItemRequest request) {
        ServiceCatalogItem item = findItem(id);
        item.update(request.name(), request.description(), null,
                request.queueId(), request.slaResponseMinutes(), request.slaResolveMinutes());
        catalogItemRepository.save(item);
        if (request.formSchema() != null) {
            formFieldRepository.deleteByCatalogItemId(id);
            saveFields(id, request.formSchema());
        }
        return toDetail(item);
    }

    /** 지식 기사 추천. KM 도메인 미구축이라 빈 배열 반환(TODO: KM 구축 후 연동). */
    @Transactional(readOnly = true)
    public List<KnowledgeSuggestionResponse> suggestions(Long catalogItemId, String keyword) {
        return List.of();
    }

    private void saveFields(Long itemId, List<FormFieldDto> schema) {
        int order = 0;
        for (FormFieldDto f : schema) {
            formFieldRepository.save(new CatalogFormField(
                    itemId, f.key(), f.label(), f.type(), f.required(), writeOptions(f.options()), order++));
        }
    }

    private ServiceCatalogItem findItem(Long id) {
        return catalogItemRepository.findById(id)
                .filter(i -> !i.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATALOG_ITEM_NOT_FOUND));
    }

    private CatalogItemDetailResponse toDetail(ServiceCatalogItem item) {
        List<FormFieldDto> schema = formFieldRepository.findByCatalogItemIdOrderBySortOrderAsc(item.getId()).stream()
                .map(f -> new FormFieldDto(f.getFieldKey(), f.getLabel(), f.getFieldType(), f.isRequired(),
                        readOptions(f.getOptions())))
                .toList();
        return new CatalogItemDetailResponse(item.getId(), item.getName(), item.getDescription(),
                item.getQueueId(), item.getSlaResponseMinutes(), item.getSlaResolveMinutes(), schema);
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
