package com.itsm.compliance.domain;

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
 * 시정조치 항목. 요구사항 1:N(반복 등록 가능), DETECTED→IN_PROGRESS→RESOLVED 순차 전이.
 */
@Getter
@Entity
@Table(name = "corrective_action")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CorrectiveAction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requirement_id", nullable = false)
    private Long requirementId;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CorrectiveActionStatus status;

    public CorrectiveAction(Long requirementId, String description) {
        this.requirementId = requirementId;
        this.description = description;
        this.status = CorrectiveActionStatus.DETECTED;
    }

    public void changeStatus(CorrectiveActionStatus status) {
        this.status = status;
    }
}
