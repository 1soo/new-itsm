package com.itsm.auth.infrastructure.persistence;

import com.itsm.auth.domain.ScreenRole;
import com.itsm.auth.domain.repository.ScreenRoleRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ScreenRoleRepository 포트의 Spring Data JPA 구현.
 */
public interface ScreenRoleJpaRepository extends JpaRepository<ScreenRole, Long>, ScreenRoleRepository {
}
