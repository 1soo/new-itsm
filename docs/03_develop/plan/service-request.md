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

## 전이 버튼 라벨·타임라인 actor·카탈로그 카테고리 CRUD·textarea 필드 (유지보수 요청, 2026-07-16)

### 설계 근거
- 화면: `docs/02_plan/screen/service-request.md` v0.5 SCR-SRM-005(전이 버튼 라벨표 131~140행), SCR-SRM-007/009(카테고리 관리, 탭 구성).
- API: `docs/02_plan/api_spec/service-request.md` v0.4 — API-SRM-001/002/003/004(`categoryId`/`categoryName`), API-SRM-008(`timeline[].actor`), API-SRM-010(전이), API-SRM-018~021(카테고리 CRUD 신규).
- DB: `docs/02_plan/database/service-request.md` v0.4 — `service_catalog_category` 신규, `service_catalog_item.category`(자유 텍스트) → `category_id`(FK) 전환, `catalog_form_field.field_type`에 `textarea` 추가.
- 권한: `docs/02_plan/security/authorization/process_owner.md` v0.4(카테고리 CRUD는 PROCESS_OWNER 전용, 목록 조회는 공개).
- 공통 아키텍처: `docs/03_develop/plan/common.md` "상태 전이 버튼 라벨·타임라인 actor 공통 아키텍처" 절.
- 참고 기존 코드: `source/backend/.../srm/domain/ServiceCatalogItem.java`·`RequestStatus.java`, `application/ServiceCatalogService.java`·`ServiceRequestService.java`, `application/dto/CatalogItemDetailResponse.java`·`CreateCatalogItemRequest.java`·`UpdateCatalogItemRequest.java`·`RequestDetailResponse.java`, `presentation/ServiceCatalogController.java`, `infrastructure/persistence/ServiceCatalogItemJpaRepository.java`; `source/frontend/.../service-request/status.ts`·`types.ts`·`RequestDetailPage.tsx`·`CatalogManagePage.tsx`·`PortalPage.tsx`.

### 담당 범위

#### DB (dev-database) — `source/db/sql/`
- 신규 파일 `34_srm_catalog_category.sql`(다음 순번):
  - `service_catalog_category` 테이블 신규(공통 컬럼 규칙, `id`/`name` UNIQUE/`sort_order`/`created_by/at`/`updated_by/at`/`is_deleted` — `queue` 테이블 DDL 패턴 참고).
  - `service_catalog_item.category_id BIGINT NULL REFERENCES service_catalog_category(id)` 추가.
  - 기존 `category`(자유 텍스트) 값이 있으면 DISTINCT 값으로 `service_catalog_category` 백필 후 `category_id` 매핑(값이 없으면 스킵, 에러 아님).
  - 백필 완료 후 `service_catalog_item.category` 컬럼 DROP(26번 마이그레이션이 옛 승인 컬럼을 제거한 패턴과 동일).
  - 시드: 예시 카테고리 2~3건(예: "하드웨어"/"소프트웨어"/"계정") 생성 후 기존 카탈로그 항목 중 최소 1건에 매핑(tester가 카테고리 필터·표시를 검증할 수 있게).
- 신규 파일 `35_catalog_form_field_textarea_type.sql`(SRM+ESM 공유 기능이라 한 파일에 두 테이블 반영):
  - `catalog_form_field`와 `esm_catalog_form_field` 각각의 `ck_catalog_form_field_type`/`ck_esm_catalog_form_field_type` CHECK 제약을 DROP 후 `'textarea'` 값을 포함해 재생성.
- 완료 후 `source/db/sql/CLAUDE.md`에 두 파일 반영.

#### BE (dev-backend) — `source/backend/src/main/java/com/itsm/srm/`

**1. 전이 버튼 라벨(FE 전용, BE 변경 없음)** — 아래 3번 항목만 BE 대상.

