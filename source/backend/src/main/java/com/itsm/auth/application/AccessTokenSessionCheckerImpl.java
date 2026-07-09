package com.itsm.auth.application;

import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.security.AccessTokenSessionChecker;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * JTI 세션 매핑 검증. Access Token의 jti가 사용자의 현재 세션(app_user.access_token_jti)과
 * 일치하고 계정이 활성 상태일 때만 유효로 판정한다.
 */
@Component
public class AccessTokenSessionCheckerImpl implements AccessTokenSessionChecker {

    private final AppUserRepository appUserRepository;

    public AccessTokenSessionCheckerImpl(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCurrentAccessToken(Long userId, UUID jti) {
        return appUserRepository.findById(userId)
                .map(user -> user.isActive() && jti.equals(user.getAccessTokenJti()))
                .orElse(false);
    }
}
