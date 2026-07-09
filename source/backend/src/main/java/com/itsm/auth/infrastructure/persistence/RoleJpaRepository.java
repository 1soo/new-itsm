package com.itsm.auth.infrastructure.persistence;

import com.itsm.auth.domain.Role;
import com.itsm.auth.domain.repository.RoleRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * RoleRepository 포트의 Spring Data JPA 구현.
 */
public interface RoleJpaRepository extends JpaRepository<Role, Long>, RoleRepository {

    @Override
    boolean existsByRoleCode(String roleCode);

    @Override
    boolean existsByRoleName(String roleName);

    @Override
    @Query("select r from Role r, UserRole ur where ur.roleId = r.id and ur.userId = :userId")
    List<Role> findRolesByUserId(@Param("userId") Long userId);
}
