# 개발 계획 — esm (엔터프라이즈 서비스 관리, ESM)

> 도메인: esm · 개발 순서 1/4(신규 확장 도메인) · 작성: dev-lead · 2026-07-10

## 1. 목표

부서별(HR/법무/시설/재무, IT 자체는 기존 SRM 유지) 서비스 카탈로그·요청 처리, HR 케이스 관리(HR 전용 민감정보), 온보딩/오프보딩 체크리스트(부서 간 하위 작업 오케스트레이션), ESM 지표 대시보드를 구현한다. auth(app_user.department 추가)·asset(오프보딩 자산 회수 연계)·common(comment/timeline_event 재사용) 기반. 확장 도메인 4개 중 첫 번째로 진행한다.

## 2. 설계 근거 (docs/02_plan)

- 화면: `screen/esm.md`(SCR-ESM-001~011), 공통 SCR-COM-007/008
- API: `api_spec/esm.md`(API-ESM-001~017), 참고 `api_spec/asset.md`(API-ITAM-001, 오프보딩 자산 조회)
- DB: `database/esm.md`(esm_catalog_item/esm_catalog_form_field/esm_checklist_template_task/esm_request/esm_request_form_value/esm_hr_case/esm_checklist/esm_checklist_task) + `database/common.md`(comment/timeline_event) + `database/auth.md`(app_user.department 컬럼 추가)
- 역할: `security/authorization/hr_case_manager.md`(HR_CASE_MANAGER, 신규), `security/authorization/dept_coordinator.md`(DEPT_COORDINATOR, 신규), `process_owner.md`(카탈로그 관리 갱신), `end_user.md`(부서 요청 제출·추적 갱신)

## 3. 담당별 범위

