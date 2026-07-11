package com.itsm.change.application;

import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.change.domain.ChangeRequest;
import com.itsm.change.domain.repository.ChangeRequestRepository;
import com.itsm.common.approval.application.ApprovalTicketSummaryProvider;
import com.itsm.common.approval.application.TicketSummary;
import com.itsm.common.ticket.TicketType;
import org.springframework.stereotype.Component;

/**
 * 공용 승인 대기함·상세 조회(API-COM-003/004)가 CHANGE 티켓의 요약을 노출하기 위한 어댑터.
 */
@Component
public class ChangeApprovalTicketSummaryProvider implements ApprovalTicketSummaryProvider {

    private final ChangeRequestRepository changeRequestRepository;
    private final AppUserRepository appUserRepository;

    public ChangeApprovalTicketSummaryProvider(ChangeRequestRepository changeRequestRepository,
                                               AppUserRepository appUserRepository) {
        this.changeRequestRepository = changeRequestRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public TicketType supportedType() {
        return TicketType.CHANGE;
    }

    @Override
    public TicketSummary summaryOf(Long ticketId) {
        ChangeRequest change = changeRequestRepository.findById(ticketId).orElse(null);
        if (change == null) {
            return null;
        }
        String requesterName = appUserRepository.findByEmail(change.getCreatedBy())
                .map(AppUser::getName).orElse(change.getCreatedBy());
        return new TicketSummary(change.getTicketKey(), change.getSummary(), requesterName);
    }
}
