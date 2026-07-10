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
 * 카탈로그 항목의 체크리스트 하위 작업 템플릿(checklist_template_type != NONE인 항목에만 존재).
 * 요청 제출 시 복제되어 EsmChecklistTask 실행 인스턴스를 생성한다.
 */
@Getter
@Entity
@Table(name = "esm_checklist_template_task")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EsmChecklistTemplateTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "catalog_item_id", nullable = false)
    private Long catalogItemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Department department;

    @Column(name = "task_description", nullable = false, length = 300)
    private String taskDescription;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    public EsmChecklistTemplateTask(Long catalogItemId, Department department, String taskDescription, int sortOrder) {
        this.catalogItemId = catalogItemId;
        this.department = department;
        this.taskDescription = taskDescription;
        this.sortOrder = sortOrder;
    }
}
