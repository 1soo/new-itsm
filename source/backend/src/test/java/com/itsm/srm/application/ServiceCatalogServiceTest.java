package com.itsm.srm.application;

import tools.jackson.databind.ObjectMapper;
import com.itsm.auth.domain.repository.RoleRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.srm.application.dto.CatalogItemDetailResponse;
import com.itsm.srm.application.dto.CreateCatalogItemRequest;
import com.itsm.srm.domain.repository.ServiceCatalogCategoryRepository;
import com.itsm.srm.domain.repository.ServiceCatalogItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServiceCatalogServiceTest {

    @Mock ServiceCatalogItemRepository catalogItemRepository;
    @Mock ServiceCatalogCategoryRepository categoryRepository;
    @Mock RoleRepository roleRepository;

    ServiceCatalogService service;

    @BeforeEach
    void setUp() {
        service = new ServiceCatalogService(catalogItemRepository, categoryRepository, roleRepository, new ObjectMapper());
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

        Map<String, Object> formSchema = Map.of(
                "display", "form",
                "components", List.of(Map.of("key", "reason", "label", "Reason", "type", "textfield", "input", true)));
        CreateCatalogItemRequest request = new CreateCatalogItemRequest(
                "Laptop", "desc", null, 1L, 60, 480, null, formSchema);

        CatalogItemDetailResponse response = service.create(request);

        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.formSchema()).containsKey("components");
    }

    @Test
    void suggestionsReturnEmptyWhenKmNotBuilt() {
        assertThat(service.suggestions(1L, "vpn")).isEmpty();
    }
}
