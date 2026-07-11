package com.itsm.knowledge.application;

import com.itsm.auth.domain.AppUser;
import com.itsm.auth.domain.repository.AppUserRepository;
import com.itsm.common.approval.application.ApprovalTicketSummaryProvider;
import com.itsm.common.approval.application.TicketSummary;
import com.itsm.common.ticket.TicketType;
import com.itsm.knowledge.domain.KnowledgeArticle;
import com.itsm.knowledge.domain.repository.KnowledgeArticleRepository;
import org.springframework.stereotype.Component;

/**
 * 공용 승인 대기함·상세 조회(API-COM-003/004)가 KNOWLEDGE 티켓의 요약을 노출하기 위한 어댑터.
 */
@Component
public class KnowledgeApprovalTicketSummaryProvider implements ApprovalTicketSummaryProvider {

    private final KnowledgeArticleRepository articleRepository;
    private final AppUserRepository appUserRepository;

    public KnowledgeApprovalTicketSummaryProvider(KnowledgeArticleRepository articleRepository,
                                                  AppUserRepository appUserRepository) {
        this.articleRepository = articleRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    public TicketType supportedType() {
        return TicketType.KNOWLEDGE;
    }

    @Override
    public TicketSummary summaryOf(Long ticketId) {
        KnowledgeArticle article = articleRepository.findById(ticketId).orElse(null);
        if (article == null) {
            return null;
        }
        String requesterName = appUserRepository.findById(article.getAuthorId())
                .map(AppUser::getName).orElse(null);
        return new TicketSummary("KM-" + ticketId, article.getTitle(), requesterName);
    }
}
