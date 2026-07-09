package com.itsm.incident.domain;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 심각도·우선순위 변경 이력(append).
 */
@Getter
@Entity
@Table(name = "incident_severity_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IncidentSeverityHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    @Column(name = "old_severity", length = 10)
    private String oldSeverity;

    @Column(name = "new_severity", length = 10)
    private String newSeverity;

    @Column(name = "old_priority", length = 10)
    private String oldPriority;

    @Column(name = "new_priority", length = 10)
    private String newPriority;

    public IncidentSeverityHistory(Long incidentId, String oldSeverity, String newSeverity,
                                   String oldPriority, String newPriority) {
        this.incidentId = incidentId;
        this.oldSeverity = oldSeverity;
        this.newSeverity = newSeverity;
        this.oldPriority = oldPriority;
        this.newPriority = newPriority;
    }
}
