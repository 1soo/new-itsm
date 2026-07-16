package com.itsm.srm.application;

import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.srm.application.dto.CategoryCreateRequest;
import com.itsm.srm.application.dto.CategoryListResponse;
import com.itsm.srm.application.dto.CategoryResponse;
import com.itsm.srm.application.dto.CategoryUpdateRequest;
import com.itsm.srm.domain.ServiceCatalogCategory;
import com.itsm.srm.domain.repository.ServiceCatalogCategoryRepository;
import com.itsm.srm.domain.repository.ServiceCatalogItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 서비스 카탈로그 카테고리 유스케이스(조회는 인증 사용자, 생성/수정/삭제는 PROCESS_OWNER, 2026-07-16 유지보수 요청).
 */
@Service
public class ServiceCatalogCategoryService {

    private final ServiceCatalogCategoryRepository categoryRepository;
    private final ServiceCatalogItemRepository catalogItemRepository;

    public ServiceCatalogCategoryService(ServiceCatalogCategoryRepository categoryRepository,
                                         ServiceCatalogItemRepository catalogItemRepository) {
        this.categoryRepository = categoryRepository;
        this.catalogItemRepository = catalogItemRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryListResponse> list() {
        return categoryRepository.findAllOrderBySortOrderAsc().stream()
                .map(c -> new CategoryListResponse(c.getId(), c.getName(), c.getSortOrder(),
                        catalogItemRepository.countByCategoryId(c.getId())))
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryCreateRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }
        ServiceCatalogCategory saved = categoryRepository.save(
                new ServiceCatalogCategory(request.name(), request.sortOrder()));
        return toResponse(saved);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryUpdateRequest request) {
        ServiceCatalogCategory category = findCategory(id);
        if (request.name() != null && categoryRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }
        category.update(request.name(), request.sortOrder());
        categoryRepository.save(category);
        return toResponse(category);
    }

    @Transactional
    public void delete(Long id) {
        ServiceCatalogCategory category = findCategory(id);
        if (catalogItemRepository.countByCategoryId(id) > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_IN_USE);
        }
        category.markDeleted();
        categoryRepository.save(category);
    }

    private ServiceCatalogCategory findCategory(Long id) {
        return categoryRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private CategoryResponse toResponse(ServiceCatalogCategory c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getSortOrder());
    }
}
