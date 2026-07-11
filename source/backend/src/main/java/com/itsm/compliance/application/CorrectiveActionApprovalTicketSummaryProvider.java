package com.itsm.compliance.application;

import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.approval.application.ApprovalTicketSummaryProvider;
import com.itsm.common.approval.application.TicketSummary;
import com.itsm.common.ticket.TicketType;
import com.itsm.compliance.domain.CorrectiveAction;
import com.itsm.compliance.domain.repository.CorrectiveActionRepository;
import org.springframework.stereotype.Component;

/**
 * 공용 승인 대기함·상세 조회(API-COM-003/004)가 CORRECTIVE_ACTION(시정조치) 티켓의 요약을 노출하기 위한 어댑터.
 * 시정조치는 요구사항(COMPLIANCE_REQUIREMENT)에 속한 개별 항목이라 자연키(ticketKey)가 없어 "CA-{actionId}" 형태로 합성한다.
 */
@Component
public class CorrectiveActionApprovalTicketSummaryProvider implements ApprovalTicketSummaryProvider {

    private final CorrectiveActionRepository correctiveActionRepository;
    private final AppUserRepository appUserRepository;

    public CorrectiveActionApprovalTicketSummaryProvider(CorrectiveActionRepository correctiveActionRepository,
                                                          AppUserRepository appUserRepository) {
        this.correctiveActionRepository = correctiveActionRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public TicketType supportedType() {
        return TicketType.CORRECTIVE_ACTION;
    }

    @Override
    public TicketSummary summaryOf(Long ticketId) {
        CorrectiveAction action = correctiveActionRepository.findById(ticketId).orElse(null);
        if (action == null) {
            return null;
        }
        String requesterName = appUserRepository.findByEmail(action.getCreatedBy())
                .map(AppUser::getName).orElse(null);
        return new TicketSummary("CA-" + ticketId, action.getDescription(), requesterName);
    }
}
