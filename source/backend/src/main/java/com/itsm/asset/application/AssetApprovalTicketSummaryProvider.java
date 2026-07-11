package com.itsm.asset.application;

import com.itsm.asset.domain.Asset;
import com.itsm.asset.domain.repository.AssetRepository;
import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.approval.application.ApprovalTicketSummaryProvider;
import com.itsm.common.approval.application.TicketSummary;
import com.itsm.common.ticket.TicketType;
import org.springframework.stereotype.Component;

/**
 * 공용 승인 대기함·상세 조회(API-COM-003/004)가 ASSET 티켓의 요약을 노출하기 위한 어댑터.
 */
@Component
public class AssetApprovalTicketSummaryProvider implements ApprovalTicketSummaryProvider {

    private final AssetRepository assetRepository;
    private final AppUserRepository appUserRepository;

    public AssetApprovalTicketSummaryProvider(AssetRepository assetRepository,
                                              AppUserRepository appUserRepository) {
        this.assetRepository = assetRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public TicketType supportedType() {
        return TicketType.ASSET;
    }

    @Override
    public TicketSummary summaryOf(Long ticketId) {
        Asset asset = assetRepository.findById(ticketId).orElse(null);
        if (asset == null) {
            return null;
        }
        String requesterName = appUserRepository.findByEmail(asset.getCreatedBy())
                .map(AppUser::getName).orElse(null);
        return new TicketSummary(asset.getAssetKey(), asset.getName(), requesterName);
    }
}
