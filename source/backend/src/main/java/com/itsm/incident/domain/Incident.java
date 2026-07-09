package com.itsm.incident.domain;

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
 * 인시던트 티켓.
 */
@Getter
@Entity
@Table(name = "incident")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Incident extends BaseEntity {

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
    @Column(nullable = false, length = 10)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IncidentStatus status;

    @Column(name = "affected_service", length = 150)
    private String affectedService;

    @Column(name = "affected_product", length = 150)
    private String affectedProduct;

    @Column(name = "impact_start_at")
    private OffsetDateTime impactStartAt;

    @Column(name = "detected_at")
    private OffsetDateTime detectedAt;

    @Column(name = "impact_end_at")
    private OffsetDateTime impactEndAt;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    @Column(name = "mttd_minutes")
    private Integer mttdMinutes;

    @Column(name = "mtta_minutes")
    private Integer mttaMinutes;

    @Column(name = "mttr_minutes")
    private Integer mttrMinutes;

    public Incident(String ticketKey, String summary, String description, Severity severity,
                    String affectedService, String affectedProduct) {
        this.ticketKey = ticketKey;
        this.summary = summary;
        this.description = description;
        this.severity = severity;
        this.affectedService = affectedService;
        this.affectedProduct = affectedProduct;
        this.status = IncidentStatus.NEW;
    }

    public void changeStatus(IncidentStatus status) {
        this.status = status;
    }

    public void changeSeverityAndPriority(Severity severity, Priority priority) {
        if (severity != null) this.severity = severity;
        if (priority != null) this.priority = priority;
    }

    public void resolve(OffsetDateTime impactStartAt, OffsetDateTime detectedAt, OffsetDateTime impactEndAt,
                        OffsetDateTime resolvedAt, Integer mttd, Integer mtta, Integer mttr) {
        if (impactStartAt != null) this.impactStartAt = impactStartAt;
        if (detectedAt != null) this.detectedAt = detectedAt;
        if (impactEndAt != null) this.impactEndAt = impactEndAt;
        this.resolvedAt = resolvedAt;
        this.mttdMinutes = mttd;
        this.mttaMinutes = mtta;
        this.mttrMinutes = mttr;
        this.status = IncidentStatus.RESOLVED;
    }

    /** SEV1(major)은 포스트모템 필수. */
    public boolean isPostmortemRequired() {
        return severity == Severity.SEV1;
    }
}
