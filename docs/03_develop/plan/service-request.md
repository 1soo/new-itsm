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

## 개발 계획 — 2026-07-18 유지보수: 요청 큐 폐지 → 카테고리 기반 분류 일원화

- 요구사항: `queue` 테이블·`service_catalog_item.queue_id`·`service_request.queue_id`를 완전히 제거하고, 요청 분류/필터링을 `service_request.catalog_item_id → service_catalog_item.category_id` **실시간 조인**으로 대체(스냅샷 컬럼 없음 — 카탈로그 항목의 카테고리를 바꾸면 과거 요청 분류도 즉시 반영). `category_id`는 계속 nullable(미분류 그룹 유지). 담당자 배정 후보 필터링(`assigneeRoleId`)은 이번 변경과 무관 — 손대지 않는다.
- 설계 근거: `docs/02_plan/database/service-request.md`(변경 이력 표 2026-07-18 행, queue 테이블/컬럼 제거), `docs/02_plan/api_spec/service-request.md`(API-SRM-007 `categoryId=` 필터, API-SRM-016 "요청 카테고리별 건수 조회"로 대체, API-SRM-002/003/004 `queueId` 제거, API-SRM-008 응답 `queue` 필드 제거), `docs/02_plan/screen/service-request.md`(SCR-SRM-004 "요청 처리함"으로 개칭, SCR-SRM-007 담당 큐 select 제거).
- 참고 기존 코드: `source/backend/.../srm/domain/Queue.java`·`ServiceCatalogItem.java`·`ServiceRequest.java`, `application/QueueService.java`·`ServiceCatalogService.java`·`ServiceRequestService.java`, `application/dto/QueueResponse.java`·`CatalogItemDetailResponse.java`·`CreateCatalogItemRequest.java`·`UpdateCatalogItemRequest.java`·`RequestDetailResponse.java`, `presentation/QueueController.java`·`ServiceRequestController.java`, `infrastructure/persistence/QueueJpaRepository.java`·`ServiceRequestJpaRepository.java`; `source/frontend/.../service-request/RequestQueuePage.tsx`·`CatalogManagePage.tsx`·`RequestDetailPage.tsx`·`api.ts`·`types.ts`.

### 담당 범위

#### DB (dev-database) — `source/db/sql/`

- 신규 파일 `37_srm_queue_retirement.sql`(다음 순번, 실제 생성 시점의 최신 파일 다음 번호로 확인):
  1. `ALTER TABLE service_catalog_item DROP COLUMN queue_id;`
  2. `ALTER TABLE service_request DROP COLUMN queue_id;`
  3. `DROP TABLE queue;`
  4. `UPDATE screen SET screen_name = '요청 처리함', screen_name_en = 'Request Inbox' WHERE screen_code = 'SCR-SRM-004';`(경로·아이콘·그룹·정렬순서는 변경하지 않는다 — 라벨만 개칭, dev-lead 판단).
- 백필 불필요(컬럼 자체 삭제, 스냅샷 없음).
- `05_srm_seed.sql`(큐 시드)은 원본 미수정(26/33/34/35/36번 선례와 동일 패턴 — 이후 마이그레이션이 DROP하므로 무해).
- 완료 후 `source/db/sql/CLAUDE.md`에 파일 반영.

#### BE (dev-backend) — `source/backend/src/main/java/com/itsm/srm/`

**삭제 대상**
- `domain/Queue.java`, `domain/repository/QueueRepository.java`, `infrastructure/persistence/QueueJpaRepository.java`, `application/QueueService.java`, `application/dto/QueueResponse.java`, `presentation/QueueController.java`(→ `GET /api/v1/queues` 폐지).

**엔티티/DTO**
- `domain/ServiceCatalogItem.java`: `queueId` 필드·생성자 파라미터·`update()` 파라미터 제거.
- `domain/ServiceRequest.java`: `queueId` 필드·생성자 파라미터 제거.
- `application/dto/CreateCatalogItemRequest.java`·`UpdateCatalogItemRequest.java`·`CatalogItemDetailResponse.java`: `queueId` 필드 제거.
- `application/dto/RequestDetailResponse.java`: `queue` 필드 제거.
- 신규 `application/dto/CategoryCountResponse.java`(API-SRM-016): `categoryId: Long|null`, `categoryName: String|null`, `openCount: long`.

