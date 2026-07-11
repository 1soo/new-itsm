package com.itsm.common.approval.domain;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * 인스턴스 차수 스냅샷(규칙 정의가 이후 바뀌어도 진행 중 인스턴스는 생성 시점 값을 그대로 사용).
 */
@Getter
@Entity
@Table(name = "approval_request_step",
        uniqueConstraints = @UniqueConstraint(columnNames = {"approval_request_id", "step_no"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalRequestStep extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "approval_request_id", nullable = false)
    private Long approvalRequestId;

    @Column(name = "step_no", nullable = false)
    private short stepNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_mode", nullable = false, length = 5)
    private DecisionMode decisionMode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ApprovalStepStatus status;

    public ApprovalRequestStep(Long approvalRequestId, short stepNo, DecisionMode decisionMode) {
        this.approvalRequestId = approvalRequestId;
        this.stepNo = stepNo;
        this.decisionMode = decisionMode;
        this.status = ApprovalStepStatus.PENDING;
    }

    public void approve() {
        this.status = ApprovalStepStatus.APPROVED;
    }

    public void reject() {
        this.status = ApprovalStepStatus.REJECTED;
    }

    public void skip() {
        this.status = ApprovalStepStatus.SKIPPED;
    }
}
