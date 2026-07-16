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
 * 서비스 카탈로그 카테고리(관리자가 통제하는 고정 목록, 2026-07-16 유지보수 요청).
 */
@Getter
@Entity
@Table(name = "service_catalog_category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceCatalogCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public ServiceCatalogCategory(String name, Integer sortOrder) {
        this.name = name;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }

    public void update(String name, Integer sortOrder) {
        if (name != null) this.name = name;
        if (sortOrder != null) this.sortOrder = sortOrder;
    }
}
