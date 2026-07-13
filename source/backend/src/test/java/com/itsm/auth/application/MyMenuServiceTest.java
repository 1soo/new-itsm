package com.itsm.auth.application;

import com.itsm.auth.application.dto.MyMenuResponse;
import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.Screen;
import com.itsm.auth.domain.ScreenRole;
import com.itsm.auth.domain.repository.RoleRepository;
import com.itsm.auth.domain.repository.ScreenRepository;
import com.itsm.auth.domain.repository.ScreenRoleRepository;
import com.itsm.common.security.AuthPrincipal;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MyMenuServiceTest {

    @Mock ScreenRepository screenRepository;
    @Mock ScreenRoleRepository screenRoleRepository;
    @Mock RoleRepository roleRepository;

    MyMenuService service;

    @BeforeEach
    void setUp() {
        service = new MyMenuService(screenRepository, screenRoleRepository, roleRepository);
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void login(String... roles) {
        AuthPrincipal principal = new AuthPrincipal(1L, "u@itsm.local", List.of(roles), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private Screen screenWithId(Long id, String code, String groupCode, String groupLabel, int sortOrder) {
        String groupLabelEn = groupLabel == null ? null : groupLabel + " (EN)";
        Screen screen = new Screen(code, code + " 이름", code + " Name", "/" + code, "auth", null,
                groupCode, groupLabel, groupLabelEn, sortOrder, true);
        try {
            var field = Screen.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(screen, id);
        } catch (ReflectiveOperationException ignored) {
            // id 리플렉션 세팅 실패 시 null로 유지(테스트 목적상 무해)
        }
        return screen;
    }

    @Test
    void unmappedScreenAlwaysVisible() {
        login("END_USER");
        Screen dashboard = screenWithId(1L, "SCR-COM-013", null, null, 0);
        when(screenRepository.findAllVisible()).thenReturn(List.of(dashboard));
        when(screenRoleRepository.findByScreenId(1L)).thenReturn(List.of());

        MyMenuResponse response = service.myMenu();

        assertThat(response.groups()).hasSize(1);
        assertThat(response.groups().get(0).items()).extracting("screenCode").containsExactly("SCR-COM-013");
    }

    @Test
    void mappedScreenHiddenWithoutMatchingRole() {
        login("END_USER");
        Screen adminMenu = screenWithId(2L, "SCR-ADMIN-006", "admin", "관리자", 420);
        when(screenRepository.findAllVisible()).thenReturn(List.of(adminMenu));
        when(screenRoleRepository.findByScreenId(2L)).thenReturn(List.of(new ScreenRole(2L, 5L)));
        when(roleRepository.findAllById(List.of(5L))).thenReturn(List.of(new Role("SYSTEM_ADMIN", "관리자", null)));

        MyMenuResponse response = service.myMenu();

        assertThat(response.groups()).isEmpty();
    }

    @Test
    void mappedScreenVisibleWithMatchingRole() {
        login("APPROVER");
        Screen approvalMenu = screenWithId(3L, "SCR-SRM-006", "srm", "서비스 요청", 50);
        when(screenRepository.findAllVisible()).thenReturn(List.of(approvalMenu));
        when(screenRoleRepository.findByScreenId(3L)).thenReturn(List.of(new ScreenRole(3L, 5L)));
        when(roleRepository.findAllById(List.of(5L))).thenReturn(List.of(new Role("APPROVER", "승인자", null)));

        MyMenuResponse response = service.myMenu();

        assertThat(response.groups()).hasSize(1);
        assertThat(response.groups().get(0).items()).extracting("screenCode").containsExactly("SCR-SRM-006");
    }

    @Test
    void systemAdminBypassesRoleMapping() {
        login("SYSTEM_ADMIN");
        Screen adminMenu = screenWithId(2L, "SCR-ADMIN-006", "admin", "관리자", 420);
        when(screenRepository.findAllVisible()).thenReturn(List.of(adminMenu));
        when(screenRoleRepository.findByScreenId(2L)).thenReturn(List.of(new ScreenRole(2L, 5L)));
        when(roleRepository.findAllById(List.of(5L))).thenReturn(List.of(new Role("APPROVER", "승인자", null)));

        MyMenuResponse response = service.myMenu();

        assertThat(response.groups()).hasSize(1);
        assertThat(response.groups().get(0).items()).extracting("screenCode").containsExactly("SCR-ADMIN-006");
    }

    @Test
    void groupsOrderedByFirstOccurrenceAndItemsGroupedTogether() {
        login("SYSTEM_ADMIN");
        Screen dashboard = screenWithId(1L, "SCR-COM-013", null, null, 0);
        Screen profile = screenWithId(2L, "SCR-AUTH-002", null, null, 10);
        Screen srmMenu = screenWithId(3L, "SCR-SRM-001", "srm", "서비스 요청", 20);
        when(screenRepository.findAllVisible()).thenReturn(List.of(dashboard, profile, srmMenu));
        when(screenRoleRepository.findByScreenId(any())).thenReturn(List.of());

        MyMenuResponse response = service.myMenu();

        assertThat(response.groups()).hasSize(2);
        assertThat(response.groups().get(0).groupCode()).isNull();
        assertThat(response.groups().get(0).items()).extracting("screenCode")
                .containsExactly("SCR-COM-013", "SCR-AUTH-002");
        assertThat(response.groups().get(1).groupCode()).isEqualTo("srm");
    }
}
