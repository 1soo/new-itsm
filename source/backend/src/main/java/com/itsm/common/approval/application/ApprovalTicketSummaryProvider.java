package com.itsm.common.approval.application;

import com.itsm.common.ticket.TicketType;

/**
 * 승인 대기함·상세 조회(API-COM-003/004)가 티켓 요약 정보(ticketKey·제목·요청자)를 노출하기 위한 확장 포인트.
 * 각 도메인이 자신의 {@link TicketType}에 대한 구현 빈을 등록한다(common 모듈은 개별 도메인 저장소에 의존하지 않는다).
 */
public interface ApprovalTicketSummaryProvider {

    TicketType supportedType();

    /** 대상 티켓이 이미 삭제·존재하지 않으면 null을 반환할 수 있다(호출측이 null 필드로 노출). */
    TicketSummary summaryOf(Long ticketId);
}
