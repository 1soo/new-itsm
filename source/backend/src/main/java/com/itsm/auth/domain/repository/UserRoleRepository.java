package com.itsm.auth.domain.repository;

import com.itsm.auth.domain.UserRole;

import java.util.Optional;

/**
 * 사용자-역할 매핑 저장소 포트.
 */
public interface UserRoleRepository {

    UserRole save(UserRole userRole);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);

    void delete(UserRole userRole);

    long countByRoleId(Long roleId);
}
