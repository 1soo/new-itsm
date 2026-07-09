package com.itsm.common.security;

import java.util.List;
import java.util.UUID;

/**
 * 인증된 요청 주체. JWT claim(userId, jti, role)에서 추출한다.
 */
public record AuthPrincipal(Long userId, String email, List<String> roles, UUID jti) {
}