**서비스 로직**
- `application/ServiceCatalogService.java`: create/update에서 `queueId` 처리 로직 제거.
- `application/ServiceRequestService.java`:
  - `create()`: 기존 "카탈로그 큐 없으면 기본 큐로 배정" 로직(`queueRepository.findFirstByIsDefaultTrue()` 포함) 완전 제거, `ServiceRequest` 생성자 호출에서 `queueId` 인자 제거.
  - `list()`/검색: 파라미터를 `queueId` → `categoryId`(nullable Long) + `uncategorized`(boolean, 컨트롤러가 raw 쿼리 파라미터 `categoryId=uncategorized` 리터럴을 파싱해 전달)로 교체. `service_catalog_item.category_id`와 조인해 필터링(기존 `searchByKeyword`의 `exists (select 1 from ServiceCatalogItem c where c.id = r.catalogItemId ...)` 패턴 참고, 이번엔 존재 확인이 아니라 `categoryId` 값 비교이므로 스칼라 서브쿼리 또는 세미조인으로 구현).
  - 신규 `categoryCounts()`(API-SRM-016): `service_catalog_category`를 `sort_order` 오름차순으로 순회하며 각 카테고리에 속한 카탈로그 항목들의 미종료(`status <> CLOSED`) 요청 건수를 집계, 마지막에 미분류(`category_id IS NULL`) 그룹 건수를 추가.
- `domain/repository/ServiceRequestRepository.java`·`infrastructure/persistence/ServiceRequestJpaRepository.java`: `search(Long requesterId, Long queueId, ...)` 시그니처를 `categoryId`/`uncategorized` 기준으로 교체, `countOpenByQueueId` 삭제하고 카테고리별 집계 쿼리 신규 추가(신규 메서드명은 dev-backend 재량, 예: `countOpenByCategoryId`/`countOpenUncategorized` 또는 집계 전용 프로젝션 쿼리 1개).
- `presentation/ServiceRequestController.java`:
  - 목록 조회 쿼리 파라미터 `queue=` → `categoryId=`(문자열: 숫자 또는 리터럴 `"uncategorized"`, api_spec 그대로).
  - 신규 `GET /api/v1/service-requests/category-counts`(API-SRM-016) 추가. 기존 `/{id}` 경로와 충돌 검토(이 코드베이스에 이미 `/metrics` 같은 정적 서브경로가 `/{id}`와 공존하는 전례 있음 — 동일 패턴으로 안전).
- `common/exception/ErrorCode.java`: 이번 변경으로 신규 오류코드 없음(큐 관련 오류코드가 있었다면 확인 후 제거).

**테스트**
- `ServiceCatalogServiceTest`/`ServiceRequestServiceTest`/`SrmApprovalIntegrationTest`의 `queueId`/`Queue` 관련 mock·생성자 호출 전부 제거·갱신(직전 form.io phase에서 이미 유사 작업 진행한 경험 참고).

#### FE (dev-frontend) — `source/frontend/src/features/service-request/`

- **파일명 유지**: `RequestQueuePage.tsx` 파일명은 그대로 둔다(설계는 화면 개념·라벨만 개칭했고, 파일명 변경은 불필요한 참조 churn이므로 — dev-lead 판단). 화면 타이틀 텍스트만 "요청 큐" → "요청 처리함"(`requestQueue.title` i18n 값 갱신).
- **`RequestQueuePage.tsx`**: 좌측 `QueueButton` 목록(큐+건수, `listQueues()`)을 카테고리 목록(카테고리명/"미분류"+건수, 신규 `getCategoryCounts()`)으로 교체. 카테고리 클릭 시 우측 표를 `categoryId=`(숫자 또는 `"uncategorized"`)로 서버 필터링. `isDefault`/"기본" 배지는 제거(카테고리엔 해당 개념 없음), "미분류" 그룹은 목록 마지막 고정 노출(API 응답이 이미 이 순서로 옴).
- **`RequestDetailPage.tsx`**: 268행 `<MetaRow label={t("requestDetail.queue", ...)} value={detail.queue || "-"} />` 삭제(백엔드 응답에서 `queue` 필드 자체가 사라지므로).
- **`CatalogManagePage.tsx`**: "담당 큐 선택" Select 블록(438~453행 부근) 전체 삭제, `FormState.queueId`/`EMPTY_FORM.queueId`/`queues` state/`listQueues()` 호출/제출 payload의 `queueId` 매핑 모두 제거.
- **`api.ts`**: `listQueues()` 삭제, 신규 `getCategoryCounts(): Promise<CategoryCount[]>`(`GET /service-requests/category-counts`) 추가. `listRequests`의 query 타입에서 `queue` → `categoryId`(string) 교체.
- **`types.ts`**: `Queue` 인터페이스 삭제, 신규 `CategoryCount { categoryId: number | null; categoryName: string | null; openCount: number }` 추가. `CatalogItemDetail`/`CatalogItemInput`의 `queueId` 필드 삭제. `RequestSummary`/`RequestDetail`의 `queue?: string` 필드 삭제. `RequestListQuery`의 `queue?: string` → `categoryId?: string` 교체.

