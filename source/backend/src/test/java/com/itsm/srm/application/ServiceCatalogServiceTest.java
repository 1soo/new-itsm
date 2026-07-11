package com.itsm.srm.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.srm.application.dto.CatalogItemDetailResponse;
import com.itsm.srm.application.dto.CreateCatalogItemRequest;
import com.itsm.srm.application.dto.FormFieldDto;
import com.itsm.srm.domain.CatalogFormField;
import com.itsm.srm.domain.ServiceCatalogItem;
import com.itsm.srm.domain.repository.CatalogFormFieldRepository;
import com.itsm.srm.domain.repository.ServiceCatalogItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceCatalogServiceTest {

    @Mock ServiceCatalogItemRepository catalogItemRepository;
    @Mock CatalogFormFieldRepository formFieldRepository;

    ServiceCatalogService service;

    @BeforeEach
    void setUp() {
        service = new ServiceCatalogService(catalogItemRepository, formFieldRepository, new ObjectMapper());
    }

    @Test
    void detailNotFoundThrows() {
        when(catalogItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detail(99L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.CATALOG_ITEM_NOT_FOUND));
    }

    @Test
    void createPersistsItemWithFormSchema() {
        when(catalogItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(formFieldRepository.findByCatalogItemIdOrderBySortOrderAsc(any()))
                .thenReturn(List.of(new CatalogFormField(1L, "reason", "사유", "text", true, null, 0)));

        CreateCatalogItemRequest request = new CreateCatalogItemRequest(
                "Laptop", "desc", 1L, 60, 480,
                List.of(new FormFieldDto("reason", "Reason", "text", true, null)));

        CatalogItemDetailResponse response = service.create(request);

        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.formSchema()).hasSize(1);
    }

    @Test
    void suggestionsReturnEmptyWhenKmNotBuilt() {
        assertThat(service.suggestions(1L, "vpn")).isEmpty();
    }
}
