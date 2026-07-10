package com.itsm.esm.infrastructure.persistence;

import com.itsm.esm.domain.EsmRequestFormValue;
import com.itsm.esm.domain.repository.EsmRequestFormValueRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EsmRequestFormValueJpaRepository
        extends JpaRepository<EsmRequestFormValue, Long>, EsmRequestFormValueRepository {
}
