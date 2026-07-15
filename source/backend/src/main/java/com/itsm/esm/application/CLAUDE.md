# CLAUDE.md

esm 도메인 애플리케이션 서비스 계층.

## 파일
- `EsmCatalogService.java` — 부서 카탈로그 항목 CRUD·동적 양식·체크리스트 템플릿 관리(PROCESS_OWNER 전용 생성/수정, IT 부서 거부)
- `EsmRequestService.java` — 부서 요청 제출(체크리스트 자동생성·오프보딩 자산회수 하위작업 자동추가)·조회·상태전이·코멘트 유스케이스(DEPT_COORDINATOR 소속 부서 검증). Stage 6(2026-07-12)에서 IN_PROGRESS→COMPLETED 전이 시 공용 승인 게이트(`ApprovalGateService.checkGate(domain=ESM, requestSubtypeKey=null, requesterId, ...)`) 연동(HR 케이스·체크리스트 하위 작업은 게이트 대상 아님). 요청자는 `EsmRequest.requesterId`(이미 AppUser.id로 저장돼 이메일 역조회 불필요, 다른 도메인 created_by 패턴과 다름) 그대로 사용. 상세 조회(API-ESM-007)의 `approval` 필드도 동일 노출. `assertCanView()`(요청자 본인/DEPT_COORDINATOR 소속 부서 일치 — 댓글 작성(API-ESM-009) 등에서 사용)와 `assertCanViewDetail()`(2026-07-15 신규, 상세조회 API-ESM-007 전용 — 위 조건에 더해 `ApprovalGateService.canApproverView(ESM, null, request.getRequesterId())` 매칭 시에도 허용)을 분리(코드리뷰 지적 반영 — 동적 승인자 조회 권한이 댓글 작성까지 확대되지 않도록)
- `EsmRequestApprovalTicketSummaryProvider.java` — 공용 승인 대기함·상세(API-COM-003/004)가 노출할 ESM_REQUEST 티켓 요약(ticketKey·카탈로그항목명·요청자명) 어댑터(`ApprovalTicketSummaryProvider` 구현)
- `EsmHrCaseService.java` — HR 케이스 접수·조회·상태전이(접수→기록→조사→해결 순차) 유스케이스(HR_CASE_MANAGER 전용)
- `EsmChecklistService.java` — 체크리스트 상세 조회·내 하위 작업 목록·하위 작업 완료 처리(전체 완료 시 체크리스트 자동완료) 유스케이스
- `EsmMetricsService.java` — ESM 지표(요청 건수·평균 처리시간·온보딩/오프보딩 완료율) 집계

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
