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
 * 부서별 요청 유형(카탈로그 항목). checklistTemplateType이 ONBOARDING/OFFBOARDING이면
 * 요청 제출 시(API-ESM-005) EsmChecklistTemplateTask를 복제해 체크리스트를 자동 생성한다.
 */
@Getter
@Entity
@Table(name = "esm_catalog_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EsmCatalogItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "checklist_template_type", nullable = false, length = 15)
    private ChecklistTemplateType checklistTemplateType;

    public EsmCatalogItem(String name, String description, Department department,
                          ChecklistTemplateType checklistTemplateType) {
        this.name = name;
        this.description = description;
        this.department = department;
        this.checklistTemplateType = checklistTemplateType == null ? ChecklistTemplateType.NONE : checklistTemplateType;
    }

    public void update(String name, String description, Department department, ChecklistTemplateType checklistTemplateType) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (department != null) {
            this.department = department;
        }
        if (checklistTemplateType != null) {
            this.checklistTemplateType = checklistTemplateType;
        }
    }
}
