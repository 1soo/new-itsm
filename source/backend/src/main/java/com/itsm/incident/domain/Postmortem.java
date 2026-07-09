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
 * 포스트모템(1:1 인시던트). root_cause 필수.
 */
@Getter
@Entity
@Table(name = "postmortem")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Postmortem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false, unique = true)
    private Long incidentId;

    @Column(columnDefinition = "text")
    private String summary;

    @Column(name = "timeline_summary", columnDefinition = "text")
    private String timelineSummary;

    @Column(name = "root_cause", nullable = false, length = 500)
    private String rootCause;

    public Postmortem(Long incidentId, String summary, String timelineSummary, String rootCause) {
        this.incidentId = incidentId;
        this.summary = summary;
        this.timelineSummary = timelineSummary;
        this.rootCause = rootCause;
    }

    public void update(String summary, String timelineSummary, String rootCause) {
        this.summary = summary;
        this.timelineSummary = timelineSummary;
        this.rootCause = rootCause;
    }
}
