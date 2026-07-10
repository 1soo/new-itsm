package com.itsm.esm.domain.repository;

import com.itsm.esm.domain.EsmRequestFormValue;

import java.util.List;

/**
 * 부서 요청 양식 값 저장소 포트.
 */
public interface EsmRequestFormValueRepository {

    EsmRequestFormValue save(EsmRequestFormValue value);

    List<EsmRequestFormValue> findByEsmRequestId(Long esmRequestId);
}
