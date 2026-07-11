package com.itsm.common.approval.domain;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 인스턴스 차수별 필요 역할 스냅샷(AND 집계의 분모).
 */
@Getter
@Entity
@Table(name = "approval_request_step_role",
        uniqueConstraints = @UniqueConstraint(columnNames = {"step_id", "role_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalRequestStepRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "step_id", nullable = false)
    private Long stepId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    public ApprovalRequestStepRole(Long stepId, Long roleId) {
        this.stepId = stepId;
        this.roleId = roleId;
    }
}
