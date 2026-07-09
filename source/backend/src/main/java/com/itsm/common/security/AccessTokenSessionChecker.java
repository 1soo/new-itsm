package com.itsm.common.security;

import java.util.UUID;

/**
 * 서버측 JTI 세션 매핑 검증 포트. Access Token의 jti가 사용자의 현재 세션과 일치하는지 확인한다.
 * (구현: auth 도메인 — app_user.access_token_jti 비교)
 */
public interface AccessTokenSessionChecker {

    boolean isCurrentAccessToken(Long userId, UUID jti);
}
