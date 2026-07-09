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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대응 역할 배정(Tech Lead/Comms/Scribe).
 */
@Getter
@Entity
@Table(name = "incident_responder",
        uniqueConstraints = @UniqueConstraint(columnNames = {"incident_id", "user_id", "response_role"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IncidentResponder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "response_role", nullable = false, length = 20)
    private ResponseRole responseRole;

    public IncidentResponder(Long incidentId, Long userId, ResponseRole responseRole) {
        this.incidentId = incidentId;
        this.userId = userId;
        this.responseRole = responseRole;
    }
}
