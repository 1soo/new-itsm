package com.itsm.common.approval.infrastructure.persistence;

import com.itsm.common.approval.domain.ApprovalProcess;
import com.itsm.common.approval.domain.repository.ApprovalProcessRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ApprovalProcessRepository 포트의 Spring Data JPA 구현.
 */
public interface ApprovalProcessJpaRepository extends JpaRepository<ApprovalProcess, Long>, ApprovalProcessRepository {

    @Override
    @Query("select p from ApprovalProcess p where p.isDeleted = false and p.domain = :domain")
    List<ApprovalProcess> findByDomain(@Param("domain") String domain);

    @Override
    @Query("""
            select p from ApprovalProcess p
            where p.isDeleted = false
              and (:domain is null or p.domain = cast(:domain as string))
            """)
    Page<ApprovalProcess> search(@Param("domain") String domain, Pageable pageable);

    @Override
    @Query("""
            select case when count(p) > 0 then true else false end from ApprovalProcess p
            where p.isDeleted = false and p.domain = :domain and p.priorityTier = :tier
            """)
    boolean existsByDomainAndPriorityTier(@Param("domain") String domain, @Param("tier") short priorityTier);

    @Override
    @Query("""
            select case when count(p) > 0 then true else false end from ApprovalProcess p
            where p.isDeleted = false and p.domain = :domain and p.priorityTier = :tier and p.id <> :excludeId
            """)
    boolean existsByDomainAndPriorityTierAndIdNot(@Param("domain") String domain, @Param("tier") short priorityTier,
                                                   @Param("excludeId") Long excludeId);

    @Override
    @Query("""
            select case when count(p) > 0 then true else false end from ApprovalProcess p
            where p.isDeleted = false and p.domain = :domain and p.priorityTier = :tier
              and ((:key is null and p.requestSubtypeKey is null) or p.requestSubtypeKey = cast(:key as string))
            """)
    boolean existsByDomainAndRequestSubtypeKeyAndPriorityTier(@Param("domain") String domain, @Param("key") String requestSubtypeKey,
                                                               @Param("tier") short priorityTier);

    @Override
    @Query("""
            select case when count(p) > 0 then true else false end from ApprovalProcess p
            where p.isDeleted = false and p.domain = :domain and p.priorityTier = :tier and p.id <> :excludeId
              and ((:key is null and p.requestSubtypeKey is null) or p.requestSubtypeKey = cast(:key as string))
            """)
    boolean existsByDomainAndRequestSubtypeKeyAndPriorityTierAndIdNot(
            @Param("domain") String domain, @Param("key") String requestSubtypeKey,
            @Param("tier") short priorityTier, @Param("excludeId") Long excludeId);

    @Override
    @Query("""
            select p from ApprovalProcess p
            where p.isDeleted = false and p.domain = :domain and p.priorityTier = :tier
              and ((:key is null and p.requestSubtypeKey is null) or p.requestSubtypeKey = cast(:key as string))
            """)
    List<ApprovalProcess> findByDomainAndRequestSubtypeKeyAndPriorityTier(
            @Param("domain") String domain, @Param("key") String requestSubtypeKey, @Param("tier") short priorityTier);
}
