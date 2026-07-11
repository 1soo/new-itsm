package com.itsm.common.approval.domain;

import com.itsm.common.entity.BaseEntity;
import com.itsm.common.ticket.TicketType;
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

/**
 * 승인 인스턴스 헤더(기존 approval 테이블 대체, 전 도메인 공용 다형 참조).
 */
@Getter
@Entity
@Table(name = "approval_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type", nullable = false, length = 20)
    private TicketType ticketType;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "approval_process_id", nullable = false)
    private Long approvalProcessId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ApprovalRequestStatus status;

    @Column(name = "current_step_no")
    private Short currentStepNo;

    public ApprovalRequest(TicketType ticketType, Long ticketId, Long approvalProcessId, short firstStepNo) {
        this.ticketType = ticketType;
        this.ticketId = ticketId;
        this.approvalProcessId = approvalProcessId;
        this.status = ApprovalRequestStatus.IN_PROGRESS;
        this.currentStepNo = firstStepNo;
    }

    public void advanceTo(short stepNo) {
        this.currentStepNo = stepNo;
    }

    public void approve() {
        this.status = ApprovalRequestStatus.APPROVED;
        this.currentStepNo = null;
    }

    public void reject() {
        this.status = ApprovalRequestStatus.REJECTED;
        this.currentStepNo = null;
    }

    public boolean isInProgress() {
        return status == ApprovalRequestStatus.IN_PROGRESS;
    }
}
