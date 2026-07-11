package com.itsm.incident.application;

import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.approval.application.ApprovalTicketSummaryProvider;
import com.itsm.common.approval.application.TicketSummary;
import com.itsm.common.ticket.TicketType;
import com.itsm.incident.domain.Incident;
import com.itsm.incident.domain.repository.IncidentRepository;
import org.springframework.stereotype.Component;

/**
 * 공용 승인 대기함·상세 조회(API-COM-003/004)가 INCIDENT 티켓의 요약을 노출하기 위한 어댑터.
 */
@Component
public class IncidentApprovalTicketSummaryProvider implements ApprovalTicketSummaryProvider {

    private final IncidentRepository incidentRepository;
    private final AppUserRepository appUserRepository;

    public IncidentApprovalTicketSummaryProvider(IncidentRepository incidentRepository,
                                                 AppUserRepository appUserRepository) {
        this.incidentRepository = incidentRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public TicketType supportedType() {
        return TicketType.INCIDENT;
    }

    @Override
    public TicketSummary summaryOf(Long ticketId) {
        Incident incident = incidentRepository.findById(ticketId).orElse(null);
        if (incident == null) {
            return null;
        }
        String requesterName = appUserRepository.findByEmail(incident.getCreatedBy())
                .map(AppUser::getName).orElse(null);
        return new TicketSummary(incident.getTicketKey(), incident.getSummary(), requesterName);
    }
}
