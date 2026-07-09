package com.itsm.srm.infrastructure.persistence;

import com.itsm.srm.domain.ServiceRequestFormValue;
import com.itsm.srm.domain.repository.ServiceRequestFormValueRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRequestFormValueJpaRepository
        extends JpaRepository<ServiceRequestFormValue, Long>, ServiceRequestFormValueRepository {
}
