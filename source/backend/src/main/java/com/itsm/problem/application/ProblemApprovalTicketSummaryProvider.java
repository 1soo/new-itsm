package com.itsm.problem.application;

import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.approval.application.ApprovalTicketSummaryProvider;
import com.itsm.common.approval.application.TicketSummary;
import com.itsm.common.ticket.TicketType;
import com.itsm.problem.domain.Problem;
import com.itsm.problem.domain.repository.ProblemRepository;
import org.springframework.stereotype.Component;

/**
 * 공용 승인 대기함·상세 조회(API-COM-003/004)가 PROBLEM 티켓의 요약을 노출하기 위한 어댑터.
 */
@Component
public class ProblemApprovalTicketSummaryProvider implements ApprovalTicketSummaryProvider {

    private final ProblemRepository problemRepository;
    private final AppUserRepository appUserRepository;

    public ProblemApprovalTicketSummaryProvider(ProblemRepository problemRepository,
                                                AppUserRepository appUserRepository) {
        this.problemRepository = problemRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public TicketType supportedType() {
        return TicketType.PROBLEM;
    }

    @Override
    public TicketSummary summaryOf(Long ticketId) {
        Problem problem = problemRepository.findById(ticketId).orElse(null);
        if (problem == null) {
            return null;
        }
        String requesterName = appUserRepository.findByEmail(problem.getCreatedBy())
                .map(AppUser::getName).orElse(null);
        return new TicketSummary(problem.getTicketKey(), problem.getSummary(), requesterName);
    }
}
