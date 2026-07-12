# 통합 테스트 시나리오 — auth (Role-Menu 동적 매핑, 유지보수)

> 실행 타임스탬프: 20260711-115017 · 도메인: auth · 범위: API-AUTH-016~022, SCR-ADMIN-006(메뉴 관리), 사이드바 동적 RBAC 노출
> 참고: `docs/03_develop/plan/auth.md` "Role-Menu 동적 매핑" 절(완료 기준)

## 사전 조건

- 빌드 테스트 통과(Backend Gradle, Frontend Vite build)
- PostgreSQL 컨테이너(itsm-postgres) 기동·healthy, `screen` 컬럼 추가·백필 및 SCR-ADMIN-006 시드 적용
- Backend(:8080)·Frontend dev(:5173) 기동
- 초기 계정: `admin@itsm.local` / `Admin@1234` (SYSTEM_ADMIN)
- 격리: playwright는 매 항목 새 context(새 창)·storage 초기화. API는 항목별 신규 토큰 발급.

## 시나리오

### A. 빌드 테스트

#### TC-BUILD-001 · Backend 빌드·단위테스트
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-016~022), spring-boot-development(예외별 JUnit 필수)
- 절차: `./gradlew clean test build` 실행
- 기대 결과: 컴파일·전체 테스트 통과, BUILD SUCCESSFUL

#### TC-BUILD-002 · Frontend 빌드
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-006), react-development(빌드 테스트)
- 절차: `npm run build` (tsc + vite build)
- 기대 결과: 타입체크·번들 성공, dist 산출(navConfig.tsx 삭제로 인한 미참조 오류 없음)

---

### B. BE API — 메뉴(화면) 목록/생성/수정/삭제 (API-AUTH-016~019)

#### TC-SCR-001 · 메뉴 목록 조회(관리자)
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-016 200)
- 절차: 관리자 토큰 → `GET /api/v1/admin/screens?page=0&size=20`
- 기대 결과: 200, `{content[],page,size,totalElements}`, 각 항목에 `iconName/groupCode/groupLabel/sortOrder/navVisible/roles` 포함, SCR-ADMIN-006 자기 자신 포함

#### TC-SCR-002 · 메뉴 목록 조회 미인증/권한부족
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-016 401/403)
- 절차: (a) Authorization 없이 조회 (b) END_USER 토큰으로 조회
- 기대 결과: (a) 401 (b) 403

#### TC-SCR-003 · 메뉴 생성 정상 201
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-017 201)
- 절차: `POST /admin/screens` {screenCode:"SCR-TEST-901", screenName, path:"/test/menu-901", domain:"auth", iconName:"ListTree", groupCode:"admin", groupLabel:"관리자", sortOrder:99, navVisible:true}
- 기대 결과: 201, 목록 항목 구조 동일(`roles: []`)

#### TC-SCR-004 · 메뉴 생성 필수 누락 400
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-017 400)
- 절차: `POST /admin/screens` {screenName:"", path:"", domain:""} (screenCode 등 누락)
- 기대 결과: 400, code=VALIDATION_ERROR

#### TC-SCR-005 · 메뉴 생성 screenCode 중복 409
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-017 409)
- 절차: TC-SCR-003과 동일 screenCode로 재생성 요청
- 기대 결과: 409, code=SCREEN_CODE_DUPLICATE

#### TC-SCR-006 · 메뉴 생성 path 중복 409
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-017 409), database/auth.md(`screen.path` UNIQUE)
- 절차: 다른 screenCode·동일 path("/test/menu-901")로 생성 요청
- 기대 결과: 409, code=PATH_DUPLICATE

#### TC-SCR-007 · 메뉴 수정 정상 200
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-018 200)
- 절차: `PATCH /admin/screens/{TC-SCR-003 id}` {screenName:"수정됨", sortOrder:50}
- 기대 결과: 200, screenName/sortOrder 반영, screenCode·domain 불변

#### TC-SCR-008 · 메뉴 수정 없는 메뉴 404
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-018 404)
- 절차: `PATCH /admin/screens/999999` {screenName:"x"}
- 기대 결과: 404, code=SCREEN_NOT_FOUND

