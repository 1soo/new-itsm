package com.itsm.auth.application;

import com.itsm.auth.application.dto.CreateScreenRequest;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.auth.application.dto.ScreenDeletedResponse;
import com.itsm.auth.application.dto.ScreenResponse;
import com.itsm.auth.application.dto.ScreenRolesResponse;
import com.itsm.auth.application.dto.UpdateScreenRequest;
import com.itsm.auth.domain.AuditResult;
import com.itsm.auth.domain.EventType;
import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.Screen;
import com.itsm.auth.domain.ScreenRole;
import com.itsm.auth.domain.repository.RoleRepository;
import com.itsm.auth.domain.repository.ScreenRepository;
import com.itsm.auth.domain.repository.ScreenRoleRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 메뉴(화면) 관리 유스케이스 (SYSTEM_ADMIN 전용). Role-Menu 동적 매핑.
 */
@Service
public class ScreenAdminService {

    private final ScreenRepository screenRepository;
    private final ScreenRoleRepository screenRoleRepository;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;

    public ScreenAdminService(ScreenRepository screenRepository,
                              ScreenRoleRepository screenRoleRepository,
                              RoleRepository roleRepository,
                              AuditLogService auditLogService) {
        this.screenRepository = screenRepository;
        this.screenRoleRepository = screenRoleRepository;
        this.roleRepository = roleRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ScreenResponse> list(String groupCode, String domain, Pageable pageable) {
        return PageResponse.from(screenRepository.search(groupCode, domain, pageable), this::toResponse);
    }

    @Transactional
    public ScreenResponse create(CreateScreenRequest request) {
        if (screenRepository.existsByScreenCode(request.screenCode())) {
            throw new BusinessException(ErrorCode.SCREEN_CODE_DUPLICATE);
        }
        if (screenRepository.existsByPath(request.path())) {
            throw new BusinessException(ErrorCode.PATH_DUPLICATE);
        }
        if (request.groupCode() != null && !StringUtils.hasText(request.groupLabelEn())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "groupCode 지정 시 groupLabelEn은 필수입니다.");
        }
        Screen screen = new Screen(request.screenCode(), request.screenName(), request.screenNameEn(), request.path(),
                request.domain(), request.iconName(), request.groupCode(), request.groupLabel(), request.groupLabelEn(),
                request.sortOrder() != null ? request.sortOrder() : 0,
                request.navVisible() != null ? request.navVisible() : true);
        Screen saved = screenRepository.save(screen);
        recordChange(saved.getScreenCode());
        return toResponse(saved, List.of());
    }

    @Transactional
    public ScreenResponse update(Long screenId, UpdateScreenRequest request) {
        Screen screen = findScreen(screenId);
        if (request.path() != null && screenRepository.existsByPathAndIdNot(request.path(), screenId)) {
            throw new BusinessException(ErrorCode.PATH_DUPLICATE);
        }
        screen.update(request.screenName(), request.screenNameEn(), request.path(), request.iconName(),
                request.groupCode(), request.groupLabel(), request.groupLabelEn(),
                request.sortOrder(), request.navVisible());
        Screen saved = screenRepository.save(screen);
        recordChange(saved.getScreenCode());
        return toResponse(saved);
    }

    @Transactional
    public ScreenDeletedResponse delete(Long screenId) {
        Screen screen = findScreen(screenId);
        screen.markDeleted();
        screenRepository.save(screen);
        recordChange(screen.getScreenCode());
        return new ScreenDeletedResponse(screenId, true);
    }

    @Transactional
    public ScreenRolesResponse assignRole(Long screenId, Long roleId) {
        Screen screen = findScreen(screenId);
        if (roleRepository.findById(roleId).isEmpty()) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        if (screenRoleRepository.existsByScreenIdAndRoleId(screenId, roleId)) {
            throw new BusinessException(ErrorCode.SCREEN_ROLE_MAPPING_DUPLICATE);
        }
        screenRoleRepository.save(new ScreenRole(screenId, roleId));
        recordChange(screen.getScreenCode());
        return new ScreenRolesResponse(screenId, roleCodes(screenId));
    }

    @Transactional
    public ScreenRolesResponse revokeRole(Long screenId, Long roleId) {
        Screen screen = findScreen(screenId);
        screenRoleRepository.findByScreenIdAndRoleId(screenId, roleId)
                .ifPresent(screenRoleRepository::delete);
        recordChange(screen.getScreenCode());
        return new ScreenRolesResponse(screenId, roleCodes(screenId));
    }

    private void recordChange(String screenCode) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        auditLogService.record(EventType.ROLE_CHANGE, principal.userId(), principal.email(), screenCode, AuditResult.SUCCESS);
    }

    private Screen findScreen(Long screenId) {
        return screenRepository.findById(screenId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SCREEN_NOT_FOUND));
    }

    private ScreenResponse toResponse(Screen screen) {
        return toResponse(screen, roleCodes(screen.getId()));
    }

    private ScreenResponse toResponse(Screen screen, List<String> roles) {
        return new ScreenResponse(screen.getId(), screen.getScreenCode(), screen.getScreenName(), screen.getScreenNameEn(),
                screen.getPath(), screen.getDomain(), screen.getIconName(), screen.getGroupCode(), screen.getGroupLabel(),
                screen.getGroupLabelEn(), screen.getSortOrder(), screen.isNavVisible(), roles);
    }

    private List<String> roleCodes(Long screenId) {
        List<Long> roleIds = screenRoleRepository.findByScreenId(screenId).stream()
                .map(ScreenRole::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleRepository.findAllById(roleIds).stream().map(Role::getRoleCode).toList();
    }
}
