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
 * 차수별 승인 역할(1개 이상).
 */
@Getter
@Entity
@Table(name = "approval_process_step_role",
        uniqueConstraints = @UniqueConstraint(columnNames = {"step_id", "role_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalProcessStepRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "step_id", nullable = false)
    private Long stepId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    public ApprovalProcessStepRole(Long stepId, Long roleId) {
        this.stepId = stepId;
        this.roleId = roleId;
    }
}
