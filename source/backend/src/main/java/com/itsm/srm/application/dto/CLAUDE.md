# CLAUDE.md

서비스 요청(srm) 도메인 애플리케이션 계층 요청·응답 DTO(record/enum). 승인 결정·대기 관련 DTO(ApprovalDecision*, PendingApprovalResponse)는 2026-07-11 승인 프로세스 커스텀 기능으로 `common.approval.application.dto`(전 도메인 공용)로 대체·제거됨.

## 파일
- `CreateRequestRequest.java` — 서비스 요청 생성(catalogItemId, 동적 formValues)
- `RequestCreatedResponse.java` — 요청 생성 응답
- `RequestSummaryResponse.java` — 요청 목록 요약 응답(SLA 상태 포함). `assigneeId`(배정 담당자 id, 미배정 null) 포함 — 요청 큐(SCR-SRM-004) 배정 버튼 노출조건(본인배정 시 숨김) 판정용(2026-07-15, TC-SRM-009 결함 수정). `pendingApprovalTargetState`(진행 중 승인 인스턴스 targetState, 2026-07-22 신규) 포함
- `RequestDetailResponse.java` — 요청 상세 응답(양식값·승인(approvalRequestId/status/targetState, 2026-07-22 targetState 추가)·SLA·연계지식·연계자산(REQ-ITAM-006)·댓글·타임라인·허용 상태전이). `queue` 필드는 2026-07-18 유지보수 요청(요청 큐 폐지)으로 제거됨
- `StatusTransitionRequest.java` — 상태 전이 요청(targetStatus, note)
- `StatusResponse.java` — 상태 응답(id, status)
- `AssignRequest.java` — 담당자 배정 요청(assigneeId, 미지정 시 본인)
- `AssigneeCandidateResponse.java` — 담당자 배정 후보 응답(id, name — API-SRM-017, 2026-07-15)
- `CommentCreateRequest.java` — 댓글 작성 요청(body)
- `CommentResponse.java` — 댓글 응답
- `CsatRequest.java` — 만족도(CSAT) 제출 요청(score 1~5, comment)
- `CsatResponse.java` — CSAT 응답
- `CreateCatalogItemRequest.java` — 카탈로그 항목 생성 요청(SLA·담당자 역할 assigneeRoleId(선택, 2026-07-15)·categoryId(선택, 2026-07-16)·`formSchema`(자체 8×n 그리드 스키마 `Map<String,Object>`, `{components}`, 2026-07-18 유지보수 요청 — form.io Form JSON 계약 완전 폐기). 승인 필드 제거됨. `queueId` 필드는 2026-07-18 유지보수 요청(요청 큐 폐지)으로 제거됨)
- `UpdateCatalogItemRequest.java` — 카탈로그 항목 수정 요청(제공 시 `formSchema` 전체 교체, assigneeRoleId·categoryId 선택. `queueId` 필드는 2026-07-18 유지보수 요청으로 제거됨)
- `CatalogItemSummaryResponse.java` — 카탈로그 항목 요약 응답(categoryId/categoryName, 2026-07-16 유지보수 요청 — 기존 category 자유 텍스트 필드 대체)
- `CatalogItemDetailResponse.java` — 카탈로그 항목 상세 응답(categoryId/categoryName·`formSchema`(자체 8×n 그리드 스키마)·assigneeRoleId/assigneeRoleName 포함. `queueId` 필드는 2026-07-18 유지보수 요청으로 제거됨)
- `CategoryResponse.java` — 카탈로그 카테고리 생성/수정 응답(id, name, sortOrder, 2026-07-16 유지보수 요청)
- `CategoryListResponse.java` — 카탈로그 카테고리 목록 항목 응답(id, name, sortOrder, itemCount — 참조 중인 카탈로그 항목 수)
- `CategoryCreateRequest.java` — 카탈로그 카테고리 생성 요청(name 필수, sortOrder 선택)
- `CategoryUpdateRequest.java` — 카탈로그 카테고리 수정 요청(부분 갱신)
- `CategoryCountResponse.java` — 요청 카테고리별 미종료 건수 응답(categoryId/categoryName(null=미분류)·openCount, API-SRM-016, 2026-07-18 유지보수 요청 — 기존 `QueueResponse`/큐 목록·건수 조회 대체)
- `MetricsResponse.java` — SRM 지표 응답(CSAT 평균·응답/해결 평균·SLA 준수율)
- `KnowledgeSuggestionResponse.java` — 지식 추천 응답(articleId, title, score)
