package com.itsm.common.ticket.repository;

import com.itsm.common.ticket.Approval;
import com.itsm.common.ticket.ApprovalStatus;
import com.itsm.common.ticket.TicketType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 승인 저장소 포트.
 */
public interface ApprovalRepository {

    Approval save(Approval approval);

    Optional<Approval> findById(Long id);

    Optional<Approval> findByTicketTypeAndTicketId(TicketType ticketType, Long ticketId);

    /** 역할 기반 승인 대기 목록: 대상 유형·상태이면서 approver_role이 요청자 보유 역할에 포함되는 건. */
    List<Approval> findByTicketTypeAndStatusAndApproverRoleIn(
            TicketType ticketType, ApprovalStatus status, Collection<String> approverRoles);
}
