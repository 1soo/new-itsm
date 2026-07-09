package com.itsm.auth.domain.repository;

import com.itsm.auth.domain.RefreshToken;

import java.util.Optional;
import java.util.UUID;

/**
 * Refresh Token 세션 저장소 포트.
 */
public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken refreshToken);

    Optional<RefreshToken> findByJti(UUID jti);
}