### 진행 순서

1. DB(큐 테이블/컬럼 제거) → BE(엔티티/서비스/컨트롤러 정리 + 카테고리 필터·집계 API) → FE(처리함 화면 카테고리 전환, 카탈로그 관리 큐 select 제거) — 순차.

### 완료(테스트 통과) 기준

- DB: `queue` 테이블 및 관련 FK 컬럼 완전 제거, 기존 요청/카탈로그 데이터 정상 조회(카테고리 조인 정상 동작).
- BE: `GET /api/v1/service-requests?categoryId=` 숫자/`"uncategorized"`/미지정 3가지 모두 정상 필터링, `GET /api/v1/service-requests/category-counts`가 카테고리별(+미분류 마지막) 미종료 건수 정상 반환, `/api/v1/queues` 관련 엔드포인트·클래스 완전 제거.
- FE: SCR-SRM-004 좌측 카테고리 목록 클릭 시 우측 표 필터링 정상 동작, 카탈로그 항목의 카테고리를 바꾸면 기존 요청 분류도 즉시 반영됨(실시간 조인 확인), 카탈로그 관리 화면에 담당 큐 select가 더 이상 없음, 요청 상세에 "큐" 표시 없음.
- tester 통합 테스트 후 dev-lead에 결과 보고 → 실패 0까지 수정 루프 → 완료 시 커밋.

## 개발 계획 — 2026-07-18 유지보수: form.io 완전 제거 → 자체 8×n 그리드 폼 빌더 전면 재구현

- 요구사항: `@formio/js`/`@formio/react` 의존성과 관련 코드를 완전히 제거하고(주석 처리 금지), SRM 서비스 카탈로그의 동적 양식을 자체 구현한 8칸 고정×n행(스크롤 가능) 그리드 드래그앤드롭 빌더로 전면 재구현한다. 팔레트 7종(text/textarea/select/radio/checkbox/date/file), 컴포넌트 크기 1~2칸×1~2칸(textarea만 높이 제약 없음), 컴포넌트별 Content 설정(label 정렬, input 폭%/정렬/default/읽기전용, 필수 여부, 정규식 validation, select/radio/checkbox는 콤마구분 옵션+CI연계 라디오 자리(로직 미구현)), pre-view 축소판. 기존 `service_catalog_item.form_schema`(form.io 스키마) 데이터는 호환 불가능하므로 빈 그리드로 리셋(사용자 승인 완료, 마이그레이션 아님). 이 아키텍처는 SRM 전용이다(ESM은 레거시 EAV 그대로 사용, 대상 아님).
- 설계 근거: `docs/02_plan/screen/service-request.md` 5절(그리드 아키텍처 전체 — 5.1 공용 컴포넌트/5.2 그리드 규칙/5.3 팔레트/5.4 Content 설정/5.5 pre-view·저장 흐름/5.6 기존 데이터 처리), SCR-SRM-002/007(4절), `docs/02_plan/database/service-request.md`(`form_schema` 컬럼 상세·리셋 노트), `docs/02_plan/api_spec/service-request.md`(API-SRM-002/003/004 grid 컴포넌트 JSON 스키마 예시), `docs/02_plan/api_spec/common.md` 0-2절(서버 재검증 규칙, SRM 전용으로 재정의됨), `docs/02_plan/screen/common.md` 8절(SRM으로 이전 완료, 포인터만 남음).
- 참고 기존 코드(전면 재작성 대상 — form.io 기반): `source/frontend/src/components/common/dynamic-form-builder.tsx`·`dynamic-form-renderer.tsx`·`form-schema.ts`·`index.ts`, `source/frontend/src/index.css`(`.formio-scope`/`.formio-dialog`/`.formio-render-scope` 관련 규칙 전부 제거 대상), `source/frontend/src/features/service-request/CatalogManagePage.tsx`·`RequestSubmitPage.tsx`, `source/frontend/package.json`(`@formio/js`/`@formio/react` 의존성 제거), `source/backend/.../common/form/FormSubmissionValidator.java`·`FormJsonMapper.java`, `source/backend/.../srm/application/ServiceCatalogService.java`. 유사 이력(반대 방향 전환): `docs/06_maintenance/20260718-070801/srm/history.md`.

