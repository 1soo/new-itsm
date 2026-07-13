package com.itsm.auth.application;

import com.itsm.auth.application.dto.MenuGroupResponse;
import com.itsm.auth.application.dto.MenuItemResponse;
import com.itsm.auth.application.dto.MyMenuResponse;
import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.Screen;
import com.itsm.auth.domain.ScreenRole;
import com.itsm.auth.domain.repository.RoleRepository;
import com.itsm.auth.domain.repository.ScreenRepository;
import com.itsm.auth.domain.repository.ScreenRoleRepository;
import com.itsm.common.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 로그인 사용자의 역할에 매핑된 사이드바 메뉴 조회(API-AUTH-022). screen_role 매핑이 전혀 없는
 * 화면은 전체 인증 사용자에게 공개된 것으로 간주한다.
 */
@Service
public class MyMenuService {

    private final ScreenRepository screenRepository;
    private final ScreenRoleRepository screenRoleRepository;
    private final RoleRepository roleRepository;

    public MyMenuService(ScreenRepository screenRepository,
                         ScreenRoleRepository screenRoleRepository,
                         RoleRepository roleRepository) {
        this.screenRepository = screenRepository;
        this.screenRoleRepository = screenRoleRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional(readOnly = true)
    public MyMenuResponse myMenu() {
        // findAllVisible()이 sort_order 오름차순을 보장하므로, 그룹별 최소 sort_order 순서는
        // LinkedHashMap의 최초 삽입 순서로 자연히 유지된다.
        Map<String, List<Screen>> byGroup = new LinkedHashMap<>();
        for (Screen screen : screenRepository.findAllVisible()) {
            if (accessible(screen)) {
                byGroup.computeIfAbsent(screen.getGroupCode(), k -> new ArrayList<>()).add(screen);
            }
        }

        List<MenuGroupResponse> groups = byGroup.values().stream()
                .map(screens -> new MenuGroupResponse(
                        screens.get(0).getGroupCode(),
                        screens.get(0).getGroupLabel(),
                        screens.get(0).getGroupLabelEn(),
                        screens.stream()
                                .map(s -> new MenuItemResponse(s.getScreenCode(), s.getScreenName(), s.getScreenNameEn(), s.getPath(), s.getIconName()))
                                .toList()))
                .toList();

        return new MyMenuResponse(groups);
    }

    private boolean accessible(Screen screen) {
        List<Long> roleIds = screenRoleRepository.findByScreenId(screen.getId()).stream()
                .map(ScreenRole::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return true;
        }
        List<String> roleCodes = roleRepository.findAllById(roleIds).stream().map(Role::getRoleCode).toList();
        return SecurityUtils.hasAnyRole(roleCodes.toArray(new String[0]));
    }
}
