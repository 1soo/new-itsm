# CLAUDE.md

esm 도메인 애플리케이션 서비스 계층.

## 파일
- `EsmCatalogService.java` — 부서 카탈로그 항목 CRUD·동적 양식·체크리스트 템플릿 관리(PROCESS_OWNER 전용 생성/수정, IT 부서 거부). `formSchema`(SRM과 완전히 동일한 자체 8×n 그리드 스키마, `{"components":[],"labels":[]}` 기본값)를 `EsmCatalogItem.formSchema`(JSONB)에 공용 `FormJsonMapper`로 직렬화/역직렬화(2026-07-19 유지보수 요청, 레거시 EAV `EsmCatalogFormField` 폐기)
- `EsmRequestService.java` — 부서 요청 제출(체크리스트 자동생성·오프보딩 자산회수 하위작업 자동추가)·조회·상태전이·코멘트 유스케이스(DEPT_COORDINATOR 소속 부서 검증). Stage 6(2026-07-12)에서 승인 게이트 연동(HR 케이스·체크리스트 하위 작업은 게이트 대상 아님). 2026-07-22 상태별 승인자 지정 확장: `transition()`은 target 가드 없이 무조건 `checkGate(domain=ESM, requestSubtypeKey=null, requesterId, TT, id, targetState=target)` 호출(SUBMITTED→IN_PROGRESS/COMPLETED/REJECTED, IN_PROGRESS→COMPLETED/REJECTED 어떤 전이든 게이트 대상), checkGate의 requesterId는 호출자(`principal.userId()`)로 통일(기존 `esmRequest.getRequesterId()` 고정 필드 참조 폐기). `create()`(API-ESM-005)는 `TicketCreationGateSupport.createThenGate`로 REQUIRES_NEW 분리(체크리스트 생성 포함, 생성시점 targetState="SUBMITTED"). 상세 조회(API-ESM-007)의 `approval`(`ApprovalInfo`)에 `targetState` 포함. 목록(API-ESM-006)은 `pendingApprovalTargetStatesOf` 배치 조회로 `RequestSummaryResponse.pendingApprovalTargetState` 채움(N+1 방지). `assertCanView()`(요청자 본인/DEPT_COORDINATOR 소속 부서 일치 — 댓글 작성(API-ESM-009) 등에서 사용)와 `assertCanViewDetail()`(2026-07-15 신규, 상세조회 API-ESM-007 전용 — 위 조건에 더해 `ApprovalGateService.canApproverView(ESM, null, request.getRequesterId())` 매칭 시에도 허용, 이번 유지보수로 변경 없음 — canApproverView는 티켓의 실제 요청자 기준 유지)을 분리(코드리뷰 지적 반영 — 동적 승인자 조회 권한이 댓글 작성까지 확대되지 않도록). 제출(`create()`) 시 카탈로그 항목의 `formSchema` + 제출된 `formValues`를 공용 `FormSubmissionValidator`(2026-07-19 유지보수 요청, 레거시 필수값 검증 로직·EAV `EsmRequestFormValue` 폐기)로 재검증 후 `EsmRequest.formValues`(JSONB)에 그대로 저장, 상세조회는 저장된 JSONB를 그대로 파싱해 반환
- `EsmRequestApprovalTicketSummaryProvider.java` — 공용 승인 대기함·상세(API-COM-003/004)가 노출할 ESM_REQUEST 티켓 요약(ticketKey·카탈로그항목명·요청자명) 어댑터(`ApprovalTicketSummaryProvider` 구현)
- `EsmHrCaseService.java` — HR 케이스 접수·조회·상태전이(접수→기록→조사→해결 순차) 유스케이스(HR_CASE_MANAGER 전용)
- `EsmChecklistService.java` — 체크리스트 상세 조회·내 하위 작업 목록·하위 작업 완료 처리(전체 완료 시 체크리스트 자동완료) 유스케이스
- `EsmMetricsService.java` — ESM 지표(요청 건수·평균 처리시간·온보딩/오프보딩 완료율) 집계

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
