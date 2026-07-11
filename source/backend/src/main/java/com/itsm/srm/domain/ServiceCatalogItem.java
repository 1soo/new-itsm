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

/**
 * 요청 유형(카탈로그 항목). 동적 양식은 CatalogFormField로 분리.
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

    @Column(length = 100)
    private String category;

    @Column(name = "queue_id")
    private Long queueId;

    @Column(name = "sla_response_minutes")
    private Integer slaResponseMinutes;

    @Column(name = "sla_resolve_minutes")
    private Integer slaResolveMinutes;

    public ServiceCatalogItem(String name, String description, String category,
                              Long queueId, Integer slaResponseMinutes, Integer slaResolveMinutes) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.queueId = queueId;
        this.slaResponseMinutes = slaResponseMinutes;
        this.slaResolveMinutes = slaResolveMinutes;
    }

    public void update(String name, String description, String category,
                       Long queueId, Integer slaResponseMinutes, Integer slaResolveMinutes) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (category != null) this.category = category;
        if (queueId != null) this.queueId = queueId;
        if (slaResponseMinutes != null) this.slaResponseMinutes = slaResponseMinutes;
        if (slaResolveMinutes != null) this.slaResolveMinutes = slaResolveMinutes;
    }
}
