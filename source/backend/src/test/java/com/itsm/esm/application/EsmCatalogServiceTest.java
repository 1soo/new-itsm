package com.itsm.esm.application;

import tools.jackson.databind.ObjectMapper;
import com.itsm.auth.domain.Department;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.esm.application.dto.CreateCatalogItemRequest;
import com.itsm.esm.application.dto.UpdateCatalogItemRequest;
import com.itsm.esm.domain.ChecklistTemplateType;
import com.itsm.esm.domain.EsmCatalogItem;
import com.itsm.esm.domain.repository.EsmCatalogFormFieldRepository;
import com.itsm.esm.domain.repository.EsmCatalogItemRepository;
import com.itsm.esm.domain.repository.EsmChecklistTemplateTaskRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EsmCatalogServiceTest {

    @Mock EsmCatalogItemRepository catalogItemRepository;
    @Mock EsmCatalogFormFieldRepository formFieldRepository;
    @Mock EsmChecklistTemplateTaskRepository templateTaskRepository;

    EsmCatalogService service;

    @BeforeEach
    void setUp() {
        service = new EsmCatalogService(catalogItemRepository, formFieldRepository, templateTaskRepository,
                new ObjectMapper());
        when(catalogItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formFieldRepository.findByCatalogItemIdOrderBySortOrderAsc(any())).thenReturn(List.of());
        when(templateTaskRepository.findByCatalogItemIdOrderBySortOrderAsc(any())).thenReturn(List.of());
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void login(Long userId, String... roles) {
        AuthPrincipal principal = new AuthPrincipal(userId, "u@itsm.local", List.of(roles), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private ErrorCode codeOf(Throwable e) {
        return ((BusinessException) e).getErrorCode();
    }

    @Test
    void createForbiddenForNonProcessOwner() {
        login(1L, "END_USER");

        assertThatThrownBy(() -> service.create(new CreateCatalogItemRequest(
                "좌석 배정", null, Department.FACILITIES, ChecklistTemplateType.NONE, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void createRejectsItDepartment() {
        login(1L, "PROCESS_OWNER");

        assertThatThrownBy(() -> service.create(new CreateCatalogItemRequest(
                "장비 지급", null, Department.IT, ChecklistTemplateType.NONE, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    @Test
    void createSuccess() {
        login(1L, "PROCESS_OWNER");

        var response = service.create(new CreateCatalogItemRequest(
                "좌석 배정", "설명", Department.FACILITIES, ChecklistTemplateType.NONE, null, null));

        assertThat(response.name()).isEqualTo("좌석 배정");
        assertThat(response.department()).isEqualTo(Department.FACILITIES);
    }

    @Test
    void updateForbiddenForNonProcessOwner() {
        login(1L, "END_USER");

        assertThatThrownBy(() -> service.update(1L, new UpdateCatalogItemRequest(
                "이름", null, null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ACCESS_DENIED));
    }

    @Test
    void detailNotFoundThrows() {
        login(1L, "END_USER");
        when(catalogItemRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detail(9L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(codeOf(e)).isEqualTo(ErrorCode.ESM_CATALOG_ITEM_NOT_FOUND));
    }

    @Test
    void listReturnsSummaries() {
        login(1L, "END_USER");
        EsmCatalogItem item = new EsmCatalogItem("계약서 검토", null, Department.LEGAL, ChecklistTemplateType.NONE);
        when(catalogItemRepository.search(null, null)).thenReturn(List.of(item));

        var response = service.list(null, null);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).name()).isEqualTo("계약서 검토");
    }
}
