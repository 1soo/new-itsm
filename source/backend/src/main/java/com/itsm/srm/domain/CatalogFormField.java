package com.itsm.srm.domain;

import com.itsm.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 요청 유형 동적 양식 필드. options는 select 옵션 목록(JSON 배열 문자열).
 */
@Getter
@Entity
@Table(name = "catalog_form_field",
        uniqueConstraints = @UniqueConstraint(columnNames = {"catalog_item_id", "field_key"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogFormField extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "catalog_item_id", nullable = false)
    private Long catalogItemId;

    @Column(name = "field_key", nullable = false, length = 50)
    private String fieldKey;

    @Column(nullable = false, length = 150)
    private String label;

    @Column(name = "field_type", nullable = false, length = 20)
    private String fieldType;

    @Column(nullable = false)
    private boolean required = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String options;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    public CatalogFormField(Long catalogItemId, String fieldKey, String label, String fieldType,
                            boolean required, String options, int sortOrder) {
        this.catalogItemId = catalogItemId;
        this.fieldKey = fieldKey;
        this.label = label;
        this.fieldType = fieldType;
        this.required = required;
        this.options = options;
        this.sortOrder = sortOrder;
    }
}
