package com.itsm.compliance.infrastructure.persistence;

import com.itsm.compliance.domain.ComplianceRequirement;
import com.itsm.compliance.domain.repository.ComplianceRequirementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ComplianceRequirementJpaRepository
        extends JpaRepository<ComplianceRequirement, Long>, ComplianceRequirementRepository {

    @Override
    @Query("""
            select r from ComplianceRequirement r
            where r.isDeleted = false
              and (:ownerAssigned is null
                   or (:ownerAssigned = true and r.ownerId is not null)
                   or (:ownerAssigned = false and r.ownerId is null))
              and (:keyword is null
                   or lower(r.name) like lower(concat('%', cast(:keyword as string), '%'))
                   or lower(r.basis) like lower(concat('%', cast(:keyword as string), '%')))
              and (:complianceStatus is null
                   or (:complianceStatus = 'NON_COMPLIANT' and exists (
                        select 1 from CorrectiveAction a
                        where a.requirementId = r.id and a.isDeleted = false
                          and a.status in (com.itsm.compliance.domain.CorrectiveActionStatus.DETECTED,
                                           com.itsm.compliance.domain.CorrectiveActionStatus.IN_PROGRESS)))
                   or (:complianceStatus = 'COMPLIANT' and not exists (
                        select 1 from CorrectiveAction a
                        where a.requirementId = r.id and a.isDeleted = false
                          and a.status in (com.itsm.compliance.domain.CorrectiveActionStatus.DETECTED,
                                           com.itsm.compliance.domain.CorrectiveActionStatus.IN_PROGRESS))))
            order by r.updatedAt desc nulls last, r.createdAt desc
            """)
    Page<ComplianceRequirement> search(@Param("complianceStatus") String complianceStatus,
                                        @Param("ownerAssigned") Boolean ownerAssigned,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);

    @Override
    long countByRequirementKeyStartingWith(String prefix);

    @Override
    List<ComplianceRequirement> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);
}
