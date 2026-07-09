package com.itsm.problem.infrastructure.persistence;

import com.itsm.problem.domain.KnownError;
import com.itsm.problem.domain.repository.KnownErrorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KnownErrorJpaRepository
        extends JpaRepository<KnownError, Long>, KnownErrorRepository {

    @Override
    @Query("""
            select k from KnownError k
            where k.isDeleted = false
              and (:keyword is null
                   or lower(k.title) like lower(concat('%', cast(:keyword as string), '%'))
                   or lower(k.rootCause) like lower(concat('%', cast(:keyword as string), '%')))
            """)
    Page<KnownError> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
