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
 * 규칙의 승인자 차수(n차, 최대 10차). Drag&Drop 순서 교체는 두 행의 stepNo를 맞바꿔 저장한다.
 */
@Getter
@Entity
@Table(name = "approval_process_step",
        uniqueConstraints = @UniqueConstraint(columnNames = {"approval_process_id", "step_no"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalProcessStep extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "approval_process_id", nullable = false)
    private Long approvalProcessId;

    @Column(name = "step_no", nullable = false)
    private short stepNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_mode", nullable = false, length = 5)
    private DecisionMode decisionMode;

    public ApprovalProcessStep(Long approvalProcessId, short stepNo, DecisionMode decisionMode) {
        this.approvalProcessId = approvalProcessId;
        this.stepNo = stepNo;
        this.decisionMode = decisionMode;
    }

    public void renumber(short stepNo) {
        this.stepNo = stepNo;
    }
}
