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

    /** 게이트 매칭용: 도메인의 활성(is_deleted=false) 규칙 + 전체 도메인(domain null) 규칙. */
    List<ApprovalProcess> findByDomain(String domain);

    /** 관리자 목록 조회(domain 선택 필터). */
    Page<ApprovalProcess> search(String domain, Pageable pageable);

    /** tier=0(전체 미지정 캐치올) 존재 검증. */
    boolean existsByPriorityTier(short priorityTier);

    boolean existsByPriorityTierAndIdNot(short priorityTier, Long excludeId);

    /** tier=11(도메인만) 존재 검증. */
    boolean existsByDomainAndPriorityTier(String domain, short priorityTier);

    boolean existsByDomainAndPriorityTierAndIdNot(String domain, short priorityTier, Long excludeId);

    /** tier=23(도메인+요청유형) 존재 검증. */
    boolean existsByDomainAndRequestSubtypeKeyAndPriorityTier(String domain, String requestSubtypeKey, short priorityTier);

    boolean existsByDomainAndRequestSubtypeKeyAndPriorityTierAndIdNot(
            String domain, String requestSubtypeKey, short priorityTier, Long excludeId);

    /** tier=14(역할만) 역할 조합 중복 검증 대상 후보(전체 스코프). */
    List<ApprovalProcess> findByPriorityTier(short priorityTier);

    /** tier=25(도메인+역할) 역할 조합 중복 검증 대상 후보(동일 domain 스코프, requestSubtypeKey는 항상 null). */
    List<ApprovalProcess> findByDomainAndPriorityTier(String domain, short priorityTier);

    /** tier=37(도메인+요청유형+역할) 역할 조합 중복 검증 대상 후보(동일 domain+requestSubtypeKey 스코프). */
    List<ApprovalProcess> findByDomainAndRequestSubtypeKeyAndPriorityTier(
            String domain, String requestSubtypeKey, short priorityTier);

    /** tier=43(도메인+적용상태+역할) 역할 조합 중복 검증 대상 후보(동일 domain+targetState 스코프, requestSubtypeKey는 항상 null). */
    List<ApprovalProcess> findByDomainAndTargetStateAndPriorityTier(
            String domain, String targetState, short priorityTier);

    /** tier=55(도메인+적용상태+요청유형+역할) 역할 조합 중복 검증 대상 후보(동일 domain+targetState+requestSubtypeKey 스코프). */
    List<ApprovalProcess> findByDomainAndTargetStateAndRequestSubtypeKeyAndPriorityTier(
            String domain, String targetState, String requestSubtypeKey, short priorityTier);
}