### 담당 범위

#### DB (dev-db) — `source/db/sql/`

- 신규 파일 `38_srm_form_schema_reset.sql`(다음 순번):
  1. `UPDATE service_catalog_item SET form_schema = '{"components":[]}'::jsonb;`(전체 로우 리셋 — 마이그레이션이 아니라 단순 초기화, `docs/02_plan/database/service-request.md` "기존 데이터 리셋" 노트 근거).
  2. `ALTER TABLE service_catalog_item ALTER COLUMN form_schema SET DEFAULT '{"components":[]}';`(36번 마이그레이션이 설정한 구 기본값 `{"display":"form","components":[]}` 정리 — BE가 생성 시 항상 명시적으로 `formSchema`를 채우므로 실사용 영향은 없으나 스키마 일관성 차원).
- `service_request.form_values`(제출 데이터, key-value)는 레이아웃과 무관해 리셋 대상 아님 — 손대지 마라.
- 완료 후 `source/db/sql/CLAUDE.md`에 파일 반영.

#### BE (dev-be) — `source/backend/src/main/java/com/itsm/common/form/`, `source/backend/src/main/java/com/itsm/srm/`

> 이미 상당 부분 진행됨(FormSubmissionValidator 평면 배열 재검증 리라이트, ServiceCatalogService `DEFAULT_FORM_SCHEMA`/DTO `@Schema` 설명 갱신 확인함, compileJava/compileTestJava 성공). 아래는 전체 범위 기준 재확인용 — 이미 끝난 항목은 스킵하고 남은 것만 마무리.

- `common/form/FormSubmissionValidator.java`: `form_schema.components`(평면 배열, 재귀 불필요)를 순회하며 각 컴포넌트의 `key`/`label`/`validation.required`/`validation.regex`만 검증(기존 `validate.required`/`pattern`/`minLength`/`maxLength`/`min`/`max` 계약 완전 폐기 — 그리드 스키마엔 그 필드들 자체가 없음). `required`면서 값 없으면 `REQUIRED_FIELD_MISSING`, `regex` 지정 시(값 있을 때만) 불일치하면 `FORM_FIELD_INVALID`. `type`/`position`/`size`/`labelAlign`/`input`/`options`/`ciLinked`는 서버 재검증 대상 아님(레이아웃·표시 전용, `docs/02_plan/api_spec/common.md` 0-2절 그대로).
- `common/form/FormJsonMapper.java`: 변경 불필요(범용 `Map<String,Object>`↔JSON 직렬화 유틸이라 그리드 스키마에도 그대로 재사용 가능, 확인만 하면 됨).
- `application/ServiceCatalogService.java`: `DEFAULT_FORM_SCHEMA` 상수를 `{"components":[]}`로 갱신(이미 확인됨). `writeSchema`/`readSchema` 로직 자체는 불변(여전히 opaque `Map<String,Object>` 왕복).
- `application/dto/CatalogItemDetailResponse.java`·`CreateCatalogItemRequest.java`·`UpdateCatalogItemRequest.java`의 `formSchema` `@Schema(description=...)` 문구를 "자체 8×n 그리드 스키마({components})"로 갱신(이미 확인됨).
- `common/form/CLAUDE.md`·`srm/application/CLAUDE.md` 등 관련 CLAUDE.md 문서에서 form.io 언급 정리.
- 테스트: `source/backend/src/test/java/com/itsm/common/form/FormSubmissionValidatorTest.java`를 새 검증 로직(required/regex만) 기준으로 갱신, `ServiceCatalogServiceTest`/`ServiceRequestServiceTest`/`SrmApprovalIntegrationTest`의 formSchema 테스트 픽스처를 그리드 컴포넌트 객체 형태(`{key,type,label,position,size,validation:{required,regex}}`)로 갱신.
- `build.gradle`에 `@formio` 관련 BE 의존성은 원래 없었으므로(FE 전용 라이브러리) 변경 불필요.

#### FE (dev-fe) — `source/frontend/src/components/common/`, `source/frontend/src/features/service-request/`

