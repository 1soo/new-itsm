package com.itsm.esm.domain.repository;

import com.itsm.esm.domain.EsmHrCase;
import com.itsm.esm.domain.HrCaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * HR 케이스 저장소 포트.
 */
public interface EsmHrCaseRepository {

    EsmHrCase save(EsmHrCase hrCase);

    Optional<EsmHrCase> findById(Long id);

    Page<EsmHrCase> search(HrCaseStatus status, Pageable pageable);
}
