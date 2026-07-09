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

/**
 * 요청 양식 입력 값(EAV).
 */
@Getter
@Entity
@Table(name = "service_request_form_value",
        uniqueConstraints = @UniqueConstraint(columnNames = {"service_request_id", "field_key"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ServiceRequestFormValue extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_request_id", nullable = false)
    private Long serviceRequestId;

    @Column(name = "field_key", nullable = false, length = 50)
    private String fieldKey;

    @Column(name = "field_value", columnDefinition = "text")
    private String fieldValue;

    public ServiceRequestFormValue(Long serviceRequestId, String fieldKey, String fieldValue) {
        this.serviceRequestId = serviceRequestId;
        this.fieldKey = fieldKey;
        this.fieldValue = fieldValue;
    }
}
