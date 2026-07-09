package com.itsm.change.domain;

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
 * 변경 요청(RFC). 유형·위험도·승인 경로·6단계 상태·구현 결과를 관리한다.
 */
@Getter
@Entity
@Table(name = "change_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChangeRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_key", nullable = false, unique = true, length = 20)
    private String ticketKey;

    @Column(nullable = false, length = 300)
    private String summary;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ChangeType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ChangeRisk risk;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChangeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_route", length = 15)
    private ApprovalRoute approvalRoute;

    @Column(name = "implementation_plan", columnDefinition = "text")
    private String implementationPlan;

    @Column(name = "rollback_plan", columnDefinition = "text")
    private String rollbackPlan;

    @Column(name = "scheduled_at")
    private OffsetDateTime scheduledAt;

    @Column(name = "template_id")
    private Long templateId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Outcome outcome;

    @Column(name = "rolled_back")
    private Boolean rolledBack;

    @Column(name = "result_note", length = 500)
    private String resultNote;

    public ChangeRequest(String ticketKey, String summary, String description, ChangeType type, ChangeRisk risk,
                          ApprovalRoute approvalRoute, String implementationPlan, String rollbackPlan,
                          OffsetDateTime scheduledAt, Long templateId) {
        this.ticketKey = ticketKey;
        this.summary = summary;
        this.description = description;
        this.type = type;
        this.risk = risk;
        this.status = ChangeStatus.REQUESTED;
        this.approvalRoute = approvalRoute;
        this.implementationPlan = implementationPlan;
        this.rollbackPlan = rollbackPlan;
        this.scheduledAt = scheduledAt;
        this.templateId = templateId;
    }

    public void changeStatus(ChangeStatus status) {
        this.status = status;
    }

    public void updateClassification(ChangeType type, ChangeRisk risk) {
        this.type = type;
        this.risk = risk;
    }

    public void updateApprovalRoute(ApprovalRoute approvalRoute) {
        this.approvalRoute = approvalRoute;
    }

    public void recordResult(Outcome outcome, Boolean rolledBack, String resultNote) {
        this.outcome = outcome;
        this.rolledBack = rolledBack;
        this.resultNote = resultNote;
    }
}
