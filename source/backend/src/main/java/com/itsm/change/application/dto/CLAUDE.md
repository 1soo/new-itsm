# CLAUDE.md

change 도메인 애플리케이션 계층의 요청·응답 DTO(record/enum). 승인 관련 DTO(ChangeApprovalDecision/Request/Response, PendingChangeApprovalResponse)는 2026-07-11 승인 프로세스 커스텀 기능으로 `common.approval.application.dto`(전 도메인 공용)로 대체·제거됨.

## 파일
- `CreateChangeRequest.java` — RFC 생성 요청(요약·유형·위험도·구현/롤백 계획·영향시스템·일정·템플릿)
- `ChangeCreatedResponse.java` — 생성 응답(id, ticketKey, status, type)
- `ChangeSummaryResponse.java` — 목록 요약 응답(pendingApprovalTargetState(진행 중 승인 인스턴스 targetState, 2026-07-22 신규) 포함)
- `ChangeDetailResponse.java` — 상세 응답(구현결과·승인(approvalRequestId/status/targetState, common.approval 조회, 2026-07-22 targetState 추가)·연계·허용 상태전이 포함)
- `StatusTransitionRequest.java` — 상태 전이 요청(targetStatus, note)
- `StatusResponse.java` — 상태 응답(id, status)
- `ClassificationRequest.java` — 유형·위험 변경 요청
- `ClassificationResponse.java` — 유형·위험 응답
- `ResultRequest.java` — 구현 결과 기록 요청(outcome, rolledBack, note)
- `ResultResponse.java` — 구현 결과 응답
- `LinkRequest.java` — 인시던트/문제 연계 요청(targetType, targetId)
- `LinkResponse.java` — 연계 응답(changeId, targetType, targetId)
- `ScheduleItemResponse.java` — 변경 일정 항목 응답
- `ChangeTemplateResponse.java` — 표준 변경 템플릿 응답
- `ChangeMetricsResponse.java` — 변경 지표 응답(successRate/failureRate/emergencyRate/total)
