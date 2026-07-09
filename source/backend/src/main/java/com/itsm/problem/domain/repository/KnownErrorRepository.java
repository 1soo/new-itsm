package com.itsm.problem.domain.repository;

import com.itsm.problem.domain.KnownError;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 알려진 오류(KEDB) 저장소 포트.
 */
public interface KnownErrorRepository {

    KnownError save(KnownError knownError);

    Page<KnownError> searchByKeyword(String keyword, Pageable pageable);
}
