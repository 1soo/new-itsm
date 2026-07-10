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
 * 체크리스트 하위 작업(실행 인스턴스). relatedAssetId는 오프보딩 자산 회수 작업에서만 채워진다.
 */
@Getter
@Entity
@Table(name = "esm_checklist_task")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EsmChecklistTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "checklist_id", nullable = false)
    private Long checklistId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Department department;

    @Column(nullable = false, length = 300)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ChecklistTaskStatus status;

    @Column(name = "related_asset_id")
    private Long relatedAssetId;

    public EsmChecklistTask(Long checklistId, Department department, String description, Long relatedAssetId) {
        this.checklistId = checklistId;
        this.department = department;
        this.description = description;
        this.relatedAssetId = relatedAssetId;
        this.status = ChecklistTaskStatus.PENDING;
    }

    public void markDone() {
        this.status = ChecklistTaskStatus.DONE;
    }
}