**공용 컴포넌트 전면 재작성(form.io 완전 제거)**
- `form-schema.ts`: 기존 `FormIoSchema`/`FormIoSubmissionData`/`FORM_BUILDER_OPTIONS`(form.io 전용)만 삭제. 신규 타입 정의: `GridComponentType = "text"|"textarea"|"select"|"radio"|"checkbox"|"date"|"file"`, `GridPosition{col:number; row:number}`, `GridSize{w:number; h:number}`, `GridComponent{key; type; label; position; size; labelAlign?; input?:{widthPercent?; align?; defaultValue?; readOnly?}; validation?:{required?; regex?}; options?:string; ciLinked?:boolean}`, `GridFormSchema{components: GridComponent[]}`, `GridFormValues = Record<string, unknown>`.
  > **정정(2026-07-18)**: 계획 초안에 "레거시 구획(FormFieldSchema 등)은 field-builder.tsx/dynamic-form.tsx 삭제로 이미 죽은 코드"라고 적었으나 오류였다 — 두 파일 다 여전히 존재하고 **ESM**(`EsmCatalogManagePage.tsx`/`DeptRequestSubmitPage.tsx`)이 계속 사용 중이다(설계도 "ESM은 레거시 EAV 그대로 사용, 대상 아님" 명시). 레거시 구획·`field-builder.tsx`·`dynamic-form.tsx`는 ESM용으로 그대로 유지, 삭제하지 않는다(dev-fe 발견).
- `dynamic-form-builder.tsx`: `@formio/react` `FormBuilder` 임포트·래핑 제거. 신규 자체 그리드 캔버스 컴포넌트로 전면 재작성 — 좌측 팔레트(7종 아이콘+라벨, 클릭 또는 드래그로 캔버스에 추가) / 우측 8칸 그리드 캔버스(스크롤 가능, `initialSchema`로 편집 모드 진입). 드래그앤드롭·리사이즈는 신규 빌드 의존성 없이 구현 가능하면 네이티브 pointer 이벤트로, 필요하면 이미 `package.json`에 있는 라이브러리 우선 검토(신규 의존성 추가는 최소화 — 정말 필요하면 dev-lead에게 확인 후 추가). 겹침 배치 시 인라인 오류 안내("이미 배치된 컴포넌트와 겹칩니다"). 컴포넌트 hover 시 설정 버튼 노출 → 클릭 시 Content 설정 미니 팝업(5.4절 항목 그대로: label 정렬, input 폭%/정렬/default/읽기전용/필수, validation regex, select/radio/checkbox는 콤마 옵션+CI연계 라디오(비활성 자리)). 하단 적용/취소 버튼 — 적용 시 `onChange`(또는 `onApply`)로 최신 그리드 스키마를 상위에 전달(자동저장 없음).
- `dynamic-form-renderer.tsx`: `@formio/react` `Form` 임포트·래핑 제거. 신규 자체 렌더러로 재작성 — `schema.components`를 `position`/`size` 그대로 CSS Grid(8열)로 배치 렌더링, 각 컴포넌트 타입별 입력 요소 렌더(select/radio/checkbox는 `options`(콤마 분리) 파싱, 옵션 많으면 자동 줄바꿈+셀 내부 스크롤). 클라이언트 검증은 `validation.regex`(정규식 매칭)만 수행(기존 minLength/maxLength/min/max 개념 없음). 기존 하단 제출/취소 버튼 푸터 패턴은 유지(SCR-SRM-002 "하단 제출 버튼" 요건 그대로).
- `index.ts`: `FormIoSchema`/`FORM_BUILDER_OPTIONS` 등 form.io 관련 export 제거, 신규 `GridFormSchema`/`GridComponent`/`GridFormValues` 등 export로 교체.
- `source/frontend/src/index.css`: `.formio-scope`/`.formio-dialog`/`.formio-render-scope` 관련 규칙(8.3절 스코핑 CSS 전체 — line 423 이후 새로 추가됐던 블록) 완전 삭제. 그리드 캔버스·팝업에 필요한 스타일은 기존 ADS 토큰·Tailwind 유틸리티로 직접 작성(신규 전역 CSS 오버라이드 최소화).
- `package.json`/`package-lock.json`: `@formio/js`·`@formio/react` 의존성 제거(`npm uninstall` 또는 수동 삭제 후 `npm install`로 lock 갱신).

