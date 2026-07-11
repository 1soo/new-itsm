package com.itsm.knowledge.application;

import com.itsm.common.approval.application.ApprovalDecisionCallback;
import com.itsm.common.ticket.TicketType;
import com.itsm.knowledge.domain.ArticleStatus;
import com.itsm.knowledge.domain.KnowledgeArticle;
import com.itsm.knowledge.domain.repository.KnowledgeArticleRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 승인 인스턴스 최종 확정 시 지식 기사를 자동 전환한다(API-KM-006, SCR-KM-003).
 * APPROVED → PUBLISHED, REJECTED → DRAFT(반려 사유는 기사에 저장하지 않고 API-COM-004로 조회).
 */
@Component
public class KnowledgeApprovalDecisionCallback implements ApprovalDecisionCallback {

    private final KnowledgeArticleRepository articleRepository;

    public KnowledgeApprovalDecisionCallback(KnowledgeArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Override
    public TicketType supportedType() {
        return TicketType.KNOWLEDGE;
    }

    @Override
    @Transactional
    public void onApproved(Long ticketId) {
        articleRepository.findById(ticketId).ifPresent(article -> {
            article.publish();
            articleRepository.save(article);
        });
    }

    @Override
    @Transactional
    public void onRejected(Long ticketId, String reason) {
        articleRepository.findById(ticketId).ifPresent(article -> {
            article.changeStatus(ArticleStatus.DRAFT);
            articleRepository.save(article);
        });
    }
}