#### TC-SCR-009 · 메뉴 수정 path 중복 409
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-018 409)
- 절차: 다른 기존 메뉴의 path로 `PATCH /admin/screens/{id}` {path: 기존 사용 중인 path}
- 기대 결과: 409, code=PATH_DUPLICATE

#### TC-SCR-010 · 메뉴 삭제(soft delete) 200
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-019 200), (`screen.is_deleted=true`)
- 절차: `DELETE /admin/screens/{TC-SCR-003 id}` → 이어서 `GET /admin/screens` 재조회
- 기대 결과: DELETE 200 `{id, deleted:true}`, 이후 목록 조회에서 해당 메뉴 제외(`is_deleted=false` 필터)

#### TC-SCR-011 · 메뉴 삭제 없는 메뉴 404
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-019 404)
- 절차: `DELETE /admin/screens/999999`
- 기대 결과: 404, code=SCREEN_NOT_FOUND

---

### C. BE API — 메뉴 역할 매핑 (API-AUTH-020~021)

#### TC-SROLE-001 · 역할 매핑 부여 정상(즉시 반영)
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-020 200)
- 절차: 신규 메뉴 생성 → `POST /admin/screens/{id}/roles` {roleId: END_USER}
- 기대 결과: 200, `{screenId, roles:["END_USER"]}`

#### TC-SROLE-002 · 역할 매핑 부여 존재하지 않는 역할 400
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-020 400)
- 절차: `POST /admin/screens/{id}/roles` {roleId: 999999}
- 기대 결과: 400, code=ROLE_NOT_FOUND

#### TC-SROLE-003 · 역할 매핑 부여 없는 메뉴 404
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-020 404)
- 절차: `POST /admin/screens/999999/roles` {roleId: END_USER}
- 기대 결과: 404, code=SCREEN_NOT_FOUND

#### TC-SROLE-004 · 역할 매핑 부여 중복 409
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-020 409)
- 절차: TC-SROLE-001과 동일 {screenId, roleId} 재요청
- 기대 결과: 409, code=SCREEN_ROLE_MAPPING_DUPLICATE

#### TC-SROLE-005 · 역할 매핑 회수 정상
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-021 200)
- 절차: `DELETE /admin/screens/{id}/roles/{roleId}`
- 기대 결과: 200, `{screenId, roles:[]}`(제거 반영)

#### TC-SROLE-006 · 메뉴 변경 시 ROLE_CHANGE 감사 기록
- 근거: @docs/03_develop/plan/auth.md ("모든 변경은 ROLE_CHANGE 감사 로그 기록")
- 절차: 위 생성/수정/삭제/역할부여/회수 수행 후 `GET /admin/audit-logs?eventType=ROLE_CHANGE`
- 기대 결과: 각 변경 건에 대응하는 ROLE_CHANGE 이벤트가 actor/target(screenCode)과 함께 기록

---

### D. BE API — 내 메뉴 조회 (API-AUTH-022)

#### TC-MYMENU-001 · SYSTEM_ADMIN 전체 메뉴 조회
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-022 200)
- 절차: 관리자 로그인 → `GET /api/v1/menus/mine`
- 기대 결과: 200, `{groups:[{groupCode,groupLabel,items[]}]}`, `nav_visible=true`인 화면만 포함, `sort_order` 오름차순, 그룹은 그룹 내 최소 sort_order 기준 정렬. SCR-ADMIN-006(메뉴 관리) 포함

#### TC-MYMENU-002 · 무매핑 화면 전체 공개 규칙
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-022, "screen_role 매핑 전혀 없는 화면은 항상 포함")
- 절차: `screen_role` 매핑이 없는 화면(예: 대시보드) 확인 → END_USER 등 임의 역할 계정으로 `GET /menus/mine` 호출
- 기대 결과: 무매핑 화면이 END_USER 응답에도 포함됨

