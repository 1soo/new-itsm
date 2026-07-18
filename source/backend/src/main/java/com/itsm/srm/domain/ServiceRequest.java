package com.itsm.srm.domain;

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

import java.time.OffsetDateTime;

/**
 * 서비스 요청 티켓. 양식 제출 데이터는 formValues(Form.io submission.data, JSONB)에 통째로 저장한다
 * (2026-07-17 유지보수 요청, 기존 ServiceRequestFormValue EAV 대체).
 */
@Getter
@Entity
@Table(name = "service_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceRequest extends BaseEntity {

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
    private RequestStatus status;

    @Column(name = "sla_response_due")
    private OffsetDateTime slaResponseDue;

    @Column(name = "sla_resolve_due")
    private OffsetDateTime slaResolveDue;

    @Enumerated(EnumType.STRING)
    @Column(name = "sla_status", nullable = false, length = 10)
    private SlaStatus slaStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_values", nullable = false, columnDefinition = "jsonb")
    private String formValues;

    public ServiceRequest(String ticketKey, Long catalogItemId, Long requesterId,
                          OffsetDateTime slaResponseDue, OffsetDateTime slaResolveDue, String formValues) {
        this.ticketKey = ticketKey;
        this.catalogItemId = catalogItemId;
        this.requesterId = requesterId;
        this.status = RequestStatus.SUBMITTED;
        this.slaResponseDue = slaResponseDue;
        this.slaResolveDue = slaResolveDue;
        this.slaStatus = SlaStatus.OK;
        this.formValues = formValues;
    }

    public void changeStatus(RequestStatus status) {
        this.status = status;
    }

    public void assignTo(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public void updateSlaStatus(SlaStatus slaStatus) {
        this.slaStatus = slaStatus;
    }
}
