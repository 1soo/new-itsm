package com.itsm.auth.application;

import com.itsm.auth.application.dto.CreateRoleRequest;
import com.itsm.auth.application.dto.RoleCreatedResponse;
import com.itsm.auth.application.dto.RoleResponse;
import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.repository.RoleRepository;
import com.itsm.auth.domain.repository.UserRoleRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleServiceTest {

    @Mock RoleRepository roleRepository;
    @Mock UserRoleRepository userRoleRepository;

    RoleService service;

    @BeforeEach
    void setUp() {
        service = new RoleService(roleRepository, userRoleRepository);
    }

    @Test
    void createDuplicateCodeThrows() {
        when(roleRepository.existsByRoleCode("APPROVER")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateRoleRequest("APPROVER", "승인자", "desc")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ROLE_NAME_DUPLICATE));
    }

    @Test
    void createDuplicateNameThrows() {
        when(roleRepository.existsByRoleCode(anyString())).thenReturn(false);
        when(roleRepository.existsByRoleName("승인자")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateRoleRequest("APPROVER", "승인자", "desc")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode()).isEqualTo(ErrorCode.ROLE_NAME_DUPLICATE));
    }

    @Test
    void createSuccess() {
        when(roleRepository.existsByRoleName(anyString())).thenReturn(false);
        when(roleRepository.existsByRoleCode(anyString())).thenReturn(false);
        when(roleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RoleCreatedResponse response = service.create(new CreateRoleRequest("ASSET_AUDITOR", "자산 감사자", "desc"));

        assertThat(response.roleCode()).isEqualTo("ASSET_AUDITOR");
        assertThat(response.name()).isEqualTo("자산 감사자");
        assertThat(response.description()).isEqualTo("desc");
    }

    @Test
    void listReturnsRolesWithCodeAndUserCount() {
        when(roleRepository.findAll()).thenReturn(List.of(new Role("SYSTEM_ADMIN", "관리자", "desc")));
        when(userRoleRepository.countByRoleId(any())).thenReturn(3L);

        List<RoleResponse> roles = service.list();

        assertThat(roles).hasSize(1);
        assertThat(roles.get(0).roleCode()).isEqualTo("SYSTEM_ADMIN");
        assertThat(roles.get(0).name()).isEqualTo("관리자");
        assertThat(roles.get(0).userCount()).isEqualTo(3L);
    }
}
