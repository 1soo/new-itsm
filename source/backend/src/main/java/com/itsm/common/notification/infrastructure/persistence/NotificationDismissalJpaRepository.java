package com.itsm.common.notification.infrastructure.persistence;

import com.itsm.common.notification.domain.NotificationDismissal;
import com.itsm.common.notification.domain.repository.NotificationDismissalRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * NotificationDismissalRepository 포트의 Spring Data JPA 구현.
 */
public interface NotificationDismissalJpaRepository
        extends JpaRepository<NotificationDismissal, Long>, NotificationDismissalRepository {

    @Override
    boolean existsByUserIdAndNotificationTypeAndSourceId(Long userId, String notificationType, Long sourceId);

    @Override
    List<NotificationDismissal> findByUserIdOrderByIdAsc(Long userId);
}
