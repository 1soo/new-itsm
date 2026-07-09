package com.itsm.srm.domain.repository;

import com.itsm.srm.domain.ServiceRequestFormValue;

import java.util.List;

/**
 * 요청 양식 값 저장소 포트.
 */
public interface ServiceRequestFormValueRepository {

    ServiceRequestFormValue save(ServiceRequestFormValue value);

    List<ServiceRequestFormValue> findByServiceRequestId(Long serviceRequestId);
}
