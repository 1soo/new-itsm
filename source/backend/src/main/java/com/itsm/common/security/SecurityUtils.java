package com.itsm.common.security;

import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 현재 인증 주체 접근 유틸.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthPrincipal principal) {
            return principal;
        }
        throw new BusinessException(ErrorCode.UNAUTHENTICATED);
    }

    public static boolean hasRole(String roleCode) {
        return currentPrincipal().roles().contains(roleCode);
    }
}