**2. 카탈로그 카테고리 CRUD(신규)**
- `domain/ServiceCatalogCategory.java` 신규 엔티티(`id`/`name`/`sortOrder`).
- `domain/repository/ServiceCatalogCategoryRepository.java` + `infrastructure/persistence/ServiceCatalogCategoryJpaRepository.java`(`ServiceCatalogItemRepository` 패턴 그대로).
- `application/dto/`에 `CategoryResponse`(id, name, sortOrder), `CategoryCreateRequest`/`CategoryUpdateRequest`(name, sortOrder) 신규.
- `application/ServiceCatalogCategoryService.java` 신규(list — `sortOrder` 오름차순, create — 이름 중복 시 409, update, delete — 참조 중인 `service_catalog_item.category_id`가 있으면 409 `CATEGORY_IN_USE`, 자동 미분류 처리 안 함).
- `presentation/ServiceCatalogCategoryController.java` 신규(`/api/v1/service-catalog/categories`): GET(인증만), POST/PATCH/DELETE(`@PreAuthorize("hasRole('PROCESS_OWNER')")`, `ServiceCatalogController`의 `@PreAuthorize` 패턴 그대로).
- `common/exception/ErrorCode.java`에 `CATEGORY_NOT_FOUND`(404), `CATEGORY_NAME_DUPLICATE`(409), `CATEGORY_IN_USE`(409) 추가.
- **기존 카탈로그 항목 연동**: `domain/ServiceCatalogItem.java`의 `category`(String) 필드를 `categoryId`(Long)로 교체(생성자·`update()` 시그니처도 함께 변경). `domain/repository/ServiceCatalogItemRepository.search(String category, String keyword)` → `search(Long categoryId, String keyword)`로 변경, `ServiceCatalogItemJpaRepository`의 JPQL도 `i.categoryId = :categoryId` 조건으로 교체. `application/dto/CatalogItemSummaryResponse`/`CatalogItemDetailResponse`의 `category` 필드를 `categoryId`/`categoryName`으로 교체(`categoryName`은 `assigneeRoleName`과 동일하게 `ServiceCatalogCategoryRepository`로 resolve). `CreateCatalogItemRequest`/`UpdateCatalogItemRequest`에 `categoryId`(선택) 필드 추가, 존재하지 않는 categoryId면 404. `ServiceCatalogService.list/create/update/toDetail`을 위 변경에 맞게 수정. `ServiceCatalogController.list`의 `@RequestParam String category` → `Long categoryId`.

**3. 타임라인 actor + 코드→라벨**
- `domain/RequestStatus.java`에 `label()` 메서드 추가(FE `features/service-request/status.ts`의 `STATUS_LABEL`과 동일 한글값 6종).
- `application/dto/RequestDetailResponse.TimelineEntry`에 `actor` 필드 추가(현재 `(String type, String message, OffsetDateTime at)` → `actor` 추가).
- `application/ServiceRequestService.java`의 상세 조회 메서드: 타임라인 조회 시 각 `TimelineEvent.getCreatedBy()`(email)로 `appUserRepository.findByEmail()` 조회해 이름 resolve(실패 시 email 폴백, 기존 요청자명 조회와 동일 패턴)해 `actor`에 채움.
- `transition()` 메서드의 `TimelineEvent.of(TT, id, "STATUS_" + target.name(), ... "상태가 " + target.name() + "로 변경되었습니다.")`에서 `target.name()`(메시지 부분만) → `target.label()`로 교체(이벤트 타입 문자열 `"STATUS_" + target.name()`은 코드 그대로 유지, 사람이 읽는 메시지 부분만 라벨로).

**4. textarea(BE 변경 없음)** — `application/dto/FormFieldDto.java`의 `@Schema` 설명 문구만 `text|select|number|date|file` → `text|textarea|select|number|date|file`로 갱신(저장·검증 로직은 `text`와 동일해 로직 변경 불필요).

#### FE (dev-fe) — `source/frontend/src/features/service-request/`, `source/frontend/src/components/common/`

**1. 전이 버튼 라벨**
- `status.ts`에 `transitionLabel(t, target: SrStatus): string` 신규(i18n 키 `service-request:transition.*`, 매핑값은 `docs/02_plan/screen/service-request.md` 132~140행 표). `statusLabel`은 변경하지 않음.
- `RequestDetailPage.tsx`의 전이 버튼 텍스트만 `statusLabel(t, target)` → `transitionLabel(t, target)`으로 교체(토스트 문구는 `statusLabel` 유지).

