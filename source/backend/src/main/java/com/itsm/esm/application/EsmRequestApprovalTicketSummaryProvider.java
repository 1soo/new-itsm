package com.itsm.esm.application;

import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.approval.application.ApprovalTicketSummaryProvider;
import com.itsm.common.approval.application.TicketSummary;
import com.itsm.common.ticket.TicketType;
import com.itsm.esm.domain.EsmCatalogItem;
import com.itsm.esm.domain.EsmRequest;
import com.itsm.esm.domain.repository.EsmCatalogItemRepository;
import com.itsm.esm.domain.repository.EsmRequestRepository;
import org.springframework.stereotype.Component;

/**
 * 공용 승인 대기함·상세 조회(API-COM-003/004)가 ESM_REQUEST 티켓의 요약을 노출하기 위한 어댑터.
 */
@Component
public class EsmRequestApprovalTicketSummaryProvider implements ApprovalTicketSummaryProvider {

    private final EsmRequestRepository requestRepository;
    private final EsmCatalogItemRepository catalogItemRepository;
    private final AppUserRepository appUserRepository;

    public EsmRequestApprovalTicketSummaryProvider(EsmRequestRepository requestRepository,
                                                    EsmCatalogItemRepository catalogItemRepository,
                                                    AppUserRepository appUserRepository) {
        this.requestRepository = requestRepository;
        this.catalogItemRepository = catalogItemRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public TicketType supportedType() {
        return TicketType.ESM_REQUEST;
    }

    @Override
    public TicketSummary summaryOf(Long ticketId) {
        EsmRequest request = requestRepository.findById(ticketId).orElse(null);
        if (request == null) {
            return null;
        }
        String title = catalogItemRepository.findById(request.getCatalogItemId())
                .map(EsmCatalogItem::getName).orElse(null);
        String requesterName = appUserRepository.findById(request.getRequesterId())
                .map(AppUser::getName).orElse(null);
        return new TicketSummary(request.getTicketKey(), title, requesterName);
    }
}
