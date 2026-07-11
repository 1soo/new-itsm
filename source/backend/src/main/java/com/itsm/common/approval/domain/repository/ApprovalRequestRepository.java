package com.itsm.common.approval.domain.repository;

import com.itsm.common.approval.domain.ApprovalRequest;
import com.itsm.common.approval.domain.ApprovalRequestStatus;
import com.itsm.common.ticket.TicketType;

import java.util.List;
import java.util.Optional;

/**
 * 승인 인스턴스(헤더) 저장소 포트.
 */
public interface ApprovalRequestRepository {

    ApprovalRequest save(ApprovalRequest request);

    Optional<ApprovalRequest> findById(Long id);

    /** 티켓의 가장 최근 인스턴스(게이트 재확인용, 과거 이력 중 최신 1건). */
    Optional<ApprovalRequest> findTopByTicketTypeAndTicketIdOrderByIdDesc(TicketType ticketType, Long ticketId);

    /** 승인 대기함(scope=mine) 후보: 상태·도메인 필터(도메인은 approvalProcessId 경유 서브쿼리로 필터). */
    List<ApprovalRequest> findByStatusAndDomain(ApprovalRequestStatus status, String domain);
}
