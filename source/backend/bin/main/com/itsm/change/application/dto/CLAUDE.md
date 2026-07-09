# CLAUDE.md

change 도메인 애플리케이션 계층의 요청·응답 DTO(record/enum).

## 파일
- `CreateChangeRequest.java` — RFC 생성 요청(요약·유형·위험도·구현/롤백 계획·영향시스템·일정·템플릿)
- `ChangeCreatedResponse.java` — 생성 응답(id, ticketKey, status, type)
- `ChangeSummaryResponse.java` — 목록 요약 응답
- `ChangeDetailResponse.java` — 상세 응답(구현결과·승인이력·연계·허용 상태전이 포함)
- `StatusTransitionRequest.java` — 상태 전이 요청(targetStatus, note)
- `StatusResponse.java` — 상태 응답(id, status)
- `ClassificationRequest.java` — 유형·위험 변경 요청
- `ClassificationResponse.java` — 유형·위험·승인경로 응답
- `ChangeApprovalDecision.java` — 승인 결정 enum(APPROVE, REJECT)
- `ChangeApprovalRequest.java` — 승인/반려 요청(decision, opinion)
- `ChangeApprovalResponse.java` — 승인/반려 응답(id, status)
- `PendingChangeApprovalResponse.java` — 승인 대기 목록 응답(changeId, ticketKey, type, risk, requester)
- `ResultRequest.java` — 구현 결과 기록 요청(outcome, rolledBack, note)
- `ResultResponse.java` — 구현 결과 응답
- `LinkRequest.java` — 인시던트/문제 연계 요청(targetType, targetId)
- `LinkResponse.java` — 연계 응답(changeId, targetType, targetId)
- `ScheduleItemResponse.java` — 변경 일정 항목 응답
- `ChangeTemplateResponse.java` — 표준 변경 템플릿 응답
- `ChangeMetricsResponse.java` — 변경 지표 응답(successRate/failureRate/emergencyRate/total)
