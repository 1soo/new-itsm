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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 부서 요청 티켓. 양식 제출 데이터는 formValues(컴포넌트 key 기준 key-value 맵, JSONB)에 통째로 저장한다
 * (2026-07-19 유지보수 요청, 레거시 EAV 폐기, SRM ServiceRequest.formValues와 동일 패턴).
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_values", nullable = false, columnDefinition = "jsonb")
    private String formValues;

    public EsmRequest(String ticketKey, Long catalogItemId, Long requesterId, Department department,
                      String targetUserName, Long checklistId, String formValues) {
        this.ticketKey = ticketKey;
        this.catalogItemId = catalogItemId;
        this.requesterId = requesterId;
        this.department = department;
        this.targetUserName = targetUserName;
        this.checklistId = checklistId;
        this.status = EsmRequestStatus.SUBMITTED;
        this.formValues = formValues;
    }

    public void changeStatus(EsmRequestStatus status) {
        this.status = status;
    }

    public void assignTo(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
}
