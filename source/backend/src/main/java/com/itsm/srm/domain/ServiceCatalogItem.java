package com.itsm.srm.domain;

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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 요청 유형(카탈로그 항목). 동적 양식은 formSchema(자체 8×n 그리드 스키마, components 배열, JSONB)에 통째로 저장한다
 * (2026-07-18 유지보수 요청, form.io 완전 제거).
 * 승인 필요 여부는 더 이상 카탈로그 항목의 고정 속성이 아니다(승인 프로세스 커스텀 기능으로 완전 대체 —
 * SYSTEM_ADMIN이 도메인=SERVICE_REQUEST, 요청유형=이 항목으로 별도 설정, docs/02_plan/api_spec/auth.md API-AUTH-027).
 */
@Getter
@Entity
@Table(name = "service_catalog_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceCatalogItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "sla_response_minutes")
    private Integer slaResponseMinutes;

    @Column(name = "sla_resolve_minutes")
    private Integer slaResolveMinutes;

    @Column(name = "assignee_role_id")
    private Long assigneeRoleId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_schema", nullable = false, columnDefinition = "jsonb")
    private String formSchema;

    public ServiceCatalogItem(String name, String description, Long categoryId,
                              Integer slaResponseMinutes, Integer slaResolveMinutes,
                              Long assigneeRoleId, String formSchema) {
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.slaResponseMinutes = slaResponseMinutes;
        this.slaResolveMinutes = slaResolveMinutes;
        this.assigneeRoleId = assigneeRoleId;
        this.formSchema = formSchema;
    }

    public void update(String name, String description, Long categoryId,
                       Integer slaResponseMinutes, Integer slaResolveMinutes,
                       Long assigneeRoleId, String formSchema) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (categoryId != null) this.categoryId = categoryId;
        if (slaResponseMinutes != null) this.slaResponseMinutes = slaResponseMinutes;
        if (slaResolveMinutes != null) this.slaResolveMinutes = slaResolveMinutes;
        if (assigneeRoleId != null) this.assigneeRoleId = assigneeRoleId;
        if (formSchema != null) this.formSchema = formSchema;
    }
}