**2. 타임라인 actor**
- `types.ts`의 `RequestDetail.timeline` 항목 타입에 `actor: string` 추가.
- `RequestDetailPage.tsx`의 `timelineItems` 매핑(226행 부근)에 `actor: entry.actor` 추가.

**3. 카탈로그 카테고리 CRUD**
- `api.ts`에 `srmApi.listCategories()`(GET, 인증만)·`createCategory()`·`updateCategory()`·`deleteCategory()`(API-SRM-018~021) 추가.
- `types.ts`에 `Category`(id, name, sortOrder) 타입 추가, `CatalogItemDetail`/`CatalogItemInput`의 `category?: string` → `categoryId?: number`/`categoryName?: string`로 교체.
- **SCR-SRM-009 신규**: `CatalogManagePage.tsx`에 탭 UI 추가(설계 SCR-SRM-009 "SCR-SRM-007과 탭 구성" 참고) — 기존 카탈로그 항목 탭 + "카테고리 관리" 탭(목록 표+생성/수정/삭제, PROCESS_OWNER 전용). 삭제 409(`CATEGORY_IN_USE`) 시 오류 토스트로 안내(자동 미분류 처리 없음).
- 카탈로그 항목 생성/수정 폼에 카테고리 Select 추가(담당자 역할 Select와 동일 패턴, sentinel 값으로 "미지정" 표현).
- `PortalPage.tsx`의 `item.category` 배지를 `item.categoryName`으로 교체.

**4. textarea 필드 유형(공통 컴포넌트, SRM+ESM 공유 — 이 phase에서 한 번만 작업)**
- `components/common/form-schema.ts`의 `FormFieldSchema.type` 유니온에 `"textarea"` 추가.
- `components/common/dynamic-form.tsx`에 `case "textarea"` 추가(`<textarea>` 렌더, `text` case와 동일한 값 처리·오류 표시).
- `components/common/field-builder.tsx`의 필드 유형 Select에 "textarea" 옵션 추가(`fieldBuilder.fieldType.textarea` i18n 키).

### 진행 순서
1. DB(카테고리 마이그레이션) → BE(카테고리 CRUD+기존 카탈로그 연동) → FE(카테고리 CRUD 화면+Select) — 순차.
2. DB(textarea CHECK) → FE(공통 컴포넌트 3파일) — 순차, 카테고리와 병렬 가능.
3. 전이 버튼 라벨·타임라인 actor는 FE/BE 각자 독립 파일이라 위 1·2와 병렬 진행 가능.

### 완료 기준
- 요청 상세(SCR-SRM-005)의 전이 버튼에 동작 동사형 라벨이 표시되고, 전이 완료 토스트는 기존처럼 도착 상태명을 사용한다.
- 요청 상세 타임라인의 상태 변경 항목에 행위 수행자 이름과 "코드가 아닌" 상태 한글 라벨이 표시된다.
- 카탈로그 관리 화면에서 카테고리를 생성/수정/삭제할 수 있고, 참조 중인 카테고리 삭제 시 409로 거부된다. 카탈로그 항목 생성/수정 시 카테고리를 선택할 수 있고 포털 카드에 카테고리명이 표시된다.
- 카탈로그 관리에서 여러 줄 텍스트(textarea) 필드 유형을 추가할 수 있고, 요청 제출 폼에 `<textarea>`로 렌더링된다.

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

## 레트로핏 — 2026-07-17: 폼 렌더러 제출/취소 버튼 우측 하단 배치

> SRM 통합테스트 진행 중 접수된 추가 요구사항. 공용 컴포넌트 변경은 `docs/03_develop/plan/common.md` "폼 렌더러 제출/취소 버튼 우측 하단 배치" 절(dev-ui) 참조.

#### FE (dev-fe) — `source/frontend/src/features/service-request/RequestSubmitPage.tsx`

- 기존 `<div className="flex justify-end"><Button variant="outline">취소</Button></div>`(DynamicFormRenderer 아래 별도 렌더) 제거.
- `DynamicFormRenderer`에 `onCancel={() => navigate("/portal")}` 전달(기존 취소 동작 그대로), `cancelLabel`/`submitLabel`은 기존 i18n 키(`requestSubmit.cancel`/필요 시 `requestSubmit.submit` 신규) 값으로 전달.
- dev-ui의 `DynamicFormRenderer` 변경(신규 `onCancel` prop 등) 완료 후 착수.

