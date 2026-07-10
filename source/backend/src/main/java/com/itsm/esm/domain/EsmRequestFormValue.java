package com.itsm.esm.domain;

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

/**
 * 부서 요청 양식 입력 값(EAV).
 */
@Getter
@Entity
@Table(name = "esm_request_form_value",
        uniqueConstraints = @UniqueConstraint(columnNames = {"esm_request_id", "field_key"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EsmRequestFormValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "esm_request_id", nullable = false)
    private Long esmRequestId;

    @Column(name = "field_key", nullable = false, length = 50)
    private String fieldKey;

    @Column(name = "field_value", columnDefinition = "text")
    private String fieldValue;

    public EsmRequestFormValue(Long esmRequestId, String fieldKey, String fieldValue) {
        this.esmRequestId = esmRequestId;
        this.fieldKey = fieldKey;
        this.fieldValue = fieldValue;
    }
}
