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
 * HR 케이스(민감 정보). HR_CASE_MANAGER 역할만 조회·처리 가능(애플리케이션 레벨 강제).
 */
@Getter
@Entity
@Table(name = "esm_hr_case")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EsmHrCase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "subject_user_name", length = 100)
    private String subjectUserName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private HrCaseStatus status;

    public EsmHrCase(String title, String description, String subjectUserName) {
        this.title = title;
        this.description = description;
        this.subjectUserName = subjectUserName;
        this.status = HrCaseStatus.INTAKE;
    }

    public void changeStatus(HrCaseStatus status) {
        this.status = status;
    }
}
