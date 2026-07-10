package com.itsm.compliance.domain.repository;

import com.itsm.compliance.domain.ComplianceRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 컴플라이언스 요구사항 저장소 포트.
 */
public interface ComplianceRequirementRepository {

    ComplianceRequirement save(ComplianceRequirement requirement);

    Optional<ComplianceRequirement> findById(Long id);

    /**
     * complianceStatus는 corrective_action 미해결 존재 여부에 대한 EXISTS 서브쿼리로 필터링(계산값, 저장 컬럼 아님).
     * 저장 컬럼이 아니라 타입 안전 enum 대신 "COMPLIANT"/"NON_COMPLIANT" 문자열(또는 null)로 받는다.
     */
    Page<ComplianceRequirement> search(String complianceStatus, Boolean ownerAssigned, String keyword,
                                        Pageable pageable);

    long countByRequirementKeyStartingWith(String prefix);

    List<ComplianceRequirement> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);
}
