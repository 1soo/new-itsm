# 개발 계획 — service-request (서비스 요청 관리, SRM)

> 도메인: service-request (SRM) · 개발 순서 2/7 · 작성: dev-lead · 2026-07-09

## 1. 목표

셀프서비스 포털(카탈로그·동적 양식·지식 추천), 요청 제출/큐/상세(검증·라우팅·승인·이행·종료), SLA·코멘트·타임라인·CSAT, 카탈로그 관리, 지표 대시보드를 구현한다. auth 기반(인증·RBAC·공통 컴포넌트·apiClient)을 재사용하고, **common 교차 테이블(approval/comment/timeline_event)**을 이 단계에서 도입한다.

## 2. 설계 근거 (docs/02_plan)

- 화면: `screen/service-request.md`(SCR-SRM-001~008), 공통 패턴 SCR-COM-007/008 재사용
- API: `api_spec/service-request.md`(API-SRM-001~015, Base `/api/v1`)
- DB: `database/service-request.md`(queue/service_catalog_item/catalog_form_field/service_request/service_request_form_value/csat) + `database/common.md`(approval/comment/timeline_event)
- 역할: `security/authorization/`(END_USER, SERVICE_DESK_AGENT, APPROVER, PROCESS_OWNER 등)

## 3. 담당별 범위