#### TC-MYMENU-003 · 매핑된 역할만 노출
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-022), FEAT/SCR-ADMIN-006 완료 기준
- 절차: 신규 메뉴 생성(무매핑 아님) → SYSTEM_ADMIN 역할만 매핑 → END_USER 계정으로 `GET /menus/mine` 호출
- 기대 결과: END_USER 응답에 해당 메뉴 미포함(SYSTEM_ADMIN 응답에는 포함)

#### TC-MYMENU-004 · 미인증 401
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-022 401)
- 절차: Authorization 없이 `GET /menus/mine`
- 기대 결과: 401

---

### E. FE E2E (playwright, 매 항목 새 context)

#### TC-E2E-101 · 관리자 로그인 후 사이드바 동적 메뉴 노출(관리자 그룹에 "메뉴 관리" 포함)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-003, 사이드바 동적 구성), @docs/02_plan/screen/admin.md (SCR-ADMIN-006)
- 절차: /login → admin 계정 로그인
- 기대 결과: 사이드바 관리자 그룹에 "메뉴 관리" 항목 표시, 클릭 시 `/admin/menus` 이동

#### TC-E2E-102 · 메뉴 관리 화면 — 목록·생성
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-006)
- 절차: `/admin/menus` 접속 → "메뉴 생성" 클릭 → 화면 코드/도메인/메뉴명/경로 입력 → 저장
- 기대 결과: 목록에 신규 메뉴 반영(토스트), 그룹/메뉴명/경로/아이콘/순서/노출 역할 수 컬럼 표시

#### TC-E2E-103 · 메뉴 관리 화면 — 경로 중복 시 인라인 오류
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-006, "경로 중복 시 409 → 인라인 오류")
- 절차: TC-E2E-102에서 생성한 메뉴와 동일 경로로 새 메뉴 생성 시도
- 기대 결과: 저장 실패, 폼 내 인라인 오류 메시지 표시(모달 유지)

#### TC-E2E-104 · 메뉴 관리 화면 — 역할 매핑 우측 슬라이드 패널(즉시 반영)
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-006, 역할 매핑 패널 토글마다 즉시 API 호출)
- 절차: 신규 메뉴 행의 "역할 매핑" 클릭 → 우측 패널에서 특정 역할 체크 → 체크 해제
- 기대 결과: 체크 시 "노출 역할 수" 배지 즉시 갱신(전체 공개→N개 역할), 체크 해제 시 원복, 각 토글마다 성공 토스트

#### TC-E2E-105 · 메뉴 관리 화면 — 삭제 확인 다이얼로그·즉시 목록 반영
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-006, "삭제된 메뉴는 사이드바에서 즉시 사라진다")
- 절차: TC-E2E-102에서 생성한 메뉴 "삭제" 클릭 → 확인 다이얼로그 확인
- 기대 결과: 목록에서 즉시 제거(토스트)

#### TC-E2E-106 · 일반 사용자(비관리자) 사이드바 — 매핑 없는 메뉴만/매핑된 역할 메뉴만 노출
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-022), @docs/02_plan/screen/common.md (SCR-COM-003)
- 절차: END_USER 역할 계정으로 로그인
- 기대 결과: 사이드바에 "관리자" 그룹(메뉴 관리 등) 미노출, 무매핑 공개 메뉴(대시보드 등)만 노출

#### TC-E2E-107 · SCR-ADMIN-006 접근 통제(403)
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-006, "System Admin만 접근(그 외 403)")
- 절차: END_USER 계정 로그인 상태에서 `/admin/menus` 직접 URL 접근
- 기대 결과: 403 접근 거부 화면(SCR-COM-006) 표시

#### TC-E2E-108 · 메뉴 순서·그룹 정렬 사이드바 반영
- 근거: @docs/02_plan/database/auth.md (5절, sort_order/group_code 정렬 규칙)
- 절차: 관리자 로그인 후 사이드바 메뉴 그룹·순서를 메뉴 관리 화면의 그룹/순서 값과 대조
- 기대 결과: 그룹별 묶음 표시, 그룹 내 sort_order 오름차순과 사이드바 순서 일치