**기능 화면**
- `CatalogManagePage.tsx`(SCR-SRM-007): 기존 인라인 `DynamicFormBuilder` 임베드를 "Form 설정" 버튼(팝업 오픈)+그 아래 pre-view 축소판으로 교체. 팝업은 공용 `Modal`(또는 신규 전용 다이얼로그, 캔버스가 커서 기존 `Modal` 크기로 부족하면 dev-lead와 상의) 안에 `DynamicFormBuilder` 렌더, 하단 적용/취소는 팝업 자체가 담당(카탈로그 항목 폼의 "저장"과는 별개 — 적용 시 로컬 상태만 갱신). pre-view는 저장된/적용된 스키마를 읽기 전용 축소 렌더로 표시, 클릭 시 팝업 재오픈.
- `RequestSubmitPage.tsx`(SCR-SRM-002): `DynamicForm(Io)Renderer` 교체만 반영(그리드 스키마 그대로 주입), 기존 지식 추천 패널·제출 성공 토스트·상세 이동 로직은 변경 없음.
- `types.ts`(`features/service-request/`): `FormIoSchema` 재export 부분을 `GridFormSchema`(from `components/common`)로 교체.

**참고**: UI 역할 소집 없이 이번에도 FE가 공용 컴포넌트까지 전담한다(20260718-070801 전례와 동일).

### 진행 순서

1. DB(리셋 SQL) → BE(재검증기 리라이트, DTO 문구) → FE(공용 컴포넌트 전면 재작성 → 기능 화면 연동) — DB/BE는 이미 상당 부분 진행됨, FE 착수.
2. FE는 공용 컴포넌트(그리드 빌더/렌더러) 완료 후 기능 화면(CatalogManagePage/RequestSubmitPage) 연동.

### 완료(테스트 통과) 기준

- 전 코드베이스에 `@formio` 관련 의존성·import·CSS가 남아있지 않음(주석 처리 아님, 완전 삭제).
- FE: SCR-SRM-007 "Form 설정" 팝업에서 8칸 그리드에 7종 컴포넌트를 배치·리사이즈(1~2칸, textarea 높이 제약 없음)·겹침 방지·Content 설정(label 정렬/input 폭·정렬·default·읽기전용·필수/정규식, 옵션 콤마 입력)까지 가능하고 pre-view가 정상 표시됨. SCR-SRM-002에서 그 그리드가 그대로 렌더링되고 정규식 클라이언트 검증 후 제출 가능.
- BE: 요청 제출 시 `FormSubmissionValidator`가 `required`/`regex` 위반만 400으로 거부(구 minLength/maxLength/min/max 검증 없음), 정상 제출은 `form_values`에 그대로 저장.
- DB: 기존 카탈로그 항목의 `form_schema`가 배포 후 빈 그리드(`{"components":[]}`)로 리셋되어 있음.
- tester 통합 테스트 후 dev-lead에 결과 보고 → 실패 0까지 수정 루프 → Standards/Spec 코드 리뷰 → 완료 시 커밋(main).

## 개발 계획 — 2026-07-18 유지보수: 그리드 폼 빌더 세부 개선(label 컴포넌트·date/file 아이콘화·순차 단일 오류)

- 요구사항: (1) 입력 컴포넌트 7종에서 `label`/`labelAlign` 속성을 완전히 제거하고, 팔레트에 값 입력이 없는 정적 텍스트 전용 8번째 컴포넌트 `label`(text/textAlign만 가짐)을 추가한다 — 라벨이 필요하면 관리자가 별도 셀에 `label` 컴포넌트를 배치(입력 요소와 `for`/`aria-label` 연결 없음, 순수 인접 배치). (2) `date`/`file`은 브라우저 기본 input을 그대로 노출하지 않고 아이콘(캘린더/파일)만 표시, 클릭 시 숨겨진 네이티브 input을 트리거해 피커/파일선택 다이얼로그를 열며 선택값은 아이콘 옆 작은 텍스트로 표시. (3) 폼 유효성 오류 표시를 필드별 인라인에서 "제출 클릭 시 `components` 배열 순서상 첫 번째 위반 컴포넌트 오류 1건만 폼 하단에 표시"로 전환(여러 오류 동시 표시 금지, 재제출 시 처음부터 재검사). 서버(`FormSubmissionValidator`)도 동일하게 "첫 위반 즉시 400" 계약을 유지(이미 그렇게 동작 중일 가능성 높음 — 검증 후 그대로면 변경 불필요, 아니면 조정).
- 설계 근거: `docs/02_plan/screen/service-request.md` 5.3절(팔레트 8종)/5.4절(label 컴포넌트·date/file 아이콘 렌더링)/5.5절(신규, 유효성 검증·오류 표시)/5.6절(pre-view, 절 번호 이동)/5.7절(기존 데이터 처리, 절 번호 이동), SCR-SRM-002(4절, 오류 표시 UX), `docs/02_plan/api_spec/service-request.md` API-SRM-002(label 타입 JSON 스키마, 입력 타입에서 label/labelAlign 제거) 및 변경이력(API-SRM-006 400 응답 첫 위반 1건만), `docs/02_plan/api_spec/common.md` 0-2절(검증 절차 재정의 — 순차 첫 위반 즉시 반환, label 제외), `docs/02_plan/database/service-request.md`(컬럼 타입/기본값 변경 없음, 내부 JSON 구조만 조정 — **DB 마이그레이션 불필요**).
- 참고 기존 코드: `source/frontend/src/components/common/form-schema.ts`·`dynamic-form-builder.tsx`·`dynamic-form-renderer.tsx`, `source/backend/src/main/java/com/itsm/common/form/FormSubmissionValidator.java`.

