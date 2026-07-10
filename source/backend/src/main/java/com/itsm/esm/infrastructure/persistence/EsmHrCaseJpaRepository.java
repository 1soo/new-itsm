package com.itsm.esm.infrastructure.persistence;

import com.itsm.esm.domain.EsmHrCase;
import com.itsm.esm.domain.HrCaseStatus;
import com.itsm.esm.domain.repository.EsmHrCaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EsmHrCaseJpaRepository extends JpaRepository<EsmHrCase, Long>, EsmHrCaseRepository {

    @Override
    @Query("""
            select h from EsmHrCase h
            where h.isDeleted = false
              and (:status is null or h.status = :status)
            """)
    Page<EsmHrCase> search(@Param("status") HrCaseStatus status, Pageable pageable);
}
