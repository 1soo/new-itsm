package com.itsm.auth.infrastructure.persistence;

import com.itsm.auth.domain.UserRole;
import com.itsm.auth.domain.repository.UserRoleRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * UserRoleRepository 포트의 Spring Data JPA 구현.
 */
public interface UserRoleJpaRepository extends JpaRepository<UserRole, Long>, UserRoleRepository {
}
