package com.itsm.auth.application;

import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.UserStatus;
import com.itsm.auth.domain.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessTokenSessionCheckerImplTest {

    @Mock AppUserRepository appUserRepository;

    AccessTokenSessionCheckerImpl checker;

    @BeforeEach
    void setUp() {
        checker = new AccessTokenSessionCheckerImpl(appUserRepository);
    }

    private AppUser activeUserWithJti(UUID jti) {
        AppUser user = new AppUser("u@itsm.local", "hash", "사용자", UserStatus.ACTIVE);
        user.updateAccessTokenJti(jti);
        return user;
    }

    @Test
    void matchingJtiAndActiveReturnsTrue() {
        UUID jti = UUID.randomUUID();
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(activeUserWithJti(jti)));

        assertThat(checker.isCurrentAccessToken(1L, jti)).isTrue();
    }

    @Test
    void mismatchedJtiReturnsFalse() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(activeUserWithJti(UUID.randomUUID())));

        assertThat(checker.isCurrentAccessToken(1L, UUID.randomUUID())).isFalse();
    }

    @Test
    void inactiveUserReturnsFalse() {
        UUID jti = UUID.randomUUID();
        AppUser user = new AppUser("u@itsm.local", "hash", "사용자", UserStatus.INACTIVE);
        user.updateAccessTokenJti(jti);
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThat(checker.isCurrentAccessToken(1L, jti)).isFalse();
    }

    @Test
    void missingUserReturnsFalse() {
        when(appUserRepository.findById(1L)).thenReturn(Optional.empty());

        assertThat(checker.isCurrentAccessToken(1L, UUID.randomUUID())).isFalse();
    }
}
