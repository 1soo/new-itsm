package com.itsm.common.notification.application;

import com.itsm.common.notification.application.dto.DismissNotificationsRequest;
import com.itsm.common.notification.application.dto.DismissResultResponse;
import com.itsm.common.notification.application.dto.NotificationDismissalListResponse;
import com.itsm.common.notification.domain.NotificationDismissal;
import com.itsm.common.notification.domain.repository.NotificationDismissalRepository;
import com.itsm.common.security.AuthPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationDismissalServiceTest {

    @Mock NotificationDismissalRepository notificationDismissalRepository;

    NotificationDismissalService service;

    @BeforeEach
    void setUp() {
        service = new NotificationDismissalService(notificationDismissalRepository);
        when(notificationDismissalRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        login();
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private void login() {
        AuthPrincipal principal = new AuthPrincipal(1L, "u@itsm.local", List.of("END_USER"), UUID.randomUUID());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @Test
    void dismissSavesOnlyNewItemsAndCountsThem() {
        when(notificationDismissalRepository.existsByUserIdAndNotificationTypeAndSourceId(1L, "SERVICE_REQUEST_APPROVAL", 10L))
                .thenReturn(false);
        when(notificationDismissalRepository.existsByUserIdAndNotificationTypeAndSourceId(1L, "ASSET_EXPIRY", 20L))
                .thenReturn(true);

        DismissResultResponse response = service.dismiss(List.of(
                new DismissNotificationsRequest.Item("SERVICE_REQUEST_APPROVAL", 10L),
                new DismissNotificationsRequest.Item("ASSET_EXPIRY", 20L)));

        assertThat(response.dismissedCount()).isEqualTo(1);
        verify(notificationDismissalRepository, times(1)).save(any(NotificationDismissal.class));
    }

    @Test
    void dismissAllAlreadyDismissedIsIdempotentAndSavesNothing() {
        when(notificationDismissalRepository.existsByUserIdAndNotificationTypeAndSourceId(any(), any(), any()))
                .thenReturn(true);

        DismissResultResponse response = service.dismiss(List.of(
                new DismissNotificationsRequest.Item("CHANGE_APPROVAL", 5L)));

        assertThat(response.dismissedCount()).isEqualTo(0);
        verify(notificationDismissalRepository, never()).save(any());
    }

    @Test
    void listReturnsEmptyWhenNoHistory() {
        when(notificationDismissalRepository.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of());

        NotificationDismissalListResponse response = service.list();

        assertThat(response.items()).isEmpty();
    }

    @Test
    void listMapsHistoryForCurrentUser() {
        NotificationDismissal dismissal = new NotificationDismissal(1L, "SERVICE_REQUEST_APPROVAL", 10L);
        when(notificationDismissalRepository.findByUserIdOrderByIdAsc(1L)).thenReturn(List.of(dismissal));

        NotificationDismissalListResponse response = service.list();

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).notificationType()).isEqualTo("SERVICE_REQUEST_APPROVAL");
        assertThat(response.items().get(0).sourceId()).isEqualTo(10L);
        assertThat(response.items().get(0).dismissedAt()).isEqualTo(dismissal.getDismissedAt());
    }
}
