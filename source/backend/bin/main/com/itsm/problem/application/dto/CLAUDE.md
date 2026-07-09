# CLAUDE.md

문제(problem) 도메인 애플리케이션 계층의 요청·응답 DTO(record).

## 파일
- `CreateProblemRequest.java` — 문제 생성 요청(요약·출처·영향도·긴급도·구성요소)
- `ProblemCreatedResponse.java` — 생성 응답(id, ticketKey, status, priority)
- `ProblemSummaryResponse.java` — 목록 요약 응답
- `ProblemDetailResponse.java` — 상세 응답(RCA·워크어라운드·연계(인시던트/변경/자산, REQ-ITAM-006)·후속조치·허용 상태전이 포함)
- `StatusTransitionRequest.java` — 상태 전이 요청(targetStatus, note)
- `StatusResponse.java` — 상태 응답(id, status)
- `RcaRequest.java` — 근본원인 분석 요청(rootCause, fiveWhys, category)
- `RcaResponse.java` — RCA 응답
- `WorkaroundRequest.java` — 워크어라운드 등록 요청(content, linkedArticleId)
- `WorkaroundResponse.java` — 워크어라운드 응답
- `ActionCreateRequest.java` — 후속 조치 생성 요청(description, owner, dueDate)
- `ActionStatusRequest.java` — 후속 조치 상태 변경 요청(ActionStatus)
- `ActionResponse.java` — 후속 조치 응답(id, status)
- `LinkRequest.java` — 연계 요청(대상 유형 INCIDENT|CHANGE, 기존 id 또는 신규 생성)
- `LinkResponse.java` — 연계 응답(problemId, targetType, targetId)
- `CloseRequest.java` — 종료 요청(force: 미해결 조치 경고 무시)
- `CloseResponse.java` — 종료 응답(경고 메시지 포함)
- `KnownErrorCreateRequest.java` — 기지 오류(Known Error) 생성 요청(title, rootCause, workaround)
- `KnownErrorCreatedResponse.java` — 기지 오류 생성 응답
- `KnownErrorSearchResponse.java` — 기지 오류 검색 응답
