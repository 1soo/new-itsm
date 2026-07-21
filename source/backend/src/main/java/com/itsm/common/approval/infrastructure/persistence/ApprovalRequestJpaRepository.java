package com.itsm.common.approval.infrastructure.persistence;

import com.itsm.common.approval.domain.ApprovalRequest;
import com.itsm.common.approval.domain.ApprovalRequestStatus;
import com.itsm.common.approval.domain.repository.ApprovalRequestRepository;
import com.itsm.common.ticket.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ApprovalRequestRepository 포트의 Spring Data JPA 구현.
 */
public interface ApprovalRequestJpaRepository extends JpaRepository<ApprovalRequest, Long>, ApprovalRequestRepository {

    @Override
    Optional<ApprovalRequest> findTopByTicketTypeAndTicketIdOrderByIdDesc(TicketType ticketType, Long ticketId);

    @Override
    Optional<ApprovalRequest> findTopByTicketTypeAndTicketIdAndTargetStateOrderByIdDesc(
            TicketType ticketType, Long ticketId, String targetState);

    @Override
    List<ApprovalRequest> findByTicketTypeAndTicketIdInAndStatusOrderByIdDesc(
            TicketType ticketType, List<Long> ticketIds, ApprovalRequestStatus status);

    @Override
    @Query("""
            select ar from ApprovalRequest ar
            where ar.isDeleted = false and ar.status = :status
              and (:domain is null or ar.approvalProcessId in
                    (select p.id from ApprovalProcess p where p.domain = cast(:domain as string)))
            order by ar.createdAt desc
            """)
    List<ApprovalRequest> findByStatusAndDomain(@Param("status") ApprovalRequestStatus status,
                                                 @Param("domain") String domain);
}
