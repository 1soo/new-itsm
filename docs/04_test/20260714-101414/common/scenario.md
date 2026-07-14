# 통합 테스트 시나리오 — 공통(사이드바 축소·컬럼폭 고정·페이지당 아이템 수)

## 사전 조건
- 빌드 테스트 통과 (`source/frontend`: `npm run build`)
- 테스트 계정 1개 이상 로그인 가능해야 함(LoginPage 테스트 계정 표 활용)
- 각 목록 화면에 페이지네이션이 발생할 만큼(변경된 PAGE_SIZE + 1건 이상) 데이터가 존재해야 함. 데이터 부족 시 해당 화면은 "컬럼폭/헤더" 검증만 수행하고 페이지 이동 흔들림 항목은 SKIP으로 기록.

## 시나리오

### TC-COM-001 · 프론트엔드 빌드 테스트
- 근거: @docs/02_plan/screen/common.md (2.6절, SCR-COM-003, SCR-COM-007 7절)
- 전제: `source/frontend` 의존성 설치됨
- 절차:
  1. `source/frontend`에서 `npm run build` 실행
- 기대 결과: 타입체크(`tsc -b`) 및 vite build 성공, 에러 없음

### TC-COM-002 · 사이드바 치수·폰트 축소 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-001, SCR-COM-003 — 펼침 190px/접힘 48px, 라벨 12px/그룹헤더 10px)
- 전제: 로그인 완료, 사이드바 펼침 상태
- 절차:
  1. 로그인 후 사이드바 요소의 실제 렌더링 폭 측정(펼침 상태)
  2. 사이드바 접기 토글 클릭 후 폭 재측정(접힘 상태)
  3. 그룹 헤더 라벨·메뉴 항목 라벨의 computed font-size 측정
- 기대 결과: 펼침 190px, 접힘 48px, 그룹헤더 10px, 메뉴라벨 12px

### TC-COM-003 · 사이드바 접기/펼치기 모션 확인
- 근거: @docs/02_plan/screen/common.md (2.6절 — 폭 전환 duration.medium(200ms) ease-in-out bold, 라벨 opacity 페이드)
- 전제: 로그인 완료
- 절차:
  1. 사이드바 컨테이너의 `transition` CSS 속성 확인(width, duration, easing)
  2. 접기/펼치기 토글 클릭 시 시각적 애니메이션 동작 확인(스크린샷 비교)
- 기대 결과: 폭 전환에 transition 적용(200ms 근사), 레이아웃 깨짐 없음

### TC-COM-004 · DataTable 컬럼폭 인프라 — colgroup/table-fixed 렌더링
- 근거: @docs/02_plan/screen/common.md (SCR-COM-007 컬럼 폭 고정 아키텍처)
- 전제: 로그인 완료, `DataTable` 경유 목록 화면(예: IncidentListPage) 접근
- 절차:
  1. 목록 화면 진입 후 표 DOM에서 `<table>`의 `table-layout` computed style 확인
  2. `<colgroup>`/`<col>` 요소 존재 및 width 지정 컬럼의 style(width/min-width/max-width) 확인
- 기대 결과: `table-layout: fixed` 적용, width 지정 컬럼은 고정폭, 미지정 컬럼(대표 텍스트)은 잔여 폭 흡수

### TC-COM-005 · DataTable 미경유 표(LoginPage) 영향 없음 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-007 — `components/ui/table.tsx` 프리미티브 직접 조합 표는 auto layout 유지)
- 전제: 미로그인 상태(로그인 화면)
- 절차:
  1. 로그인 화면의 테스트 계정 표 DOM에서 `<table>`의 `table-layout` computed style 확인
- 기대 결과: `table-layout: fixed`가 아님(auto 또는 미지정), `<colgroup>` 없음(DataTable 영향 없음)

### TC-COM-006~022 · 도메인별 목록 화면 컬럼폭 px값 + PAGE_SIZE 검증 (17개 화면)
- 근거: @docs/02_plan/screen/{service-request,incident,problem,change,knowledge,asset,esm,vulnerability,compliance,common,admin}.md (각 도메인 컬럼 폭 표, common.md 7.4절 PAGE_SIZE 표)
- 전제: 로그인 완료(역할별 접근 가능 화면), 목록 데이터 존재
- 절차(화면당 공통):
  1. 대상 목록 화면 진입
  2. 각 컬럼 헤더 `<col>`의 style(width/minWidth/maxWidth px) 확인 후 설계 문서 표 값과 일치 여부 비교
  3. 페이지네이션으로 다음 페이지 이동 후 동일 컬럼 폭이 유지되는지(흔들림 없음) 확인
  4. 자유 텍스트 컬럼(제목/이름/이메일/주체/대상 등)에 `truncate` 또는 `line-clamp-1` 클래스 적용 확인
  5. 페이지당 렌더링된 행(row) 수가 설계된 PAGE_SIZE와 일치하는지 확인(데이터 충분 시)
- 대상 화면 및 기대값:
  | TC ID | 파일 | 페이지당 아이템 수 |
  |-------|------|------|
  | TC-COM-006 | service-request/RequestListPage.tsx | 13 |
  | TC-COM-007 | service-request/RequestQueuePage.tsx | 16 |
  | TC-COM-008 | incident/IncidentListPage.tsx | 14 |
  | TC-COM-009 | problem/ProblemListPage.tsx | 14 |
  | TC-COM-010 | change/ChangeListPage.tsx | 14 |
  | TC-COM-011 | knowledge/KnowledgeListPage.tsx | 14 |
  | TC-COM-012 | asset/AssetListPage.tsx | 14 |
  | TC-COM-013 | esm/EsmRequestQueuePage.tsx | 13 |
  | TC-COM-014 | esm/MyEsmRequestsPage.tsx | 13 |
  | TC-COM-015 | esm/HrCaseListPage.tsx | 13 |
  | TC-COM-016 | esm/MyChecklistTasksPage.tsx | 13 |
  | TC-COM-017 | vulnerability/VulnerabilityListPage.tsx | 14 |
  | TC-COM-018 | compliance/ComplianceListPage.tsx | 14 |
  | TC-COM-019 | search/SearchResultsPage.tsx | 13 |
  | TC-COM-020 | admin/AuditLogPage.tsx | 14 |
  | TC-COM-021 | admin/UserListPage.tsx | 14 |
  | TC-COM-022 | admin/MenuManagementPage.tsx | 16 |
- 기대 결과: 각 화면의 컬럼폭이 도메인 screen 문서 표 값과 일치, 페이지 이동 시 흔들림 없음, 자유텍스트 truncate/line-clamp 적용, PAGE_SIZE 일치

### TC-COM-023 · KnownErrorSearchPage(SCR-PRB-004) 제외 확인
- 근거: @docs/02_plan/screen/problem.md (컬럼 폭 대상 아님, PAGE_SIZE=10 유지), @docs/02_plan/screen/common.md (7.5절 카드 리스트 예외)
- 전제: 로그인 완료
- 절차:
  1. `problem/KnownErrorSearchPage.tsx` 화면 진입
  2. 렌더링 방식이 `Card` 반복 리스트인지(표/DataTable이 아님) 확인
  3. 페이지당 아이템 수(PAGE_SIZE) 코드/렌더링 확인
- 기대 결과: 표가 아닌 카드 리스트 유지, 컬럼폭 미적용, PAGE_SIZE=10 그대로(변경되면 FAIL)
