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
 * 규칙의 승인요청자 역할 스코프(ANY 매칭). 0개면 요청자 무관.
 */
@Getter
@Entity
@Table(name = "approval_process_requester_role",
        uniqueConstraints = @UniqueConstraint(columnNames = {"approval_process_id", "role_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApprovalProcessRequesterRole extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "approval_process_id", nullable = false)
    private Long approvalProcessId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    public ApprovalProcessRequesterRole(Long approvalProcessId, Long roleId) {
        this.approvalProcessId = approvalProcessId;
        this.roleId = roleId;
    }
}
