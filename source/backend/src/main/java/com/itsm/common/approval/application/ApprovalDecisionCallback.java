package com.itsm.common.approval.application;

import com.itsm.common.ticket.TicketType;

/**
 * 승인 인스턴스가 최종 확정(APPROVED/REJECTED)되는 시점에 도메인이 반응하기 위한 확장 포인트.
 * SRM/CHANGE처럼 사용자가 전이를 재시도해 통과시키는 도메인은 콜백이 필요 없지만(재시도 시점에 게이트가
 * 다시 판정하므로), KNOWLEDGE처럼 결정 즉시 도메인 상태가 자동으로 바뀌어야 하는 경우 이 인터페이스를
 * 구현한 빈을 등록한다({@link ApprovalTicketSummaryProvider}와 동일한 SPI 패턴).
 */
public interface ApprovalDecisionCallback {

    TicketType supportedType();

    /** 인스턴스가 APPROVED로 최종 확정됐을 때 호출된다. */
    void onApproved(Long ticketId);

    /** 인스턴스가 REJECTED로 최종 확정됐을 때 호출된다(반려 사유 포함). */
    void onRejected(Long ticketId, String reason);
}
