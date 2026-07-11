package com.itsm.common.notification.domain.repository;

import com.itsm.common.notification.domain.NotificationDismissal;

import java.util.List;

/**
 * 알림 확인처리 이력 저장소 포트.
 */
public interface NotificationDismissalRepository {

    NotificationDismissal save(NotificationDismissal dismissal);

    boolean existsByUserIdAndNotificationTypeAndSourceId(Long userId, String notificationType, Long sourceId);

    List<NotificationDismissal> findByUserIdOrderByIdAsc(Long userId);
}
