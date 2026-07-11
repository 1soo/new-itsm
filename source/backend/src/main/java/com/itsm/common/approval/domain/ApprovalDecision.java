package com.itsm.common.approval.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * 역할별 승인/반려 결정 기록(append-only). UNIQUE(step_id, role_id)로 동일 역할 슬롯 재처리를 막는다.
 */
@Getter
@Entity
@Table(name = "approval_decision", uniqueConstraints = @UniqueConstraint(columnNames = {"step_id", "role_id"}))
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "step_id", nullable = false)
    private Long stepId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "decided_by_id", nullable = false)
    private Long decidedById;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DecisionType decision;

    @Column(length = 500)
    private String reason;

    @Column(name = "decided_at", nullable = false)
    private OffsetDateTime decidedAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public ApprovalDecision(Long stepId, Long roleId, Long decidedById, DecisionType decision, String reason) {
        this.stepId = stepId;
        this.roleId = roleId;
        this.decidedById = decidedById;
        this.decision = decision;
        this.reason = reason;
        this.decidedAt = OffsetDateTime.now();
    }
}
