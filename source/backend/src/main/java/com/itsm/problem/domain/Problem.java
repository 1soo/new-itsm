package com.itsm.problem.domain;

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
 * 문제 티켓. 근본 원인(RCA)·워크어라운드·상태(6단계)를 관리한다.
 */
@Getter
@Entity
@Table(name = "problem")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Problem extends BaseEntity {

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
    @Column(length = 10)
    private ProblemOrigin origin;

    @Column(name = "investigation_reason", length = 500)
    private String investigationReason;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Level impact;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Level urgency;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private ProblemPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProblemStatus status;

    @Column(name = "root_cause", length = 1000)
    private String rootCause;

    @Column(name = "root_cause_category", length = 100)
    private String rootCauseCategory;

    @Column(columnDefinition = "text")
    private String workaround;

    @Column(length = 150)
    private String component;

    public Problem(String ticketKey, String summary, String description, ProblemOrigin origin,
                   String investigationReason, Level impact, Level urgency, String component) {
        this.ticketKey = ticketKey;
        this.summary = summary;
        this.description = description;
        this.origin = origin;
        this.investigationReason = investigationReason;
        this.impact = impact;
        this.urgency = urgency;
        this.component = component;
        this.priority = ProblemPriority.of(impact, urgency);
        this.status = ProblemStatus.DETECTION;
    }

    public void changeStatus(ProblemStatus status) {
        this.status = status;
    }

    public void updateRca(String rootCause, String rootCauseCategory) {
        this.rootCause = rootCause;
        this.rootCauseCategory = rootCauseCategory;
    }

    public void updateWorkaround(String workaround) {
        this.workaround = workaround;
    }

    public boolean isClosed() {
        return this.status == ProblemStatus.RESOLVED_CLOSED;
    }
}
