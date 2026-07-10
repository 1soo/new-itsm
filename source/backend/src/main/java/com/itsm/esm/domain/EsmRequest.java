package com.itsm.esm.domain;

import com.itsm.auth.domain.Department;
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

/**
 * 부서 요청 티켓.
 */
@Getter
@Entity
@Table(name = "esm_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EsmRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_key", nullable = false, unique = true, length = 20)
    private String ticketKey;

    @Column(name = "catalog_item_id", nullable = false)
    private Long catalogItemId;

    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Column(name = "assignee_id")
    private Long assigneeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Department department;

    @Column(name = "target_user_name", length = 100)
    private String targetUserName;

    @Column(name = "checklist_id", unique = true)
    private Long checklistId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EsmRequestStatus status;

    public EsmRequest(String ticketKey, Long catalogItemId, Long requesterId, Department department,
                      String targetUserName, Long checklistId) {
        this.ticketKey = ticketKey;
        this.catalogItemId = catalogItemId;
        this.requesterId = requesterId;
        this.department = department;
        this.targetUserName = targetUserName;
        this.checklistId = checklistId;
        this.status = EsmRequestStatus.SUBMITTED;
    }

    public void changeStatus(EsmRequestStatus status) {
        this.status = status;
    }

    public void assignTo(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
}
