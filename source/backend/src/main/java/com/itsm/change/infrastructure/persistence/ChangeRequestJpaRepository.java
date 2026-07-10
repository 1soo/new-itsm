package com.itsm.change.infrastructure.persistence;

import com.itsm.change.domain.ChangeRequest;
import com.itsm.change.domain.ChangeRisk;
import com.itsm.change.domain.ChangeStatus;
import com.itsm.change.domain.ChangeType;
import com.itsm.change.domain.repository.ChangeRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ChangeRequestJpaRepository extends JpaRepository<ChangeRequest, Long>, ChangeRequestRepository {

    @Override
    @Query("""
            select c from ChangeRequest c
            where c.isDeleted = false
              and (:type is null or c.type = :type)
              and (:status is null or c.status = :status)
              and (:risk is null or c.risk = :risk)
              and c.createdAt >= :from and c.createdAt <= :to
            """)
    Page<ChangeRequest> search(@Param("type") ChangeType type,
                               @Param("status") ChangeStatus status,
                               @Param("risk") ChangeRisk risk,
                               @Param("from") OffsetDateTime from,
                               @Param("to") OffsetDateTime to,
                               Pageable pageable);

    @Override
    @Query("""
            select c from ChangeRequest c
            where c.isDeleted = false
              and c.scheduledAt is not null
              and (:type is null or c.type = :type)
              and c.scheduledAt >= :from and c.scheduledAt <= :to
            order by c.scheduledAt asc
            """)
    List<ChangeRequest> findSchedule(@Param("type") ChangeType type,
                                     @Param("from") OffsetDateTime from,
                                     @Param("to") OffsetDateTime to);

    @Override
    @Query("""
            select c from ChangeRequest c
            where c.isDeleted = false
              and (:keyword is null
                   or lower(c.summary) like lower(concat('%', cast(:keyword as string), '%'))
                   or lower(c.description) like lower(concat('%', cast(:keyword as string), '%')))
            """)
    Page<ChangeRequest> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
