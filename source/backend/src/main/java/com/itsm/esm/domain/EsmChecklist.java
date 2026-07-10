package com.itsm.esm.domain;

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
 * 온보딩/오프보딩 체크리스트. 하위 작업(EsmChecklistTask)이 모두 DONE이면 COMPLETED로 자동 갱신된다.
 */
@Getter
@Entity
@Table(name = "esm_checklist")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EsmChecklist extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ChecklistTemplateType type;

    @Column(name = "target_user_name", nullable = false, length = 100)
    private String targetUserName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ChecklistStatus status;

    public EsmChecklist(ChecklistTemplateType type, String targetUserName) {
        this.type = type;
        this.targetUserName = targetUserName;
        this.status = ChecklistStatus.IN_PROGRESS;
    }

    public void changeStatus(ChecklistStatus status) {
        this.status = status;
    }
}
