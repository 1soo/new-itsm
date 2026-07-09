package com.itsm.auth.domain.repository;

import com.itsm.auth.domain.Role;

import java.util.List;
import java.util.Optional;

/**
 * 역할 저장소 포트.
 */
public interface RoleRepository {

    Role save(Role role);

    Optional<Role> findById(Long id);

    Optional<Role> findByRoleCode(String roleCode);

    boolean existsByRoleCode(String roleCode);

    boolean existsByRoleName(String roleName);

    List<Role> findAll();

    List<Role> findAllById(Iterable<Long> ids);

    List<Role> findRolesByUserId(Long userId);
}