### DB (dev-database) — `source/db/`
- 신규 테이블 8개: `esm_catalog_item`(department·checklist_template_type), `esm_catalog_form_field`(EAV 스키마 정의, UNIQUE(catalog_item_id, field_key)), `esm_checklist_template_task`, `esm_request`(ticket_key ESM-YYYY-####, requester/assignee FK, department 비정규화 복제, checklist_id FK UNIQUE nullable), `esm_request_form_value`(EAV, UNIQUE(esm_request_id, field_key)), `esm_hr_case`(민감정보), `esm_checklist`(type/target_user_name/status), `esm_checklist_task`(department/status/related_asset_id FK→asset.id nullable). 공통 컬럼 규칙은 auth.md 2절과 동일.
- **auth 도메인 증분**: `app_user`에 `department` 컬럼 추가(VARCHAR(20), NULL 허용, HR/LEGAL/FACILITIES/FINANCE/IT). 기존 마이그레이션에 영향 없는 순수 컬럼 추가.
- 신규 역할 시드: `HR_CASE_MANAGER`, `DEPT_COORDINATOR`를 `role` 테이블에 추가. screen/screen_role 증분: SCR-ESM-001~011 등록 + 역할 매핑(esm.md 화면표, hr_case_manager.md/dept_coordinator.md 기준).
- 테스트 유저 시드: HR_CASE_MANAGER(예: hr@itsm.local), DEPT_COORDINATOR는 부서별로 최소 2개 이상 시드 권장(예: legal-coord@itsm.local(department=LEGAL), facilities-coord@itsm.local(department=FACILITIES)) — 부서별 접근 제어(department 일치 검증)를 테스트하려면 서로 다른 부서 계정이 필요.
- PROCESS_OWNER 계정은 이미 존재하면 재사용(카탈로그 관리 권한 추가는 애플리케이션 레벨, 테이블 변경 없음).
- 카탈로그 시드 데이터: 부서별(HR/LEGAL/FACILITIES/FINANCE) 최소 1건씩, 그중 온보딩·오프보딩 각 1건은 `checklist_template_type` 지정 + `esm_checklist_template_task` 2건 이상(서로 다른 부서 배정) 포함 권장(FE/BE 통합테스트에 필요).

### BE (dev-backend) — `source/backend/`
- API-ESM-001~017(api_spec 기준). `esm` 패키지 신설(DDD 4계층, srm 패키지 컨벤션 최대 재사용 — 카탈로그+동적양식+EAV 구조가 SRM과 거의 동일).
- **공통 enum 증분**: `common.ticket.TicketType`에 `ESM_REQUEST`, `HR_CASE` 추가(comment/timeline_event 재사용을 위해 필수, 기존 값 순서·값 변경 없이 추가만).
- 카탈로그 생성/수정(API-ESM-003/004): PROCESS_OWNER 전용(403), department 누락 400. checklistTemplateType이 ONBOARDING/OFFBOARDING인데 template이 비어 있어도 저장은 허용(설계 명시: "저장은 허용하되 경고" — API 응답 자체는 200/201, 경고는 FE 표시 몫이므로 BE는 별도 필드 없이 그대로 저장). 실제 제출 시점(API-ESM-005)에만 400 거부.
- 부서 요청 제출(API-ESM-005): catalogItemId 필수, formValues는 카탈로그의 formSchema 기준 필수 필드 검증(400). checklistTemplateType이 ONBOARDING/OFFBOARDING이면: (1) template 비어있으면 400, (2) 아니면 esm_checklist + esm_checklist_task(템플릿 복제) 생성, targetUserName 필수(없으면 400), (3) OFFBOARDING이면 `assetApi` 동등 리포지토리로 `GET .../assets?owner={targetUserName}` 상당 조회(같은 애플리케이션 내부 호출이므로 AssetRepository 직접 재사용 권장, HTTP 재호출 아님)해 자산별 회수 하위 작업 자동 추가(자산 없으면 스킵, 에러 아님).
- 부서 요청 목록(API-ESM-006): `scope=mine`(요청자 본인) / `scope=all&department=`(DEPT_COORDINATOR, 본인 department와 쿼리 department 불일치 시 403 — 정확히는 로그인 사용자의 department로 강제하고 쿼리 department 파라미터는 참고용으로만 취급하거나 무시 권장, 모호하면 저에게 질문).
- 상태 전이(API-ESM-008): SUBMITTED→IN_PROGRESS/COMPLETED/REJECTED. DEPT_COORDINATOR의 department가 요청의 department와 일치해야 처리 가능(403). 정의되지 않은 대상 상태 400.
- HR 케이스(API-ESM-010~013): HR_CASE_MANAGER 전용, 다른 역할(SYSTEM_ADMIN 포함) 접근 시 403(설계 명시, 우선순위 규칙 그대로 구현). 상태 전이는 INTAKE→DOCUMENTATION→INVESTIGATION→RESOLUTION 순서만 허용(400).
- 체크리스트(API-ESM-014): 연계 요청 접근 권한자 또는 하위 작업 담당 부서 소속만 조회(403).
- 내 하위 작업(API-ESM-015)/완료 처리(API-ESM-016): 로그인 사용자의 `department`와 하위 작업의 `department` 일치 검증. 완료 처리 시 해당 체크리스트의 모든 하위 작업이 DONE이면 `esm_checklist.status`를 COMPLETED로 자동 갱신, 응답에 `checklistStatus` 포함.
- ESM 지표(API-ESM-017): requestCount/avgProcessingMinutes(생성~완료 상태전이 시각차 평균)/onboardingCompletionRate/offboardingCompletionRate(완료 체크리스트 수 / 전체 체크리스트 수, 유형별). 데이터 없으면 0.
- RBAC 표는 hr_case_manager.md/dept_coordinator.md/process_owner.md/end_user.md 기준으로 구현. `SecurityUtils` 패턴 재사용.
- JUnit 통합테스트(기존 도메인 패턴 재사용) — 특히 department 불일치 403, HR 케이스 타 역할 403, 체크리스트 자동완료, 오프보딩 자산 회수 자동생성 케이스 포함.

### UI (dev-ui) — `source/frontend/` 공통 영역
- 이번 도메인은 기존 공통 컴포넌트(목록/상세 패턴, 동적 폼 빌더가 있다면 SRM 카탈로그 관리 화면에서 재사용)로 대부분 커버 가능해 보임. SCR-ESM-006(카탈로그 관리, 양식 필드 빌더+체크리스트 템플릿 빌더)에 신규 "체크리스트 템플릿 빌더" 반복입력 UI가 필요하면, 기존 SRM 카탈로그 관리 화면의 동적 필드 빌더 패턴을 그대로 재사용하는 선에서 FE와 상의(신규 공통 컴포넌트 도입은 최소화).
- 신규 요소 발생 시(예: 부서 탭 UI) 최대한 기존 `components/ui`(Tabs 등) 재사용.

### FE (dev-frontend) — `source/frontend/` 기능 영역
- 신규 기능 모듈 `features/esm/`(api.ts/types.ts/status.ts/format.ts + 11개 Page 컴포넌트).
- SCR-ESM-001 부서 서비스 포털(부서 탭+카탈로그 카드), SCR-ESM-002 동적 양식 제출(SRM 카탈로그 제출 폼 패턴 재사용), SCR-ESM-003 내 요청 목록, SCR-ESM-004 처리 큐(부서 필터), SCR-ESM-005 상세(상태 전이·코멘트·연계 체크리스트 카드), SCR-ESM-006 카탈로그 관리(양식 빌더+체크리스트 템플릿 빌더), SCR-ESM-007/008 HR 케이스 목록/상세(4단계 순차 전이), SCR-ESM-009 체크리스트 상세, SCR-ESM-010 내 하위 작업 목록, SCR-ESM-011 지표 대시보드.
- 역할 상수 추가(`features/auth/roles.ts`): `ROLE_HR_CASE_MANAGER`, `ROLE_DEPT_COORDINATOR`. `navConfig.tsx`에 ESM 메뉴 그룹 추가(부서 요청은 END_USER/DEPT_COORDINATOR/PROCESS_OWNER, HR 케이스는 HR_CASE_MANAGER 전용 별도 그룹 권장 — 민감정보 특성상 사이드바에서도 구분 노출).
- `routes/index.tsx`에 SCR-ESM-001~011 라우팅 추가(기존 패턴 재사용, RequireRoles 가드 적용).

## 4. 진행 순서 · 의존성
1. DB(테이블 8개 + auth.app_user.department 컬럼 + 역할/유저 시드 + 카탈로그 시드) → BE(TicketType enum 증분 먼저 → esm 패키지) → FE 연동. UI는 최소(기존 컴포넌트 재사용 위주).
2. 계약 단일 기준 `api_spec/esm.md`. 모호점(예: 부서 요청 목록의 department 쿼리 파라미터와 로그인 사용자 department 우선순위, 카탈로그 저장 시 경고 표현 방식)은 저에게 질문 → 제가 판단 못 하면 designer에게 확인.

## 5. 완료(테스트 통과) 기준
- BE: API-ESM-001~017 정상+오류(400/401/403/404), department 기반 접근 제어(요청 처리·하위 작업), HR 케이스 타 역할 차단, 체크리스트 자동생성(온보딩/오프보딩)·자동완료, 오프보딩 자산 회수 하위 작업 자동추가, HR 케이스 4단계 순차 전이 검증.
- FE: 카탈로그 조회→제출→내 요청 목록→처리 큐→상세(상태전이·코멘트)→체크리스트 상세→내 하위작업 완료, HR 케이스 접수→목록→상세→전이, 지표 대시보드 E2E. 역할별 RBAC(HR_CASE_MANAGER/DEPT_COORDINATOR/PROCESS_OWNER/END_USER) 확인.
- `tester` 통합테스트 실패 0 → `feat(esm): ...` 커밋/푸시.

## 6. 파일 소유
- `source/db/` DB / `source/backend/`(esm 패키지 + common.ticket.TicketType 증분) BE / `source/frontend/` 공통 UI·기능 FE.

## 7. 특이사항
- `app_user.department`는 auth 도메인 테이블 증분이라 DB가 담당하되, BE의 auth 관련 조회/응답(예: `MeResponse`)에 department 노출이 필요한지는 설계에 명시 없음 — 필요 여부(FE가 로그인 사용자 department를 알아야 "내 하위 작업" 등에서 필터 UI를 구성할지) 확인 필요하면 저에게 질문.
- 온보딩/오프보딩 체크리스트 생성은 서비스 요청(SRM) 도메인이 아닌 ESM 자체 로직이며, 자산 조회만 asset 도메인을 내부적으로 참조한다(HTTP 재호출이 아니라 동일 애플리케이션 내 리포지토리 재사용을 권장 — BE 재량이나 방식 확정되면 이 문서에 기록).

## i18n 다국어 전환 (유지보수 요청, 2026-07-12)

> i18n 인프라·SweetAlert2·언어 선택은 common phase에서 완료됨(`docs/03_develop/plan/common.md` v3절). 레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환(`docs/02_plan/screen/common.md` 6절). BE/DB 변경 없음.

### 담당 범위 — dev-fe 단독(UI 미소집)

- 대상 화면(`docs/02_plan/screen/esm.md` 3절, 11개): `DeptPortalPage.tsx`(SCR-ESM-001), `DeptRequestSubmitPage.tsx`(002), `MyEsmRequestsPage.tsx`(003), `EsmRequestQueuePage.tsx`(004), `EsmRequestDetailPage.tsx`(005), `EsmCatalogManagePage.tsx`(006), `HrCaseListPage.tsx`(007), `HrCaseDetailPage.tsx`(008), `ChecklistDetailPage.tsx`(009), `MyChecklistTasksPage.tsx`(010), `EsmMetricsPage.tsx`(011).
- `features/esm/status.ts` — `t` 인자를 받도록 전환, 호출부 갱신. esm은 통합검색 대상 아님(`features/search/status.ts` 변경 불필요, asset과 동일하게 확인만 하면 됨).
- **`DeptRequestSubmitPage.tsx`의 `validateForm(schema, values)` 호출에 세 번째 인자로 `t`를 추가**해라(`validateForm(schema, values, t)`) — service-request phase에서 이미 `form-schema.ts`에 선택적 `t` 인자가 추가돼 있고, esm은 그때 미루기로 했던 도메인이다(필수 항목 오류 메시지 번역 적용).
- `format.ts` 확인 필수 — 라벨 섞여 있으면 전환.
- `useTranslation(["esm", "common"])` 사용. `locales/{ko,en}/esm.json`(현재 `{}` 스캐폴딩) 단독 소유, 직접 채운다.
- 화면 수가 많으니(11개) change/asset phase에서 반복 발견된 "열거형 원시값 나열 노출"·"값 없을 때 원시 키 노출" 두 패턴을 특히 유의해서 점검해라(체크리스트 하위 작업 상태, HR 케이스 4단계 상태, 부서명 등).

### 완료 기준
- English 전환 시 11개 화면 전체 텍스트(부서·상태·체크리스트 라벨 포함) 영어 전환.
- 부서 요청 제출/처리/체크리스트 완료·HR 케이스 상태 전이 등 기존 기능 회귀 없음(텍스트만 치환).

## 승인 대상자 역할 기반 동적 상세조회 권한 — ESM 부분 (유지보수 요청, 2026-07-15)

> 8개 도메인 공용 작업. 전체 설계·담당범위·완료기준은 `docs/03_develop/plan/common.md` 동일 제목 절 참조. 이 도메인 BE 작업: `esm/application/EsmRequestService.java` `assertCanView`(요청자 본인+DEPT_COORDINATOR)에 `approvalGateService.canApproverView("ESM", null, esmRequest.getRequesterId())` OR 추가(신규 권한, 기존 조건은 유지). FE 라우트 가드(`routes/index.tsx`)는 공용 작업에 포함되어 별도 진행 불필요.

## 전이 버튼 라벨·타임라인 actor·textarea 필드 (유지보수 요청, 2026-07-16)

### 설계 근거
- 화면: `docs/02_plan/screen/esm.md` v0.4 SCR-ESM-005(부서 요청 전이 라벨표 122~128행, 타임라인 actor)·SCR-ESM-008(HR 케이스 전이 라벨표 174~180행 — HR 케이스 상태 이력은 기존부터 `changedBy`를 표시 중이라 actor 작업 대상 아님).
- API: `docs/02_plan/api_spec/esm.md`(부서 요청 상세 응답 `timeline[].actor`, `formSchema.type`에 `textarea`).
- 공통 아키텍처: `docs/03_develop/plan/common.md` "상태 전이 버튼 라벨·타임라인 actor 공통 아키텍처" 절.
- **textarea DB 마이그레이션은 SRM phase에서 이미 함께 처리됨**(`docs/03_develop/plan/service-request.md` "카탈로그 카테고리 CRUD·textarea" 절의 `35_catalog_form_field_textarea_type.sql`이 `esm_catalog_form_field`도 포함). **FE 공통 컴포넌트(`form-schema.ts`/`dynamic-form.tsx`/`field-builder.tsx`)도 같은 phase에서 이미 처리됨**(SRM `CatalogManagePage.tsx`와 ESM `EsmCatalogManagePage.tsx`가 동일한 `FieldBuilder`를 재사용하므로 중복 작업 불필요) — 이 도메인에서는 별도 textarea 작업 없음(BE `FormFieldDto.java` 설명 문구만 아래 반영).
- 참고 기존 코드: `source/backend/.../esm/domain/EsmRequestStatus.java`, `application/EsmRequestService.java`, `application/dto/RequestDetailResponse.java`·`FormFieldDto.java`; `source/frontend/.../esm/status.ts`·`types.ts`·`EsmRequestDetailPage.tsx`·`HrCaseDetailPage.tsx`.

### 담당 범위

#### BE (dev-backend) — `source/backend/src/main/java/com/itsm/esm/`
- `domain/EsmRequestStatus.java`에 `label()` 메서드 추가(FE `features/esm/status.ts`의 `REQUEST_STATUS_LABEL`과 동일 값 4종). **`HrCaseStatus.java`는 대상 아님**(actor 작업 범위 밖 — 라벨 매핑은 FE에서만 처리, 아래 참고).
- `application/dto/RequestDetailResponse.TimelineEntry`(현재 `(String type, String message, OffsetDateTime at)`)에 `actor` 필드 추가.
- `application/EsmRequestService.java`의 상세 조회 메서드: 타임라인 조회 시 각 `TimelineEvent.getCreatedBy()`(email)로 `appUserRepository.findByEmail()` 조회해 이름 resolve(실패 시 email 폴백)해 `actor`에 채움(요청자명 조회에 쓰는 `requesterId` 기반 조회와는 별개 — 타임라인 actor는 항상 `TimelineEvent.createdBy` 기준).
- `EsmRequestService.java`의 상태 전이 메서드에서 `TimelineEvent.of(TT, id, "STATUS_" + target.name(), ... "상태가 " + target.name() + "로 변경되었습니다.")` 메시지 부분의 `target.name()` → `target.label()`로 교체(이벤트 타입 문자열은 유지).
- `application/dto/FormFieldDto.java`의 `@Schema` 설명 문구만 `text|select|number|date|file` → `text|textarea|select|number|date|file`로 갱신.

#### FE (dev-fe) — `source/frontend/src/features/esm/`
- `status.ts`에 `transitionLabel(t, target: EsmRequestStatus): string`(i18n 키 `esm:transition.*`, 매핑값 SCR-ESM-005 122~128행 표) + `hrCaseTransitionLabel(t, target: HrCaseTargetStatus): string`(i18n 키 `esm:hrCaseTransition.*`, 매핑값 SCR-ESM-008 174~180행 표) 신규 추가. 기존 `requestStatusLabel`/`hrCaseStatusLabel`은 변경하지 않음.
- `EsmRequestDetailPage.tsx`의 전이 버튼 텍스트만 `requestStatusLabel` → `transitionLabel`로 교체(토스트는 기존 유지).
- `HrCaseDetailPage.tsx`의 전이 버튼(다음 단계 버튼 1개) 텍스트만 `hrCaseStatusLabel` → `hrCaseTransitionLabel`로 교체(토스트는 기존 유지).
- `types.ts`의 부서 요청 `timeline` 항목 타입에 `actor: string` 추가.
- `EsmRequestDetailPage.tsx`의 타임라인 매핑에 `actor: entry.actor` 추가. **`HrCaseDetailPage.tsx`는 변경 없음**(이미 `changedBy`를 별도 필드로 표시 중).

### 완료 기준
- 부서 요청 상세(SCR-ESM-005)의 전이 버튼에 동작 동사형 라벨, 타임라인에 행위 수행자 이름과 한글 상태 라벨이 표시된다.

## 개발 계획 — 2026-07-17 유지보수: 서비스 카탈로그 커스텀 폼 빌더(form.io)

- 설계 근거: `docs/02_plan/database/esm.md` v0.3(SRM `service-request.md`와 동일 패턴), `docs/02_plan/api_spec/esm.md` v0.4 API-ESM-002/003/004/005, `docs/02_plan/api_spec/common.md` 0-2절, `docs/02_plan/screen/esm.md` SCR-ESM-002/006, `docs/02_plan/screen/common.md` 8절.
- **SRM phase에서 이미 만들어진 산출물을 재사용**(신규 작업 아님): 공용 컴포넌트 `dynamic-form-builder.tsx`/`dynamic-form-renderer.tsx`/`form-schema.ts`(`docs/03_develop/plan/common.md` 참조), 공용 `common.form.FormSubmissionValidator`. SRM phase의 dev-ui/dev-be 작업이 완료된 뒤 이 ESM phase를 시작한다.
- 참고 기존 코드: `source/backend/.../esm/domain/EsmCatalogFormField.java`·`EsmCatalogItem.java`·`EsmRequestFormValue.java`·`EsmRequest.java`, `application/EsmCatalogService.java`·`EsmRequestService.java`·`dto/FormFieldDto.java`(SRM과 별도 파일); `source/db/sql/16_esm_schema.sql`; `source/frontend/.../esm/EsmCatalogManagePage.tsx`·`DeptRequestSubmitPage.tsx`·`api.ts`. SRM phase에서 처리된 `source/backend/.../srm/` 대응 파일들을 그대로 참조해 동일 패턴 적용(SRM `docs/03_develop/plan/service-request.md` "개발 계획 — 2026-07-17 유지보수" 절 참조).

### 담당 범위

#### DB (dev-database) — `source/db/sql/`

- 신규 파일 `37_esm_form_schema_jsonb.sql`(SRM phase의 `36_srm_form_schema_jsonb.sql` 다음 순번, 실제 생성 시점 최신 파일 다음 번호로 확인):
  1. `esm_catalog_item`에 `form_schema JSONB NOT NULL DEFAULT '{"display":"form","components":[]}'` 추가, 기존 `esm_catalog_form_field` 백필(SRM `catalog_form_field`와 동일 변환 규칙, `docs/02_plan/database/esm.md` 60~62행).
  2. `esm_request`에 `form_values JSONB NOT NULL DEFAULT '{}'` 추가, 기존 `esm_request_form_value` 백필(SRM과 동일 방식).
  3. `DROP TABLE esm_catalog_form_field;`, `DROP TABLE esm_request_form_value;`.
- `source/db/sql/CLAUDE.md` 갱신.

#### BE (dev-backend) — `source/backend/src/main/java/com/itsm/esm/`

- SRM phase와 동일 패턴: 카탈로그 항목 엔티티에 `formSchema`(JSONB, `String`+`@JdbcTypeCode(SqlTypes.JSON)`) 추가, `EsmCatalogFormField` 엔티티·리포지토리(도메인/리포지토리/인프라 3개 파일) 삭제. `esm_request` 엔티티에 `formValues`(JSONB) 추가, `EsmRequestFormValue` 엔티티·리포지토리 3개 파일 삭제.
- `FormFieldDto` 사용 DTO(카탈로그 상세/생성/수정 응답·요청)를 `Map<String,Object>` 기반으로 변경.
- `EsmCatalogService`(또는 대응 서비스): 필드 배열 저장 루프 제거, `formSchema` 원본 JSON 그대로 저장·조회.
- `EsmRequestService.java`: 요청 제출 시 `EsmRequestFormValue` 저장 루프 제거, `formValues` 그대로 저장. 제출 검증을 `common.form.FormSubmissionValidator` 호출로 교체(SRM과 동일 클래스 재사용, 신규 구현 없음).

#### FE (dev-fe) — `source/frontend/src/features/esm/`

- **SCR-ESM-006 `EsmCatalogManagePage.tsx`**: `FieldBuilder` 사용부를 `dynamic-form-builder.tsx`의 `DynamicFormBuilder`로 교체(SRM `CatalogManagePage.tsx`와 동일 통합 방식).
- **SCR-ESM-002 `DeptRequestSubmitPage.tsx`**: `DynamicForm` 사용부를 `dynamic-form-renderer.tsx`의 `DynamicFormRenderer`로 교체(SRM `RequestSubmitPage.tsx`와 동일 방식). 처음부터 신규 `onCancel`/`cancelLabel`/`submitLabel` prop을 사용해 제출/취소 버튼을 폼 우측 하단(취소→제출 순)에 배치한다(2026-07-17 추가 요구사항, `docs/03_develop/plan/common.md` "폼 렌더러 제출/취소 버튼 우측 하단 배치" 절 참조 — 화면 자체에 별도 취소 버튼을 만들지 않음).
- **`api.ts`**: `formSchema`/`formValues` 타입을 SRM과 동일하게 갱신.
- 이 phase가 SRM 화면 전환까지 모두 마친 마지막 소비자이므로, 전환 완료 확인 후 dev-ui에게 기존 `field-builder.tsx`/`dynamic-form.tsx` 삭제를 요청한다(`docs/03_develop/plan/common.md` UI 절 참조).

### 완료(테스트 통과) 기준

- DB: 마이그레이션 후 `esm_catalog_form_field`/`esm_request_form_value` 삭제, 데이터가 정상 백필됨.
- BE: 카탈로그 생성/수정 시 Form.io Form JSON 그대로 저장·조회, 부서 요청 제출 시 공용 `FormSubmissionValidator`로 서버 재검증, 온보딩/오프보딩 체크리스트 자동 생성 로직(기존 REQ-ESM-005/006)이 회귀 없이 동작.
- FE: SCR-ESM-006에서 자유배치 폼 설계·저장, SCR-ESM-002에서 렌더링·제출 정상 동작.
- tester 통합 테스트 후 dev-lead에 결과 보고 → 실패 0까지 수정 루프 → 완료 시 커밋(이 커밋에 SRM+ESM phase가 공유한 `field-builder.tsx`/`dynamic-form.tsx` 삭제도 포함).
- HR 케이스 상세(SCR-ESM-008)의 전이 버튼에 동작 동사형 라벨이 표시된다(타임라인은 기존과 동일, actor 추가 없음).

## 개발 계획 — 2026-07-19 유지보수: ESM 동적 양식을 SRM 그리드 폼 빌더로 전면 전환

- 요구사항: ESM의 레거시 EAV 기반 동적 양식(`esm_catalog_form_field`/`esm_request_form_value`, 반복 입력 필드 빌더 `field-builder.tsx`+렌더러 `dynamic-form.tsx`)을 폐기하고, SRM이 사용 중인 자체 8×n 그리드 드래그앤드롭 폼 빌더(공용 컴포넌트 `dynamic-form-builder.tsx`/`dynamic-form-renderer.tsx`/`form-schema.ts`, BE 공용 `com.itsm.common.form.FormSubmissionValidator`/`FormJsonMapper`)로 전면 전환한다. SCR-ESM-002(동적 필드 목록→그리드 렌더러), SCR-ESM-006(양식 필드 빌더→"Form 설정" 팝업)이 대상. ESM에는 없던 `number` 타입은 팔레트에 추가하지 않고 `text`+정규식으로 흡수. 기존 카탈로그 항목의 `form_schema`는 빈 그리드로 리셋(백필 불가, 사용자 결정), 기존 제출 데이터(`esm_request_form_value`)는 `form_values`로 백필.
- 설계 근거: `docs/02_plan/screen/esm.md`(변경이력 2026-07-19, SCR-ESM-002/006), `docs/02_plan/api_spec/esm.md`(변경이력 2026-07-19, API-ESM-002~005), `docs/02_plan/database/esm.md`(변경이력 2026-07-19, 4/6절), `docs/00_context/glossary.md`(동적 양식 스키마·폼 제출 데이터 항목이 "SRM 전용"에서 "SRM/ESM 공용"으로 갱신됨). maintainer 분석·designer 설계 완료(추가 질문 없음).
- 참고 기존 코드(SRM 이식 대상 패턴): `source/backend/.../srm/application/ServiceCatalogService.java`(`writeSchema`/`readSchema`, `FormJsonMapper` 사용)·`ServiceRequestService.java`(`FormSubmissionValidator.validate`, `writeValues`/`readValues`)·`domain/ServiceCatalogItem.java`(`formSchema` 필드), `source/frontend/.../service-request/CatalogManagePage.tsx`(Form 설정 팝업+A1 축소 미리보기)·`RequestSubmitPage.tsx`(`DynamicFormRenderer` 사용), `source/db/sql/36_srm_form_schema_jsonb.sql`·`38_srm_form_schema_reset.sql`(패턴 선례).

### 담당 범위

#### DB (dev-db) — `source/db/sql/` — **완료**

- `40_esm_form_schema_jsonb.sql`: `esm_catalog_item.form_schema`/`esm_request.form_values`(JSONB) 신규(SRM과 동일 기본값 `{"components":[],"labels":[]}`/`{}`), `esm_request_form_value` 백필 후 `esm_catalog_form_field`/`esm_request_form_value` DROP. `source/db/sql/CLAUDE.md` 반영 완료.

#### BE (dev-be) — `source/backend/src/main/java/com/itsm/esm/`

- **엔티티**: `domain/EsmCatalogItem.java`에 `formSchema`(String, `@JdbcTypeCode(SqlTypes.JSON)` + `columnDefinition = "jsonb"`, SRM `ServiceCatalogItem.formSchema`와 동일 패턴) 필드 추가(생성자·`update()`에도 반영). `domain/EsmRequest.java`에 `formValues`(동일 패턴) 필드 추가.
- **삭제 대상**: `domain/EsmCatalogFormField.java`, `domain/EsmRequestFormValue.java`, `domain/repository/EsmCatalogFormFieldRepository.java`, `domain/repository/EsmRequestFormValueRepository.java`, `infrastructure/persistence/EsmCatalogFormFieldJpaRepository.java`, `infrastructure/persistence/EsmRequestFormValueJpaRepository.java`, `application/dto/FormFieldDto.java`.
- **`application/EsmCatalogService.java`**: `EsmCatalogFormFieldRepository` 의존성 제거. `DEFAULT_FORM_SCHEMA = "{\"components\":[],\"labels\":[]}"` 상수 추가, `common.form.FormJsonMapper`로 `writeSchema`/`readSchema` 헬퍼 신설(SRM `ServiceCatalogService` 그대로). `create()`/`update()`의 `saveFields(...)` 호출 제거, `formSchema` 원본 JSON을 엔티티에 그대로 저장. `toDetail()`에서 `formFieldRepository` 조립 로직 제거하고 `readSchema(item.getFormSchema())` 그대로 반환.
- **`application/dto/CreateCatalogItemRequest.java`·`UpdateCatalogItemRequest.java`·`CatalogItemDetailResponse.java`**: `List<FormFieldDto> formSchema` → `Map<String, Object> formSchema`(opaque JSON 트리)로 변경.
- **`application/EsmRequestService.java`**: `EsmRequestFormValueRepository`/`EsmCatalogFormFieldRepository` 의존성 제거. 제출 시 `EsmRequestFormValue` 저장 루프 제거, `formValues` 맵을 엔티티에 그대로 저장(`common.form.FormJsonMapper`로 `writeValues`/`readValues` 헬퍼, SRM `ServiceRequestService` 패턴). 기존 `validateRequiredFields(...)`(EsmCatalogFormField 기반) 호출을 `common.form.FormSubmissionValidator.validate(readSchema(item.getFormSchema()), request.formValues())`로 교체(카탈로그 항목의 `formSchema` 조회 필요 — `EsmCatalogItemRepository` 또는 이미 주입돼 있으면 재사용). 상세 조회의 `formValues` 조립 로직(`formValueRepository.findByEsmRequestId` 순회)도 `readValues(request.getFormValues())` 그대로 반환으로 교체. `RequestDetailResponse`/`CreateRequestRequest`의 `formValues: Map<String, Object>` 타입 자체는 변경 없음(API 계약 그대로).
- **테스트**: `EsmCatalogServiceTest`/`EsmRequestServiceTest`(존재 시) 등 EAV 관련 mock·픽스처를 `formSchema`/`formValues` Map 기반으로 갱신. 그리드 컴포넌트 객체 픽스처는 SRM `FormSubmissionValidatorTest` 패턴 참고(`{key,type,validation:{required,regex}}`).
- `common/exception/ErrorCode.java`: ESM 전용 폼 관련 오류코드가 있었다면 확인 후 정리(현재 파악으로는 공용 `REQUIRED_FIELD_MISSING`/`FORM_FIELD_INVALID` 재사용이라 신규 불필요).

#### FE (dev-fe) — `source/frontend/src/features/esm/`

- **`EsmCatalogManagePage.tsx`(SCR-ESM-006)**: `FieldBuilder`(`field-builder.tsx`) 사용부를 SRM `CatalogManagePage.tsx`와 동일하게 `DynamicFormBuilder`(공용, `@/components/common`)를 `Modal`로 감싼 "Form 설정" 팝업 + 그 아래 축소 미리보기(`disabled`+`hideFooter` `DynamicFormRenderer` 재사용, `PREVIEW_SCALE` 로컬 상수)로 교체. 상세 조회 응답 `formSchema`를 `initialSchema`로 주입, 팝업 적용 시 로컬 상태 축적, 기존 "저장" 버튼 클릭 시 카탈로그 항목 생성/수정 API 페이로드(`formSchema`)로 전달.
- **`DeptRequestSubmitPage.tsx`(SCR-ESM-002)**: `DynamicForm`(`dynamic-form.tsx`) 사용부를 공용 `DynamicFormRenderer`로 교체. 카탈로그 상세 조회 `formSchema`를 `schema`로 주입, `onSubmit`으로 받은 데이터를 그대로 요청 생성 API(`formValues`)로 전달. 지식 추천 패널은 없음(설계상 SRM과 차이, 기존 그대로 유지).
- **`api.ts`/`types.ts`**: `formFields`/`FormFieldSchema` 관련 타입을 `GridFormSchema`(공용 `@/components/common` re-export)로 교체. 기존 필드 배열 전용 클라이언트 검증 로직이 있으면 제거(공용 렌더러 내장 검증 + 서버 재검증으로 이관).
- **레거시 정리**: ESM이 더는 `field-builder.tsx`/`dynamic-form.tsx`를 쓰지 않게 되면 이 두 파일과 `form-schema.ts` 하단 레거시 구획(`FormFieldSchema`/`FormFieldType`/`FormValues`/`FormErrors`/`validateForm`/`hasOptions`), `index.ts`의 해당 export가 전부 죽은 코드가 된다 — **완전 삭제 대상**(다른 소비처 없음, `dev-fe`가 자기 작업 완료 확인 후 `dev-ui`에게 삭제 요청하거나, 공통 파일이라 `dev-ui`가 직접 정리해도 됨 — 파일 소유 경계상 `components/common/`은 UI 역할이므로 dev-ui가 삭제 수행, dev-fe는 자신의 `features/esm/` 파일만 담당).

### 진행 순서

1. DB(완료) → BE(엔티티+서비스+DTO 전환) → FE(화면 전환, BE formSchema 계약 확정 후 착수) — FE는 BE의 `formSchema`가 `Map<String,Object>`로 나오는지 확인 후 연동. BE/FE 병행 가능(계약은 이미 api_spec에 확정돼 있으므로).
2. FE 작업 완료 후, 레거시 `field-builder.tsx`/`dynamic-form.tsx`/`form-schema.ts` 레거시 구획/`index.ts` export 삭제는 dev-ui에게 요청.

### 완료(테스트 통과) 기준

- BE: 카탈로그 생성/수정 시 그리드 스키마 그대로 저장·조회(`formSchema` Map), 부서 요청 제출 시 `FormSubmissionValidator`가 `required`/`text` 정규식 위반을 첫 번째 것만 400으로 거부, 정상 제출은 `form_values`에 그대로 저장. 온보딩/오프보딩 체크리스트 자동 생성(REQ-ESM-005/006) 등 기존 기능 회귀 없음.
- FE: SCR-ESM-006에서 그리드 폼 빌더(팔레트 9종, DnD 배치, 라벨 태그, 9방향 정렬 등 SRM과 동일 기능)로 양식 설계·저장, SCR-ESM-002에서 그 폼이 그대로 렌더링되고 제출 가능. 기존 온보딩/오프보딩 대상자명 필수 입력·체크리스트 자동 생성 안내 토스트 등 기존 로직 회귀 없음.
- 레거시 `field-builder.tsx`/`dynamic-form.tsx`와 `form-schema.ts`의 레거시 타입 구획이 완전히 삭제되고, 코드베이스 어디에서도 참조되지 않음(빌드 통과로 확인).
- tester 통합 테스트 후 dev-lead에 결과 보고 → 실패 0까지 수정 루프 → Standards/Spec 코드 리뷰 → 완료 시 커밋(main).