### 담당 범위

#### DB

- 변경 없음(컬럼 타입·기본값 그대로, JSON 내부 구조만 바뀌고 이미 전 로우가 빈 그리드로 리셋된 상태라 백필 대상 데이터 없음). dev-db 소집 불필요.

#### BE (dev-be) — `source/backend/src/main/java/com/itsm/common/form/FormSubmissionValidator.java`

- **먼저 확인**: 현재 구현이 이미 `components` 배열을 순서대로 순회하며 각 컴포넌트에서 위반 발견 시 즉시 예외를 던지는 구조라, "첫 위반 즉시 400" 계약을 이미 만족할 가능성이 높다(순회 중 첫 실패 지점에서 즉시 throw → 뒤 컴포넌트는 검사되지 않음). 실제로 그런지 코드로 재확인해라.
- **`label` 타입 제외**: `type=label` 컴포넌트는 검증 대상에서 제외해야 한다(애초에 `validation` 필드가 없을 것이므로 현재 로직대로도 통과하겠지만, 명시적으로 `component.get("type")`이 `"label"`이면 continue하는 방어적 처리를 추가해라 — 스펙에 명시된 항목이라 명확성 차원).
- 위 확인·필요 시 방어적 처리 추가 후 `FormSubmissionValidatorTest.java`에 label 타입 컴포넌트가 섞인 케이스(검증 스킵 확인)와 다중 위반 중 배열상 첫 번째만 반환되는지 확인하는 케이스를 추가해라.
- 변경이 실질적으로 없다고 판단되면(이미 충족) 그 근거를 dev-lead에게 보고만 해도 된다.

#### FE (dev-fe) — `source/frontend/src/components/common/`

**`form-schema.ts`**
- `GridComponentType`에 `"label"` 추가(8종), `GRID_PALETTE_TYPES`에 순서상 마지막 추가.
- 기존 `GridComponent` 인터페이스를 판별 유니온으로 재정의: 7개 입력 타입(text/textarea/select/radio/checkbox/date/file)은 `label`/`labelAlign` 필드를 제거하고 기존 `input`/`validation`/`options`/`ciLinked` 그대로 유지하는 `GridInputComponent`, `label` 타입은 `text: string`/`textAlign?: GridAlign`만 갖는 `GridLabelComponent`로 분리. `GridComponent = GridInputComponent | GridLabelComponent`(export 이름은 유지해 하위 호환).
- `hasGridOptions`/`gridMaxHeight`가 `"label"`도 다뤄야 한다 — `gridMaxHeight("label")`은 다른 비-textarea 타입과 동일하게 2(제약 있음, textarea만 예외).

**`dynamic-form-builder.tsx`**
- 팔레트에 `label`(8번째, 아이콘은 `Type`/`AlignLeft` 등 텍스트 계열에서 겹치지 않는 걸로 dev-fe 재량) 추가. `PALETTE_LABELS`/`PALETTE_ICONS`/`defaultSize`(1×1 기본, 다른 비-textarea 타입과 동일)에 항목 추가.
- `handleAddComponent`: `type === "label"`이면 `GridLabelComponent` 형태(`text: "텍스트"`, `textAlign: "left"`)로 생성, 그 외는 기존 `GridInputComponent` 형태 그대로(단 `label`/`labelAlign` 필드는 더 이상 넣지 않는다).
- `ComponentSettingsPopover`: `component.type === "label"`이면 "표시 텍스트" Input + 정렬 토글(`AlignToggle`, 좌/가운데/우)만 노출하고 나머지(input 폭/정렬/default/읽기전용/필수/정규식/옵션)는 렌더하지 않는다. 그 외 7종은 기존 항목 그대로 유지하되 "라벨 텍스트"/"label 정렬" 필드는 제거한다(더 이상 해당 속성이 없으므로).
- `BuilderComponentCard`: `label` 타입 카드는 아이콘+`component.text`(라벨 텍스트 자체)를 표시(다른 카드가 `component.label` 대신 자기 `type` 라벨을 보여주던 것과 달리, label 컴포넌트는 실제 표시 텍스트를 카드에도 보여주는 게 직관적 — dev-fe 재량으로 UX 다듬어도 됨).

