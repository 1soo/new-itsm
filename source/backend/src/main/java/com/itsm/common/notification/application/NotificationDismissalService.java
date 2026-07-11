package com.itsm.common.notification.application;

import com.itsm.common.notification.application.dto.DismissNotificationsRequest;
import com.itsm.common.notification.application.dto.DismissResultResponse;
import com.itsm.common.notification.application.dto.NotificationDismissalListResponse;
import com.itsm.common.notification.application.dto.NotificationDismissalResponse;
import com.itsm.common.notification.domain.NotificationDismissal;
import com.itsm.common.notification.domain.repository.NotificationDismissalRepository;
import com.itsm.common.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 알림 확인처리 유스케이스(API-COM-001/002). 확인처리는 표시 여부에만 영향을 주며,
 * 원본 승인 대기·자산 만료 데이터는 변경하지 않는다.
 */
@Service
public class NotificationDismissalService {

    private final NotificationDismissalRepository notificationDismissalRepository;

    public NotificationDismissalService(NotificationDismissalRepository notificationDismissalRepository) {
        this.notificationDismissalRepository = notificationDismissalRepository;
    }

    @Transactional
    public DismissResultResponse dismiss(List<DismissNotificationsRequest.Item> items) {
        Long userId = SecurityUtils.currentPrincipal().userId();
        int dismissedCount = 0;
        for (DismissNotificationsRequest.Item item : items) {
            if (!notificationDismissalRepository.existsByUserIdAndNotificationTypeAndSourceId(
                    userId, item.notificationType(), item.sourceId())) {
                notificationDismissalRepository.save(
                        new NotificationDismissal(userId, item.notificationType(), item.sourceId()));
                dismissedCount++;
            }
        }
        return new DismissResultResponse(dismissedCount);
    }

    @Transactional(readOnly = true)
    public NotificationDismissalListResponse list() {
        Long userId = SecurityUtils.currentPrincipal().userId();
        List<NotificationDismissalResponse> items = notificationDismissalRepository.findByUserIdOrderByIdAsc(userId)
                .stream()
                .map(d -> new NotificationDismissalResponse(d.getNotificationType(), d.getSourceId(), d.getDismissedAt()))
                .toList();
        return new NotificationDismissalListResponse(items);
    }
}
