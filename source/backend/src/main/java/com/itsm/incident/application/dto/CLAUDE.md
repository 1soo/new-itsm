# CLAUDE.md

인시던트(incident) 도메인 애플리케이션 계층의 요청·응답 DTO(record).

## 파일
- `CreateIncidentRequest.java` — 인시던트 생성 요청(summary, severity, 영향 서비스/제품)
- `IncidentCreatedResponse.java` — 생성 응답(id, ticketKey, status)
- `IncidentSummaryResponse.java` — 목록 요약 응답(pendingApprovalTargetState(진행 중 승인 인스턴스 targetState, 2026-07-22 신규) 포함)
- `IncidentDetailResponse.java` — 상세 응답(대응자·메트릭·연계·타임라인·허용 상태전이 포함, `approval`(ApprovalInfo)에 targetState 포함(2026-07-22 신규))
- `StatusTransitionRequest.java` — 상태 전이 요청(targetStatus, note)
- `StatusResponse.java` — 상태 응답(id, status)
- `SeverityChangeRequest.java` — 심각도/우선순위 변경 요청
- `SeverityChangeResponse.java` — 심각도/우선순위 변경 응답
- `AssignRoleRequest.java` — 대응 역할 배정 요청(userId, ResponseRole)
- `ResponderDto.java` — 대응자 정보(userId, name, role)
- `EscalateRequest.java` — 에스컬레이션 요청(대상자, 유형, 사유)
- `EscalateResponse.java` — 에스컬레이션 응답
- `ResolveRequest.java` — 해결 처리 요청(영향 시작/감지/종료 시각, 해결 노트)
- `ResolveResponse.java` — 해결 응답(메트릭 포함)
- `IncidentMetrics.java` — 개별 인시던트 지표(MTTD/MTTA/MTTR 분)
- `IncidentMetricsResponse.java` — 집계 지표 응답(건수·심각도 분포·평균 MTTR)
- `TimelineUpdateRequest.java` — 타임라인 업데이트 요청(message, visibility)
- `TimelineUpdateResponse.java` — 타임라인 업데이트 응답
- `LinkProblemRequest.java` — 문제 연계 요청(기존 problemId 또는 신규 생성)
- `LinkResponse.java` — 연계 응답(incidentId, problemId)
- `PostmortemRequest.java` — 포스트모템 작성 요청(요약·타임라인·5Why·근본원인·액션아이템)
- `PostmortemResponse.java` — 포스트모템 응답
- `ActionItemDto.java` — 포스트모템 액션 아이템(description, owner, dueDate, status)
