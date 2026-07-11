package com.itsm.srm.application;

import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.approval.application.ApprovalTicketSummaryProvider;
import com.itsm.common.approval.application.TicketSummary;
import com.itsm.common.ticket.TicketType;
import com.itsm.srm.domain.ServiceCatalogItem;
import com.itsm.srm.domain.ServiceRequest;
import com.itsm.srm.domain.repository.ServiceCatalogItemRepository;
import com.itsm.srm.domain.repository.ServiceRequestRepository;
import org.springframework.stereotype.Component;

/**
 * 공용 승인 대기함·상세 조회(API-COM-003/004)가 서비스 요청(SERVICE_REQUEST) 티켓의 요약을 노출하기 위한 어댑터.
 */
@Component
public class SrmApprovalTicketSummaryProvider implements ApprovalTicketSummaryProvider {

    private final ServiceRequestRepository requestRepository;
    private final ServiceCatalogItemRepository catalogItemRepository;
    private final AppUserRepository appUserRepository;

    public SrmApprovalTicketSummaryProvider(ServiceRequestRepository requestRepository,
                                            ServiceCatalogItemRepository catalogItemRepository,
                                            AppUserRepository appUserRepository) {
        this.requestRepository = requestRepository;
        this.catalogItemRepository = catalogItemRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public TicketType supportedType() {
        return TicketType.SERVICE_REQUEST;
    }

    @Override
    public TicketSummary summaryOf(Long ticketId) {
        ServiceRequest sr = requestRepository.findById(ticketId).orElse(null);
        if (sr == null) {
            return null;
        }
        String catalogName = catalogItemRepository.findById(sr.getCatalogItemId())
                .map(ServiceCatalogItem::getName).orElse(null);
        String requesterName = appUserRepository.findById(sr.getRequesterId())
                .map(AppUser::getName).orElse(null);
        return new TicketSummary(sr.getTicketKey(), catalogName, requesterName);
    }
}
