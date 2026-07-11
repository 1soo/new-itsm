package com.itsm.common.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

/**
 * 알림 확인처리 이력. 헤더 알림 드롭다운의 "모두 지우기"/개별 X가 남기는 표시 상태이며,
 * 원본 승인 대기·자산 만료 데이터에는 영향을 주지 않는다. append-only.
 */
@Getter
@Entity
@Table(name = "notification_dismissal",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "notification_type", "source_id"}))
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationDismissal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "notification_type", nullable = false, length = 30)
    private String notificationType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "dismissed_at", nullable = false)
    private OffsetDateTime dismissedAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public NotificationDismissal(Long userId, String notificationType, Long sourceId) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.sourceId = sourceId;
        this.dismissedAt = OffsetDateTime.now();
    }
}
