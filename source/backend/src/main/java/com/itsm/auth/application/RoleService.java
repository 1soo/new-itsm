package com.itsm.auth.application;

import com.itsm.auth.application.dto.CreateRoleRequest;
import com.itsm.auth.application.dto.RoleCreatedResponse;
import com.itsm.auth.application.dto.RoleResponse;
import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.repository.RoleRepository;
import com.itsm.auth.domain.repository.UserRoleRepository;
import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 역할 관리 유스케이스 (SYSTEM_ADMIN 전용).
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public RoleService(RoleRepository roleRepository, UserRoleRepository userRoleRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> list() {
        return roleRepository.findAll().stream()
                .map(r -> new RoleResponse(r.getId(), r.getRoleCode(), r.getRoleName(), r.getDescription(),
                        userRoleRepository.countByRoleId(r.getId())))
                .toList();
    }

    @Transactional
    public RoleCreatedResponse create(CreateRoleRequest request) {
        if (roleRepository.existsByRoleCode(request.roleCode()) || roleRepository.existsByRoleName(request.name())) {
            throw new BusinessException(ErrorCode.ROLE_NAME_DUPLICATE);
        }
        Role saved = roleRepository.save(new Role(request.roleCode(), request.name(), request.description()));
        return new RoleCreatedResponse(saved.getId(), saved.getRoleCode(), saved.getRoleName(), saved.getDescription());
    }
}
