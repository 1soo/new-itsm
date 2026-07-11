package com.itsm.common.approval.domain.repository;

import com.itsm.common.approval.domain.ApprovalProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 승인 프로세스 정의(규칙 헤더) 저장소 포트.
 */
public interface ApprovalProcessRepository {

    ApprovalProcess save(ApprovalProcess process);

    Optional<ApprovalProcess> findById(Long id);

    /** 게이트 매칭용: 도메인의 활성(is_deleted=false) 규칙 전체. */
    List<ApprovalProcess> findByDomain(String domain);

    /** 관리자 목록 조회(domain 선택 필터). */
    Page<ApprovalProcess> search(String domain, Pageable pageable);

    boolean existsByDomainAndPriorityTier(String domain, short priorityTier);

    boolean existsByDomainAndPriorityTierAndIdNot(String domain, short priorityTier, Long excludeId);

    boolean existsByDomainAndRequestSubtypeKeyAndPriorityTier(String domain, String requestSubtypeKey, short priorityTier);

    boolean existsByDomainAndRequestSubtypeKeyAndPriorityTierAndIdNot(
            String domain, String requestSubtypeKey, short priorityTier, Long excludeId);

    /** tier=3(승인요청자 역할 전용) 역할 조합 중복 검증 대상 후보(동일 domain+requestSubtypeKey 스코프). */
    List<ApprovalProcess> findByDomainAndRequestSubtypeKeyAndPriorityTier(
            String domain, String requestSubtypeKey, short priorityTier);
}
