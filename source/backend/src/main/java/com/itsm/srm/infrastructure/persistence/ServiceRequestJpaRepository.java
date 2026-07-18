package com.itsm.srm.infrastructure.persistence;

import com.itsm.srm.domain.RequestStatus;
import com.itsm.srm.domain.ServiceRequest;
import com.itsm.srm.domain.repository.ServiceRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface ServiceRequestJpaRepository extends JpaRepository<ServiceRequest, Long>, ServiceRequestRepository {

    @Override
    @Query("""
            select r from ServiceRequest r
            where r.isDeleted = false
              and (:requesterId is null or r.requesterId = :requesterId)
              and (
                    (:categoryId is null and :uncategorized = false)
                 or exists (select 1 from ServiceCatalogItem c
                            where c.id = r.catalogItemId
                              and ((:uncategorized = true and c.categoryId is null)
                                or (:uncategorized = false and c.categoryId = :categoryId)))
              )
              and (:status is null or r.status = :status)
              and r.createdAt >= :from and r.createdAt <= :to
            """)
    Page<ServiceRequest> search(@Param("requesterId") Long requesterId,
                                @Param("categoryId") Long categoryId,
                                @Param("uncategorized") boolean uncategorized,
                                @Param("status") RequestStatus status,
                                @Param("from") OffsetDateTime from,
                                @Param("to") OffsetDateTime to,
                                Pageable pageable);

    @Override
    @Query("""
            select count(r) from ServiceRequest r
            where r.isDeleted = false
              and r.status <> com.itsm.srm.domain.RequestStatus.CLOSED
              and exists (select 1 from ServiceCatalogItem c
                          where c.id = r.catalogItemId and c.categoryId = :categoryId)
            """)
    long countOpenByCategoryId(@Param("categoryId") Long categoryId);

    @Override
    @Query("""
            select count(r) from ServiceRequest r
            where r.isDeleted = false
              and r.status <> com.itsm.srm.domain.RequestStatus.CLOSED
              and exists (select 1 from ServiceCatalogItem c
                          where c.id = r.catalogItemId and c.categoryId is null)
            """)
    long countOpenUncategorized();

    @Override
    @Query("""
            select r from ServiceRequest r
            where r.isDeleted = false
              and (:requesterId is null or r.requesterId = :requesterId)
              and (:keyword is null
                   or lower(r.ticketKey) like lower(concat('%', cast(:keyword as string), '%'))
                   or exists (select 1 from ServiceCatalogItem c
                              where c.id = r.catalogItemId
                                and lower(c.name) like lower(concat('%', cast(:keyword as string), '%'))))
            """)
    Page<ServiceRequest> searchByKeyword(@Param("requesterId") Long requesterId,
                                         @Param("keyword") String keyword,
                                         Pageable pageable);
}
