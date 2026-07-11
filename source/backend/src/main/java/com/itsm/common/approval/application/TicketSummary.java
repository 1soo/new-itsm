package com.itsm.common.approval.application;

/**
 * 승인 대기함·상세 조회가 노출하는 티켓 요약(ticketKey·제목·요청자명).
 */
public record TicketSummary(String ticketKey, String title, String requesterName) {
}
