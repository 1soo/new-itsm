# 통합 테스트 시나리오 — auth (Role-Menu 동적 매핑, 추가/회귀 검증)

> 실행 타임스탬프: 20260711-120600 · 도메인: auth
> 배경: dev-lead 재요청(2026-07-11) — 직전 실행(20260711-115017, 33/33 PASS)에서 API-AUTH-016~022 핵심 CRUD/역할매핑/내메뉴조회는 이미 검증 완료.
> 이번 실행은 dev-lead가 명시한 추가 확인 항목(페이지네이션 전체 순회 무결성, SCR-ADMIN-001~005 회귀, 다중 역할 로그인별 사이드바 차이, 대시보드 최상단 노출)에 집중한다.
> 참고: `docs/03_develop/plan/auth.md` "Role-Menu 동적 매핑" 절, 직전 결과 `docs/04_test/auth/20260711-115017/result/auth.md`

## 사전 조건

- 빌드 테스트 통과(Backend Gradle, Frontend Vite build)
- PostgreSQL(itsm-postgres) healthy, Backend(:8080)·Frontend(:5173) 기동
- 초기 계정: `admin@itsm.local`/`Admin@1234`(SYSTEM_ADMIN), 데모 계정 `user@itsm.local`(END_USER)/`agent@itsm.local`(SERVICE_DESK_AGENT) 등 `Admin@1234` 공통
- 격리: playwright는 매 항목 새 context(새 창)·storage 초기화. API는 항목별 신규 토큰 발급.

## 시나리오

### A. 빌드 테스트

#### TC-BUILD-101 · Backend 빌드·단위테스트 재확인
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-016~022)
- 절차: `./gradlew clean test build`
- 기대 결과: BUILD SUCCESSFUL

#### TC-BUILD-102 · Frontend 빌드 재확인
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-006)
- 절차: `npm run build`
- 기대 결과: 타입체크·번들 성공

---

### B. 메뉴 목록 페이지네이션 전체 순회 무결성

#### TC-PAGE-001 · size=20 전 페이지 순회 시 중복/누락 없음(sort_order 동률 케이스 포함)
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-016), dev-lead 지적("sort_order 동률 페이지네이션 버그" 회귀 확인)
- 절차: `GET /admin/screens?page=0&size=20`부터 `totalElements`로 계산한 마지막 페이지까지 순차 조회, 모든 페이지의 `id` 수집
- 기대 결과: 수집된 id 집합의 크기가 `totalElements`와 일치(중복 없음), size=100(1페이지) 조회 결과의 id 집합과 완전히 동일(누락 없음)

#### TC-PAGE-002 · groupCode/domain 필터 페이지네이션도 동일 원칙
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-016 쿼리 파라미터)
- 절차: `groupCode=admin` 필터로 size=2 페이지네이션 순회 후 id 집합을 size=50 단일 조회와 비교
- 기대 결과: 두 결과의 id 집합 일치, 중복/누락 없음

---

### C. 회귀 — SCR-ADMIN-001~005(계정/역할/감사 로그) 영향 없음

#### TC-REG-001 · 계정 목록 조회 정상(API-AUTH-006)
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-001)
- 절차: `GET /admin/users?size=5`
- 기대 결과: 200, 메뉴 컬럼 추가와 무관하게 정상 응답

#### TC-REG-002 · 역할 목록 조회 정상(API-AUTH-013)
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-004)
- 절차: `GET /admin/roles`
- 기대 결과: 200, 기존 역할 전체 정상 반환

#### TC-REG-003 · 감사 로그 조회 정상(API-AUTH-015)
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-005)
- 절차: `GET /admin/audit-logs?size=5`
- 기대 결과: 200, 정상 응답(페이지네이션 필드 정상)

#### TC-REG-004 · FE 계정/역할/감사 로그 화면 정상 렌더(playwright)
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-001/004/005)
- 절차: 관리자 로그인 → `/admin/users`, `/admin/roles`, `/admin/audit-logs` 순차 접속
- 기대 결과: 3개 화면 모두 정상 렌더(목록 표시), 콘솔 에러로 인한 화면 깨짐 없음

---

### D. FE — 역할별 사이드바 차등 노출 및 대시보드 최상단

#### TC-E2E-201 · SYSTEM_ADMIN 사이드바 — 대시보드 최상단, 관리자 그룹 전체 노출
- 근거: @docs/02_plan/screen/common.md (SCR-COM-003)
- 절차: admin 로그인
- 기대 결과: 사이드바 최상단 항목이 "대시보드", 관리자 그룹에 계정/역할/감사로그/메뉴관리 4개 모두 노출

#### TC-E2E-202 · SERVICE_DESK_AGENT 사이드바 — 관리자 그룹 미노출, 상담원 관련 메뉴만 노출
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-022), SCR-COM-003
- 절차: `agent@itsm.local`/`Admin@1234` 로그인
- 기대 결과: 대시보드 최상단 노출, 관리자 그룹 비노출, SYSTEM_ADMIN과 상이한 메뉴 구성(요청 큐 등 상담원 권한 메뉴 노출)

#### TC-E2E-203 · END_USER 사이드바 — 최소 메뉴만 노출(TC-E2E-106 재확인)
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-022)
- 절차: `user@itsm.local`/`Admin@1234` 로그인
- 기대 결과: 대시보드 최상단, 관리자·상담원 전용 메뉴 비노출

---

### E. SCR-ADMIN-006 스모크 재확인(핵심 CRUD는 직전 실행에서 이미 검증됨)

#### TC-E2E-204 · 메뉴 관리 화면 접근 및 역할 매핑 즉시 반영 스모크
- 근거: @docs/02_plan/screen/admin.md (SCR-ADMIN-006)
- 절차: admin 로그인 → `/admin/menus` 접속 → 임의 메뉴 행 "역할 매핑" 클릭 → 체크박스 토글
- 기대 결과: 패널 정상 오픈, 토글 즉시 반영(성공 토스트)
