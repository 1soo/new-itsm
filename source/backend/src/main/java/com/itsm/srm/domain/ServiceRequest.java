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

import java.time.OffsetDateTime;

/**
 * 서비스 요청 티켓.
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

    @Column(name = "queue_id")
    private Long queueId;

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

    public ServiceRequest(String ticketKey, Long catalogItemId, Long requesterId, Long queueId,
                          OffsetDateTime slaResponseDue, OffsetDateTime slaResolveDue) {
        this.ticketKey = ticketKey;
        this.catalogItemId = catalogItemId;
        this.requesterId = requesterId;
        this.queueId = queueId;
        this.status = RequestStatus.SUBMITTED;
        this.slaResponseDue = slaResponseDue;
        this.slaResolveDue = slaResolveDue;
        this.slaStatus = SlaStatus.OK;
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