### 완료 기준
- SCR-SRM-002에서 제출/취소 버튼이 폼 우측 하단(취소 왼쪽, 제출 오른쪽)에 정렬되어 표시되고 기존 동작(취소 시 포털 이동, 제출 시 요청 생성) 회귀 없음.
- tester 재테스트 통과 후 커밋.

## 개발 계획 — 2026-07-17 유지보수: 서비스 카탈로그 커스텀 폼 빌더(form.io)

- 설계 근거: `docs/02_plan/database/service-request.md` v0.5 1~6절(EAV 폐기·JSONB 전환·마이그레이션 방향), `docs/02_plan/api_spec/service-request.md` v0.5 API-SRM-002/003/004/006, `docs/02_plan/api_spec/common.md` 0-2절(서버 재검증), `docs/02_plan/screen/service-request.md` SCR-SRM-002/007, `docs/02_plan/screen/common.md` 8절.
- SRM/ESM 공용 컴포넌트(`dynamic-form-builder.tsx`/`dynamic-form-renderer.tsx`/`form-schema.ts`/`FormSubmissionValidator`)는 이 SRM phase에서 함께 만든다 — 실행 지시는 `docs/03_develop/plan/common.md` "동적 폼 빌더·렌더러 공용 아키텍처" 절 참조(dev-ui/dev-be 담당).
- 참고 기존 코드: `source/backend/.../srm/domain/CatalogFormField.java`·`ServiceCatalogItem.java`·`ServiceRequestFormValue.java`, `application/ServiceCatalogService.java`·`ServiceRequestService.java`·`dto/FormFieldDto.java`·`CatalogItemDetailResponse.java`·`CreateCatalogItemRequest.java`·`UpdateCatalogItemRequest.java`; `source/db/sql/04_srm_schema.sql`; `source/frontend/.../service-request/CatalogManagePage.tsx`·`RequestSubmitPage.tsx`·`api.ts`.

### 담당 범위

#### DB (dev-database) — `source/db/sql/`

- 신규 파일 `36_srm_form_schema_jsonb.sql`(다음 순번, 실제 생성 시점의 최신 파일 다음 번호로 확인):
  1. `service_catalog_item`에 `form_schema JSONB NOT NULL DEFAULT '{"display":"form","components":[]}'` 추가.
  2. 기존 `catalog_form_field` 행을 `catalog_item_id`로 그룹핑, `sort_order` 오름차순으로 Form.io 컴포넌트 객체 배열로 변환해 `{ "display": "form", "components": [...] }` 형태로 `form_schema`에 백필(변환 규칙은 `docs/02_plan/database/service-request.md` 82~86행 그대로 — `field_type`→`type` 매핑: text→textfield, textarea→textarea, select→select(`options`→`data.values[].{label,value}`), number→number, date→datetime(`enableTime:false`), file→file(`storage:'base64'`); `field_key`→`key`, `label`→`label`, `required`→`validate.required`, 공통 `input:true`).
  3. `service_request`에 `form_values JSONB NOT NULL DEFAULT '{}'` 추가. 기존 `service_request_form_value` 행을 `service_request_id`로 그룹핑해 `{ field_key: field_value }` 객체로 조립해 백필.
  4. `DROP TABLE catalog_form_field;`, `DROP TABLE service_request_form_value;`.
- `source/db/sql/CLAUDE.md` 갱신(두 테이블 제거, 두 컬럼 추가 반영).

#### BE (dev-backend) — `source/backend/src/main/java/com/itsm/srm/`

