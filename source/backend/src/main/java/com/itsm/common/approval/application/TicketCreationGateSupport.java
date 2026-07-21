package com.itsm.common.approval.application;

import com.itsm.common.ticket.TicketType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 생성 시점(최초 상태) 승인 게이트 지원(common.md 0절, 2026-07-22 상태별 승인자 지정 확장). 마스터 레코드는
 * 제출 즉시 생성·커밋되어야 하므로(승인 전까지 "임시/승인대기"로만 표시), 엔티티 생성+저장을 REQUIRES_NEW로
 * 먼저 커밋한 뒤 커밋 후 별도 트랜잭션에서 {@link ApprovalGateService#checkGate}를 호출한다 — 게이트가 막혀도
 * (409) 방금 커밋된 엔티티는 롤백되지 않는다.
 */
@Component
public class TicketCreationGateSupport {

    private final TransactionTemplate requiresNewTransactionTemplate;
    private final ApprovalGateService approvalGateService;

    public TicketCreationGateSupport(PlatformTransactionManager transactionManager,
                                      ApprovalGateService approvalGateService) {
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
        this.approvalGateService = approvalGateService;
    }

    /**
     * {@code createAndSave}를 REQUIRES_NEW로 커밋한 뒤, 생성된 엔티티에서 뽑아낸 ticketId로 게이트를
     * 체크한다. 게이트 통과 시 그대로, 막히면 {@link com.itsm.common.exception.BusinessException}(409)을
     * 던지되 엔티티는 이미 커밋된 상태로 남는다.
     */
    public <T> T createThenGate(Supplier<T> createAndSave, Function<T, Long> ticketIdOf,
                                 String domain, String requestSubtypeKey, Long requesterId,
                                 TicketType ticketType, String targetState) {
        T created = requiresNewTransactionTemplate.execute(status -> createAndSave.get());
        approvalGateService.checkGate(domain, requestSubtypeKey, requesterId, ticketType, ticketIdOf.apply(created), targetState);
        return created;
    }
}