**`dynamic-form-renderer.tsx`**
- 기존 `GridFieldControl`이 렌더하던 `<label htmlFor=...>{component.label}...</label>` 캡션을 제거(입력 컴포넌트에 더 이상 `label` 속성이 없음).
- 신규 `type === "label"` 분기: 값 입력 없이 `component.text`를 `component.textAlign`에 따라 정렬한 정적 텍스트로 렌더링(셀 크기 그대로 차지).
- `date`/`file` 렌더링을 아이콘 전용으로 변경: 숨겨진 네이티브 `<input type="date"|"file">`(`ref`로 참조, `className="sr-only"` 등으로 시각적으로 숨김, 접근성 상 완전 제거는 아님)을 두고, 화면에는 아이콘 버튼만 노출 — 클릭 시 `inputRef.current?.showPicker?.()`(date, 미지원 브라우저 폴백으로 `inputRef.current?.click()`) 또는 `inputRef.current?.click()`(file)으로 네이티브 UI를 연다. 선택된 값(날짜 문자열/파일명)을 아이콘 옆 작은 텍스트로 표시.
- 유효성 검증·오류 표시 전면 변경: 기존 필드별 `errors: Record<string, string>` + 인라인 오류 문단을 제거하고, 단일 `formError: string | null` state로 교체. 제출 클릭 시 `schema.components`를 배열 순서대로 순회해 **첫 번째로 위반되는 컴포넌트**를 찾으면(required 우선, 그다음 regex — 기존 컴포넌트 내부 검사 순서 그대로) 그 오류 메시지 1건만 `formError`에 설정하고 제출 차단, 폼 하단(제출/취소 버튼 위)에 표시한다. 통과하면 `formError`를 비우고 `onSubmit(values)` 호출. `type=label`은 검증 대상에서 제외(값 자체가 없음).
- 서버 재검증 실패(400) 토스트 메시지도 이미 "첫 위반 1건"이라 별도 FE 변경 불필요(BE가 이미 그런 메시지 하나만 반환).

**연동 화면**: `CatalogManagePage.tsx`의 pre-view는 `DynamicFormRenderer`를 그대로 재사용하므로 렌더러 변경만으로 `label`/`date`/`file` 아이콘화가 자동 반영된다 — 별도 수정 불필요(다만 실제로 pre-view에서도 정상 보이는지 확인은 필요).

### 진행 순서

1. FE `form-schema.ts`(타입 재정의) → `dynamic-form-builder.tsx`/`dynamic-form-renderer.tsx`(병렬 가능, 타입 의존) 순.
2. BE는 FE와 독립적으로 확인·필요시 방어 코드 추가(병렬 진행 가능).

### 완료(테스트 통과) 기준

- 팔레트에 `label` 포함 8종 노출, `label` 컴포넌트는 표시 텍스트+정렬만 설정 가능하고 값 입력·필수·정규식 등이 없음.
- 나머지 7종 Content 설정에 "라벨 텍스트"/"label 정렬" 항목이 없음(별도 `label` 컴포넌트로만 캡션 구현).
- `date`/`file` 필드가 요청 제출 화면·pre-view에서 아이콘 전용으로 표시되고, 클릭 시 네이티브 피커/파일선택이 정상 동작하며 선택값이 아이콘 옆에 표시됨.
- 요청 제출 화면에서 여러 필드가 동시에 위반이어도 폼 하단에 오류 메시지가 항상 1건만 표시되고, 수정 후 재제출 시 다음 위반이 순서대로 표시됨(배열 순서 기준, 그리드 시각적 위치 아님).
- 서버 400 응답도 첫 위반 1건만 반환(다건 동시 위반 시나리오로 확인).
- 기존 SRM 회귀 없음(팔레트 7종 기능·겹침 방지·리사이즈·pre-view 라운드트립 등 이전 통합테스트 항목 재확인).
- tester 통합 테스트 후 dev-lead에 결과 보고 → 실패 0까지 수정 루프 → Standards/Spec 코드 리뷰 → 완료 시 커밋(main).
