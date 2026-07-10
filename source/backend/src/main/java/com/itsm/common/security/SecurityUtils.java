package com.itsm.common.security;

import com.itsm.common.exception.BusinessException;
import com.itsm.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 현재 인증 주체 접근 유틸. SYSTEM_ADMIN은 role_design/system_admin.md 2절("역할별 접근 제한과 무관하게
 * 항상 허용, 403 없음")에 따라 {@link #hasRole}/{@link #hasAnyRole} 판정에서 항상 통과한다.
 */
public final class SecurityUtils {

    private static final String SYSTEM_ADMIN = "SYSTEM_ADMIN";

    private SecurityUtils() {
    }

    public static AuthPrincipal currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthPrincipal principal) {
            return principal;
        }
        throw new BusinessException(ErrorCode.UNAUTHENTICATED);
    }

    public static boolean isSystemAdmin() {
        return currentPrincipal().roles().contains(SYSTEM_ADMIN);
    }

    public static boolean hasRole(String roleCode) {
        return isSystemAdmin() || currentPrincipal().roles().contains(roleCode);
    }

    public static boolean hasAnyRole(String... roleCodes) {
        if (isSystemAdmin()) {
            return true;
        }
        var roles = currentPrincipal().roles();
        for (String roleCode : roleCodes) {
            if (roles.contains(roleCode)) {
                return true;
            }
        }
        return false;
    }
}
