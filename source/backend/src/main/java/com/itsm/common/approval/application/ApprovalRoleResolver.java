package com.itsm.common.approval.application;

import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.repository.RoleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 승인 엔진에서 role_id ↔ role_code(role claim)를 상호 변환하는 공용 헬퍼.
 */
@Component
public class ApprovalRoleResolver {

    private final RoleRepository roleRepository;

    public ApprovalRoleResolver(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /** 사용자가 보유한 역할의 role_code 목록. */
    public List<String> roleCodesOfUser(Long userId) {
        return roleRepository.findRolesByUserId(userId).stream().map(Role::getRoleCode).toList();
    }

    /** role_code 목록을 role_id 목록으로 변환(존재하지 않는 코드는 건너뜀). */
    public List<Long> roleIdsOf(List<String> roleCodes) {
        return roleCodes.stream()
                .map(roleRepository::findByRoleCode)
                .flatMap(Optional::stream)
                .map(Role::getId)
                .toList();
    }

    public Optional<Role> findById(Long roleId) {
        return roleRepository.findById(roleId);
    }
}