- **domain/ServiceCatalogItem.java**: `formSchema`(JSONB) 필드 추가 — 기존 `CatalogFormField.options`와 동일한 `@JdbcTypeCode(SqlTypes.JSON)` + `@Column(columnDefinition = "jsonb")` 패턴 재사용(타입은 `String`으로 두고 애플리케이션 계층에서 Jackson `ObjectMapper`로 `Map<String,Object>`/`JsonNode` 변환 — 기존 컨벤션과 동일).
- **domain/CatalogFormField.java**, **domain/repository/CatalogFormFieldRepository.java**, **infrastructure/persistence/CatalogFormFieldJpaRepository.java** 삭제.
- **domain/ServiceRequest.java**: `formValues`(JSONB) 필드 추가(동일 패턴).
- **domain/ServiceRequestFormValue.java**, **domain/repository/ServiceRequestFormValueRepository.java**, **infrastructure/persistence/ServiceRequestFormValueJpaRepository.java** 삭제.
- **application/dto/FormFieldDto.java** 삭제(필드 배열 계약 폐기). `CatalogItemDetailResponse.formSchema`/`CreateCatalogItemRequest.formSchema`/`UpdateCatalogItemRequest.formSchema`를 `List<FormFieldDto>` → `Map<String,Object>`(또는 동등한 JSON 트리 타입)로 변경 — Form.io Form JSON(`{display, components}`) 그대로 왕복.
- **application/ServiceCatalogService.java**: `saveFields`(CatalogFormField 저장 루프) 제거, create/update 시 `formSchema` 원본 JSON을 `ServiceCatalogItem.formSchema`에 그대로 저장. 상세 조회 시 `formFieldRepository` 조립 로직 제거하고 `formSchema` 그대로 반환.
- **application/ServiceRequestService.java**: 요청 생성 시 `ServiceRequestFormValue` 저장 루프 제거, `formValues` 맵을 `ServiceRequest.formValues`에 그대로 저장. 기존 `validateRequiredFields(...)` 호출을 `common.form.FormSubmissionValidator`(공용, `docs/03_develop/plan/common.md` 참조) 호출로 교체 — 카탈로그 항목의 `formSchema` + 제출 `formValues`를 넘겨 400 처리.
- 상세 조회 응답의 `formValues`도 저장된 JSONB 맵을 그대로 반환(기존 EAV 조립 로직 제거).

#### FE (dev-fe) — `source/frontend/src/features/service-request/`

- **SCR-SRM-007 `CatalogManagePage.tsx`**: 기존 `FieldBuilder`(→`field-builder.tsx`) 사용부를 `dynamic-form-builder.tsx`의 `DynamicFormBuilder`로 교체. 상세 조회 응답 `formSchema`를 `initialForm`으로 주입, `onChange`로 로컬 상태 축적, 기존 "저장" 버튼 클릭 시 축적된 Form JSON을 카탈로그 생성/수정 API 페이로드(`formSchema`)로 전달.
- **SCR-SRM-002 `RequestSubmitPage.tsx`**: 기존 `DynamicForm`(→`dynamic-form.tsx`) 사용부를 `dynamic-form-renderer.tsx`의 `DynamicFormRenderer`로 교체. 카탈로그 상세 조회 `formSchema`를 `src`로 주입, `onSubmit`으로 받은 `submission.data`를 그대로 요청 생성 API(`formValues`)로 전달.
- **`api.ts`**: `formSchema`/`formValues` 관련 TS 타입을 필드 배열 타입에서 `Record<string, unknown>`(또는 `FormIoSchema`, `form-schema.ts` 참조)으로 갱신. 기존 필드 배열 전용 유효성 검사 로직이 있으면 제거(서버 재검증으로 이관됨).
- UI(dev-ui)가 공용 컴포넌트를 먼저 만들어야 이 작업을 시작할 수 있으므로, 공용 컴포넌트 완료 전에는 API 타입 정리 등 독립 작업부터 진행 가능.

### 완료(테스트 통과) 기준

- DB: 마이그레이션 후 `catalog_form_field`/`service_request_form_value` 삭제, 기존 데이터가 `form_schema`/`form_values`로 정상 백필됨.
- BE: 카탈로그 생성/수정 시 Form.io Form JSON 그대로 저장·조회, 요청 제출 시 `FormSubmissionValidator`가 required/minLength/maxLength/min/max/pattern 위반을 400으로 거부, 정상 제출은 `form_values`에 `submission.data` 그대로 저장.
- FE: SCR-SRM-007에서 컬럼/패널/탭 포함 자유배치 폼 설계·저장, SCR-SRM-002에서 그 폼이 그대로 렌더링되고 제출 가능. ADS 톤에 어긋나지 않는 스타일(`.formio-scope` 오버라이드 적용 확인).
- tester 통합 테스트 후 dev-lead에 결과 보고 → 실패 0까지 수정 루프 → 완료 시 커밋.
