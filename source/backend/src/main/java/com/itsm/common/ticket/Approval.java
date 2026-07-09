package com.itsm.common.ticket;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * 승인(서비스요청·변경 공용). 역할 기반 승인: approver_role을 보유한 사용자가 공유 대기함에서 처리하며,
 * 먼저 처리한 사용자가 decided_by_id에 기록된다. 인가(403)는 요청자의 role claim에 approver_role 포함 여부로 판정.
 */
@Getter
@Entity
@Table(name = "approval")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Approval extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type", nullable = false, length = 20)
    private TicketType ticketType;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "approver_role", nullable = false, length = 50)
    private String approverRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApprovalStatus status;

    @Column(name = "decided_by_id")
    private Long decidedById;

    @Column(name = "decision_reason", length = 500)
    private String decisionReason;

    @Column(name = "decided_at")
    private OffsetDateTime decidedAt;

    public Approval(TicketType ticketType, Long ticketId, String approverRole) {
        this.ticketType = ticketType;
        this.ticketId = ticketId;
        this.approverRole = approverRole;
        this.status = ApprovalStatus.PENDING;
    }

    public void approve(Long deciderId, String reason) {
        this.status = ApprovalStatus.APPROVED;
        this.decidedById = deciderId;
        this.decisionReason = reason;
        this.decidedAt = OffsetDateTime.now();
    }

    public void reject(Long deciderId, String reason) {
        this.status = ApprovalStatus.REJECTED;
        this.decidedById = deciderId;
        this.decisionReason = reason;
        this.decidedAt = OffsetDateTime.now();
    }

    public boolean isPending() {
        return status == ApprovalStatus.PENDING;
    }
}
