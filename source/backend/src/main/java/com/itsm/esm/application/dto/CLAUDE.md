# CLAUDE.md

esm 도메인 애플리케이션 계층의 요청·응답 DTO(record).

## 파일
- `ChecklistTemplateTaskDto.java` — 체크리스트 하위 작업 템플릿(department, taskDescription)
- `CreateCatalogItemRequest.java` — 카탈로그 항목 생성 요청(담당 부서·체크리스트 템플릿·`formSchema`(SRM과 완전히 동일한 자체 8×n 그리드 스키마 `Map<String,Object>`, `{components,labels}`, 2026-07-19 유지보수 요청 — 레거시 EAV `FormFieldDto` 폐기))
- `UpdateCatalogItemRequest.java` — 카탈로그 항목 수정 요청(부분 갱신, 제공 시 `formSchema`/템플릿 전체 교체)
- `CatalogItemSummaryResponse.java` — 카탈로그 목록 요약 응답
- `CatalogItemDetailResponse.java` — 카탈로그 상세 응답(체크리스트 템플릿·`formSchema`(자체 8×n 그리드 스키마) 포함)
- `CreateRequestRequest.java` — 부서 요청 제출(catalogItemId, formValues, targetUserName)
- `RequestCreatedResponse.java` — 요청 생성 응답(checklistId 포함)
- `RequestSummaryResponse.java` — 요청 목록 요약 응답(pendingApprovalTargetState(진행 중 승인 인스턴스 targetState, 2026-07-22 신규) 포함)
- `RequestDetailResponse.java` — 요청 상세 응답(양식값·코멘트·타임라인, `approval`(ApprovalInfo)에 targetState 포함(2026-07-22 신규))
- `StatusTransitionRequest.java` — 부서 요청 상태 전이 요청(targetStatus, note)
- `StatusResponse.java` — 상태 응답(id, status)
- `CommentCreateRequest.java` — 코멘트 작성 요청(body)
- `CommentResponse.java` — 코멘트 응답
- `CreateHrCaseRequest.java` — HR 케이스 접수 요청(subjectUserName, title, description)
- `HrCaseCreatedResponse.java` — HR 케이스 접수 응답
- `HrCaseSummaryResponse.java` — HR 케이스 목록 요약 응답
- `HrCaseDetailResponse.java` — HR 케이스 상세 응답(상태 변경 이력 포함)
- `HrCaseStatusTransitionRequest.java` — HR 케이스 상태 전이 요청
- `ChecklistDetailResponse.java` — 체크리스트 상세 응답(하위 작업 목록·연계 자산 포함)
- `MyChecklistTaskResponse.java` — 내 하위 작업 목록 항목 응답
- `ChecklistTaskStatusRequest.java` — 하위 작업 상태 변경 요청(status=DONE)
- `ChecklistTaskStatusResponse.java` — 하위 작업 상태 변경 응답(전체 완료 시 체크리스트 상태 반영)
- `EsmMetricsResponse.java` — ESM 지표 응답(요청건수·평균처리시간·온보딩/오프보딩 완료율)
