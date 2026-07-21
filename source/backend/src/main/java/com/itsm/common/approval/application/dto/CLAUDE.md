# CLAUDE.md

승인 엔진 애플리케이션 계층 요청·응답 DTO(record).

## 파일
- `ApprovalInboxItemResponse.java` — 대기함 목록 항목(API-COM-003, targetState/targetStateLabel 포함, 2026-07-22 신규)
- `ApprovalDetailResponse.java` — 인스턴스 상세(API-COM-004, 차수별 역할 결정 현황 + targetState/targetStateLabel 포함, 2026-07-22 신규)
- `ApprovalDecisionRequest.java` — 승인/반려 결정 요청(decision, reason)
- `ApprovalDecisionResultResponse.java` — 결정 처리 결과(차수·인스턴스 상태)
- `ApprovalResubmitRequest.java` — 반려 후 재승인요청(API-COM-006) 요청(ticketType, ticketId)
- `ApprovalResubmitResponse.java` — 재승인요청 결과(신규 인스턴스 정보 또는 status="NO_RULE_MATCHED")