### DB (dev-database) — `source/db/`
- SRM 테이블: queue, service_catalog_item, catalog_form_field(JSONB options), service_request(ticket_key SRM-YYYY-####, status 상태셋, SLA 컬럼), service_request_form_value(EAV), csat(1:1, CHECK 1~5). 공통컬럼·FK·UNIQUE(6절).
- **common 교차 테이블 도입**: approval(ticket_type='SERVICE_REQUEST'), comment, timeline_event. (ticket_link는 incident 단계에서 도입 — SRM 미사용)
- screen/screen_role 증분: SCR-SRM-001~008 화면 seed + 역할정의서(END_USER/AGENT/APPROVER/PROCESS_OWNER) 기반 screen_role 매핑 추가. auth seed는 유지.
- 시연용 최소 seed 선택: 기본 queue(is_default), 예시 카탈로그 항목 1~2개(양식 필드 포함) — tester/데모 편의(과하지 않게).

### BE (dev-backend) — `source/backend/`
- API-SRM-001~015 전부(api_spec 준수). SRM 도메인 패키지 추가(기존 DDD 구조 재사용).
- 상태 전이 머신: SUBMITTED→VALIDATED→ROUTED→APPROVAL_PENDING→IN_FULFILLMENT→FULFILLED→CLOSED/REJECTED. 허용되지 않은 전이 400, 승인 대기 중 이행 409, 전이별 권한 상이(403).
- 승인: common.approval 사용(지정 승인자만 200, 그 외 403, 반려 사유 필수 400). 승인 대기 목록(API-SRM-012).
- SLA: 카탈로그 SLA 분값으로 sla_response_due/resolve_due 산정, sla_status(OK/WARNING/BREACHED) 계산.
- 코멘트(common.comment)/타임라인(common.timeline_event) 기록. CSAT(종료 요청만, 요청자만). 지표 집계(API-SRM-015).
- RBAC: scope=mine(본인)/all·queue(Agent 이상), 카탈로그 생성·수정(Process Owner), 배정(Agent), 승인(지정 Approver).
- **지식 추천(API-SRM-005) + 상세 linkedArticles**: knowledge 도메인 미구축이므로 **빈 배열([]) 반환(스펙의 "매칭 없으면 빈 배열" 준수)**. KM 도메인 개발 시 실제 연동으로 교체(코드에 TODO 표기). 감사/OpenAPI 규약은 auth와 동일.

### UI (dev-ui) — `source/frontend/` 공통 영역
- 재사용 컴포넌트 추가(도메인 걸쳐 재사용될 것): KPI 카드, 간단 추이 차트, 별점(Rating) 위젯, 동적 폼 렌더러(스키마 기반)·양식 필드 빌더(반복 입력). 기존 팔레트/토큰·공통 컴포넌트 활용.
- 도메인 전용 조립은 FE. dev-frontend와 어떤 것을 common으로 올릴지 착수 초기 합의.

### FE (dev-frontend) — `source/frontend/` 기능 영역
- 화면: SCR-SRM-001 포털, 002 요청제출(동적양식+추천패널), 003 내 요청목록, 004 요청 큐, 005 요청 상세(상태전이·승인표시·SLA타이머·코멘트·CSAT), 006 승인 대기함, 007 카탈로그 관리(양식 빌더), 008 지표 대시보드.
- 라우팅/사이드바 메뉴에 서비스요청 항목 추가(RBAC: END_USER 포털·내요청 / Agent 큐·상세 / Approver 승인함 / Process Owner 카탈로그관리). screen.path 정합(dev-database와 확인).
- 상태 전이 버튼은 허용 전이만 노출(403 방지), 승인 대기 중 이행 버튼 숨김, 종료 요청 재종료 차단, CSAT는 종료+요청자만.

## 4. 진행 순서 · 의존성

1. DB 스키마·common 테이블·screen seed 먼저(BE 영속성 기반). BE는 스키마 확정 후 연동.
2. BE API 계약(api_spec) 확정 → FE 연동. UI 신규 컴포넌트 → FE 조립.
3. 계약 단일 기준 api_spec/service-request.md. 이견은 dev-lead, 설계 이슈는 designer.

## 5. 완료(테스트 통과) 기준

- BE: API-SRM-001~015 정상 + 오류(400/401/403/404/409), 상태 전이 규칙, 승인 권한, SLA 계산, CSAT 제약, 지표. 지식 추천 []는 정상 취급.
- FE: 포털→제출→목록/큐→상세(전이·승인·코멘트·CSAT)→승인함→카탈로그관리→지표 대시보드 E2E.
- tester 통합테스트 실패 0 → `feat(service-request): ...` 커밋/푸시.

## 6. 파일 소유 (충돌 방지)

- `source/db/` = DB / `source/backend/`(srm 패키지 신설) = BE / `source/frontend/` 공통 = UI, 기능 = FE. auth 산출물 파일은 수정 최소화(공유 apiClient/레이아웃 확장은 협의).

## 7. 특이사항

- 지식 도메인(KM) 미구축: 추천·linkedArticles는 빈 배열. KM 단계에서 연동(양쪽 계획에 교차 표기).

## i18n 다국어 전환 (유지보수 요청, 2026-07-12)

> i18n 인프라·SweetAlert2·언어 선택은 common phase에서 완료됨(`docs/03_develop/plan/common.md` v3절). 이번 phase는 레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환한다(`docs/02_plan/screen/common.md` 6절). BE/DB 변경 없음.

### 담당 범위 — dev-fe 단독(UI 미소집)

- 대상 화면(`docs/02_plan/screen/service-request.md` 3절): `PortalPage.tsx`(SCR-SRM-001), `RequestSubmitPage.tsx`(002), `RequestListPage.tsx`(003), `RequestQueuePage.tsx`(004), `RequestDetailPage.tsx`(005), `CatalogManagePage.tsx`(007), `MetricsPage.tsx`(008).
- `features/service-request/status.ts` — 6.3절 전환 패턴대로 `statusLabel(t, status)` 등 라벨 매핑 함수를 `t: TFunction` 인자를 받도록 전환, 호출부(각 Page.tsx, `features/search/status.ts`의 재사용부 포함) 모두 `t` 전달.
- `useTranslation(["service-request", "common"])` 사용(공용 승인 패널·토스트 등 common 텍스트 참조 시).
- `locales/{ko,en}/service-request.json`(현재 `{}` 스캐폴딩) — 이번 phase 단독 소유, 직접 채운다.
- `format.ts`(날짜/숫자 포맷)는 변경하지 않는다(ko-KR 고정 유지, 확정된 결정 2).

### 완료 기준
- English 전환 시 서비스 포털·요청 제출·내 요청 목록·요청 큐·요청 상세·카탈로그 관리·지표 대시보드 전체 텍스트(상태 라벨 포함) 영어 전환.
- 기존 요청 제출/승인/이행/CSAT 등 기능 회귀 없음(텍스트만 치환).

## 요청 유형별 담당자 역할 지정 (유지보수 요청, 2026-07-15)

> Main 요청(유지보수). 요청 유형(카탈로그 항목)에 담당자 역할을 지정해 요청 큐 배정 팝업에서 그 역할 보유자 중 담당자를 선택할 수 있게 한다(자동배정 아님). 큐 배정 버튼 노출조건·라우팅 버튼 비활성화·카탈로그 관리 화면 Select 전환/프리필 결함도 함께 정리한다. UI 신규 소집 없음(기존 Modal/Select 컴포넌트 재사용).

### 설계 근거

- DB: `docs/02_plan/database/service-request.md` v0.3(`service_catalog_item.assignee_role_id`)
- API: `docs/02_plan/api_spec/service-request.md` v0.3(API-SRM-002/003/004 assigneeRoleId, API-SRM-017 신규, API-SRM-010 409), `docs/02_plan/api_spec/auth.md` v0.7 API-AUTH-030(역할 목록 — 구현 계획은 `docs/03_develop/plan/auth.md` 별도 절)
- 화면: `docs/02_plan/screen/service-request.md` v0.5 SCR-SRM-004/005/007
- 권한: `docs/02_plan/security/authorization/process_owner.md` v0.3, `service_desk_agent.md` v0.2
- 상세조회 APPROVER 정적 권한 폐지·동적 판정 전환은 별도 공용 작업(`docs/03_develop/plan/common.md` "승인 대상자 역할 기반 동적 상세조회 권한" 절) — 이 절에서는 그 결과만 반영(assertCanView 수정)
- 참고 기존 코드: `source/backend/src/main/java/com/itsm/srm/`(ServiceCatalogService/ServiceRequestService/ServiceCatalogController/ServiceRequestController), `source/frontend/src/features/service-request/`(QueuePage/RequestDetailPage/CatalogManagementPage), `source/backend/src/main/java/com/itsm/vulnerability/`(ASSIGNEE_REQUIRED_FOR_REMEDIATION 패턴)

### 담당 범위

#### DB (dev-database) — `source/db/sql/`

- 신규 파일 `33_srm_catalog_assignee_role.sql`(다음 순번): `ALTER TABLE service_catalog_item ADD COLUMN assignee_role_id BIGINT NULL REFERENCES role(id);`
- 시드: 기존 카탈로그 항목 중 최소 1건에 기존 시드된 역할(예: SERVICE_DESK_AGENT)로 `assignee_role_id`를 채워 tester가 후보 목록 조회(API-SRM-017)를 검증할 수 있게 한다(선택, 나머지는 NULL 유지).
- 완료 후 `source/db/sql/CLAUDE.md`에 파일 추가 반영.

#### BE (dev-backend) — `source/backend/src/main/java/com/itsm/srm/`

- **domain/ServiceCatalogItem.java**: `assigneeRoleId` 필드 추가(nullable).
- **application/dto/**: `CatalogItemDetailResponse`에 `assigneeRoleId`/`assigneeRoleName`(RoleRepository로 resolve) 추가, `CreateCatalogItemRequest`/`UpdateCatalogItemRequest`에 `assigneeRoleId`(선택) 추가. 목록 응답(`CatalogItemSummaryResponse`, API-SRM-001)은 스펙대로 변경 없음.
- **application/ServiceCatalogService.java**: create/update 시 `assigneeRoleId` 저장.
- **application/ServiceRequestService.java**:
  - 신규 메서드 `assigneeCandidates(Long requestId)`(API-SRM-017): 요청의 catalogItem.assigneeRoleId가 null이면 빈 리스트. 아니면 그 역할을 보유하고 `app_user.status='ACTIVE'`인 사용자 목록을 `{id, name}`으로 반환(기존 `UserRoleRepository`/`AppUserRepository`로 부족하면 신규 쿼리 메서드 추가).
  - `transition()`: target==ROUTED이고 `sr.getAssigneeId() == null`이면 신규 `ErrorCode.ASSIGNEE_REQUIRED_FOR_ROUTING`(409)로 거부(승인 게이트 체크보다 먼저 판정 — `vulnerability` 도메인 `ASSIGNEE_REQUIRED_FOR_REMEDIATION`과 동일 패턴).
  - `assertCanView`: 정적 APPROVER 조건 제거 후 `approvalGateService.canApproverView(...)` OR 추가(`docs/03_develop/plan/common.md` 절 그대로 적용).
- **presentation/ServiceRequestController.java**: `GET /{id}/assignee-candidates` 추가(API-SRM-017, SERVICE_DESK_AGENT 권한).
- `common/exception/ErrorCode.java`에 `ASSIGNEE_REQUIRED_FOR_ROUTING`(409) 추가.

#### FE (dev-frontend) — `source/frontend/src/features/service-request/`

- **SCR-SRM-004 요청 큐**: 배정 버튼 라벨 "나에게 배정"→"배정". 클릭 시 담당자 배정 팝업(기존 Modal 재사용) 오픈 → `GET /assignee-candidates` 호출 → 후보 있으면 이름 클릭 선택, 후보가 없거나 후보 중 본인이 있으면 "나에게 배정" 버튼도 함께 노출 → 확정 시 `POST /assign`(assigneeId). 배정 버튼은 (1) 목록 응답 `assigneeId`가 로그인 사용자 id와 일치, (2) 상태가 ROUTED/IN_FULFILLMENT/FULFILLED/CLOSED 중 하나 — 둘 중 하나라도 해당하면 숨김(목록 API에 `assigneeId` 필드 이미 추가됨, API-SRM-007).
- **SCR-SRM-005 요청 상세**: ROUTED 전이 버튼을 `assignee` 없으면 비활성화 + tooltip("담당자 미배정 상태로는 라우팅 단계로 전이할 수 없습니다"). 서버 409(`ASSIGNEE_REQUIRED_FOR_ROUTING`)도 동일 메시지 토스트 처리(방어적).
- **SCR-SRM-007 카탈로그 관리**: 담당 큐 select 옆에 담당자 역할 select 추가(`GET /api/v1/roles`, API-AUTH-030 — auth BE 작업 완료 후 연동). 항목 편집 진입 시 두 select 모두 상세 조회 응답(`queueId`/`assigneeRoleId`)으로 프리필(기존 큐 select가 편집 진입 시 항상 빈 값으로 리셋되던 결함도 함께 수정 — 폼 초기값이 상세 응답 로딩 전에 세팅되는 타이밍 문제 확인).
- `features/service-request/api.ts`에 `getAssigneeCandidates(requestId)` 추가. 역할 목록 조회(`getRoles`, API-AUTH-030)는 auth FE 소유 파일과 겹치지 않게 위치 확인 후 추가(애매하면 dev-lead에게 확인).

### 완료(테스트 통과) 기준

- BE: 카탈로그 생성/수정 시 assigneeRoleId 저장·조회, API-SRM-017 역할 미지정 시 빈 배열/지정 시 해당 역할 ACTIVE 사용자만 반환, ROUTED 전이 시 담당자 미배정 409, 배정 후 정상 라우팅.
- FE: 큐 화면 배정 팝업 후보 선택/본인배정 동작, 노출조건 2가지 정상 동작, 상세 화면 라우팅 버튼 비활성화+tooltip, 카탈로그 관리 화면 담당자 역할 select 및 프리필 결함 수정 확인(구 버그 재현 후 해결 확인).
- tester 통합 테스트 후 dev-lead에 결과 보고 → 실패 0까지 수정 루프 → 완료 시 커밋.
