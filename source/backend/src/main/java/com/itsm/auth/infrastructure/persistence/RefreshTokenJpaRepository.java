package com.itsm.auth.infrastructure.persistence;

import com.itsm.auth.domain.RefreshToken;
import com.itsm.auth.domain.repository.RefreshTokenRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * RefreshTokenRepository 포트의 Spring Data JPA 구현.
 */
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long>, RefreshTokenRepository {
}
