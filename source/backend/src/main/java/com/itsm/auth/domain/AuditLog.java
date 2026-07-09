package com.itsm.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

/**
 * 인증·인가·계정/역할 변경 이벤트 append-only 기록.
 */
@Getter
@Entity
@Table(name = "audit_log")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_email", length = 255)
    private String actorEmail;

    @Column(length = 255)
    private String target;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AuditResult result;

    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    private AuditLog(EventType eventType, Long actorId, String actorEmail, String target, AuditResult result) {
        this.eventType = eventType;
        this.actorId = actorId;
        this.actorEmail = actorEmail;
        this.target = target;
        this.result = result;
        this.occurredAt = OffsetDateTime.now();
    }

    public static AuditLog of(EventType eventType, Long actorId, String actorEmail, String target, AuditResult result) {
        return new AuditLog(eventType, actorId, actorEmail, target, result);
    }
}
