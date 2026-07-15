# CLAUDE.md

서비스 요청(srm) 도메인 애플리케이션 계층 요청·응답 DTO(record/enum). 승인 결정·대기 관련 DTO(ApprovalDecision*, PendingApprovalResponse)는 2026-07-11 승인 프로세스 커스텀 기능으로 `common.approval.application.dto`(전 도메인 공용)로 대체·제거됨.

## 파일
- `CreateRequestRequest.java` — 서비스 요청 생성(catalogItemId, 동적 formValues)
- `RequestCreatedResponse.java` — 요청 생성 응답
- `RequestSummaryResponse.java` — 요청 목록 요약 응답(SLA 상태 포함). `assigneeId`(배정 담당자 id, 미배정 null) 포함 — 요청 큐(SCR-SRM-004) 배정 버튼 노출조건(본인배정 시 숨김) 판정용(2026-07-15, TC-SRM-009 결함 수정)
- `RequestDetailResponse.java` — 요청 상세 응답(양식값·승인(approvalRequestId/status)·SLA·연계지식·연계자산(REQ-ITAM-006)·댓글·타임라인·허용 상태전이)
- `StatusTransitionRequest.java` — 상태 전이 요청(targetStatus, note)
- `StatusResponse.java` — 상태 응답(id, status)
- `AssignRequest.java` — 담당자 배정 요청(assigneeId, 미지정 시 본인)
- `AssigneeCandidateResponse.java` — 담당자 배정 후보 응답(id, name — API-SRM-017, 2026-07-15)
- `CommentCreateRequest.java` — 댓글 작성 요청(body)
- `CommentResponse.java` — 댓글 응답
- `CsatRequest.java` — 만족도(CSAT) 제출 요청(score 1~5, comment)
- `CsatResponse.java` — CSAT 응답
- `CreateCatalogItemRequest.java` — 카탈로그 항목 생성 요청(SLA·담당자 역할 assigneeRoleId(선택, 2026-07-15)·동적 양식. 승인 필드 제거됨)
- `UpdateCatalogItemRequest.java` — 카탈로그 항목 수정 요청(양식 전체 교체 가능, assigneeRoleId 선택)
- `CatalogItemSummaryResponse.java` — 카탈로그 항목 요약 응답
- `CatalogItemDetailResponse.java` — 카탈로그 항목 상세 응답(양식 스키마·assigneeRoleId/assigneeRoleName 포함)
- `FormFieldDto.java` — 동적 양식 필드 정의(key, label, type, required, options)
- `QueueResponse.java` — 큐 응답(기본 큐 여부·미종료 건수)
- `MetricsResponse.java` — SRM 지표 응답(CSAT 평균·응답/해결 평균·SLA 준수율)
- `KnowledgeSuggestionResponse.java` — 지식 추천 응답(articleId, title, score)
