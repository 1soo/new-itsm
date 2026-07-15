package com.itsm.auth.application;

import com.itsm.auth.application.dto.CreateUserRequest;
import com.itsm.auth.application.dto.PageResponse;
import com.itsm.auth.application.dto.StatusChangeResponse;
import com.itsm.auth.application.dto.UpdateUserRequest;
import com.itsm.auth.application.dto.UserDetailResponse;
import com.itsm.auth.application.dto.UserRolesResponse;
import com.itsm.auth.application.dto.UserSummaryResponse;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.AuditResult;
import com.itsm.auth.domain.EventType;
import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.UserRole;
import com.itsm.auth.domain.UserStatus;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.auth.domain.repository.RoleRepository;
import com.itsm.auth.domain.repository.UserRoleRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import com.itsm.common.security.AuthPrincipal;
import com.itsm.common.security.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 관리자 계정/역할 관리 유스케이스 (SYSTEM_ADMIN 전용).
 */
@Service
public class UserAdminService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserAdminService(AppUserRepository appUserRepository,
                            RoleRepository roleRepository,
                            UserRoleRepository userRoleRepository,
                            PasswordEncoder passwordEncoder,
                            AuditLogService auditLogService) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummaryResponse> list(String email, String name, UserStatus status,
                                                  String role, Pageable pageable) {
        return PageResponse.from(
                appUserRepository.search(email, name, status, role, pageable),
                u -> new UserSummaryResponse(u.getId(), u.getEmail(), u.getName(),
                        u.getStatus().name(), roleCodes(u.getId()), u.getCreatedAt()));
    }

    @Transactional
    public UserDetailResponse create(CreateUserRequest request) {
        if (appUserRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATE);
        }
        PasswordPolicy.validate(request.initialPassword());
        List<Role> roles = roleRepository.findAllById(request.roleIds());
        if (roles.size() != request.roleIds().stream().distinct().count()) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }

        AppUser user = new AppUser(request.email(), passwordEncoder.encode(request.initialPassword()),
                request.name(), UserStatus.ACTIVE);
        AppUser saved = appUserRepository.save(user);
        roles.forEach(role -> userRoleRepository.save(new UserRole(saved.getId(), role.getId())));

        recordChange(EventType.USER_CHANGE, saved.getEmail());
        return detail(saved);
    }

    @Transactional(readOnly = true)
    public UserDetailResponse get(Long userId) {
        return detail(findUser(userId));
    }

    @Transactional
    public UserDetailResponse update(Long userId, UpdateUserRequest request) {
        AppUser user = findUser(userId);
        if (StringUtils.hasText(request.name())) {
            user.changeName(request.name());
            appUserRepository.save(user);
            recordChange(EventType.USER_CHANGE, user.getEmail());
        }
        return detail(user);
    }

    @Transactional
    public StatusChangeResponse changeStatus(Long userId, UserStatus status) {
        AppUser user = findUser(userId);
        user.changeStatus(status);
        if (status == UserStatus.INACTIVE) {
            user.clearAccessTokenJti(); // 비활성화 시 현재 세션 강제 종료
        }
        appUserRepository.save(user);
        recordChange(EventType.USER_CHANGE, user.getEmail());
        return new StatusChangeResponse(user.getId(), user.getStatus().name());
    }

    @Transactional
    public UserRolesResponse assignRole(Long userId, Long roleId) {
        AppUser user = findUser(userId);
        if (roleRepository.findById(roleId).isEmpty()) {
            throw new BusinessException(ErrorCode.ROLE_NOT_FOUND);
        }
        if (!userRoleRepository.existsByUserIdAndRoleId(userId, roleId)) {
            userRoleRepository.save(new UserRole(userId, roleId));
        }
        recordChange(EventType.ROLE_CHANGE, user.getEmail());
        return new UserRolesResponse(userId, roleCodes(userId));
    }

    @Transactional
    public UserRolesResponse revokeRole(Long userId, Long roleId) {
        AppUser user = findUser(userId);
        userRoleRepository.findByUserIdAndRoleId(userId, roleId)
                .ifPresent(userRoleRepository::delete);
        recordChange(EventType.ROLE_CHANGE, user.getEmail());
        return new UserRolesResponse(userId, roleCodes(userId));
    }

    private void recordChange(EventType eventType, String target) {
        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        auditLogService.record(eventType, principal.userId(), principal.email(), target, AuditResult.SUCCESS);
    }

    private AppUser findUser(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private UserDetailResponse detail(AppUser user) {
        return new UserDetailResponse(user.getId(), user.getEmail(), user.getName(),
                user.getStatus().name(), roleCodes(user.getId()), user.getCreatedAt(), user.getUpdatedAt());
    }

    private List<String> roleCodes(Long userId) {
        return roleRepository.findRolesByUserId(userId).stream().map(Role::getRoleCode).toList();
    }
}
