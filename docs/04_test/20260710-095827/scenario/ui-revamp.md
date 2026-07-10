# 통합 테스트 시나리오 — ui-revamp (UI/UX 개편)

> 실행 타임스탬프: 20260710-095827 · 대상: cross-cutting 이니셔티브(신규 API/DB 없음, FE 스모크·시각 회귀 위주)
> 근거: `docs/01_analyze/prd/ui-revamp.md`(REQ-UIX-001~013), `docs/01_analyze/feature/ui-revamp.md`(FEAT-UIX-001~013), `docs/02_plan/screen/common.md` v0.2, `docs/03_develop/plan/ui-revamp.md` 5절 완료 기준

## 사전 조건
- BE(:8080)·FE dev(:5173) 기동, PostgreSQL(`itsm-postgres`) healthy
- 계정: admin@itsm.local/Admin@1234(SYSTEM_ADMIN), im@itsm.local(INCIDENT_MANAGER), agent@itsm.local(SERVICE_DESK_AGENT), pm@itsm.local(PROBLEM_MANAGER), cm@itsm.local(CHANGE_MANAGER), cab@itsm.local(APPROVER), kc@itsm.local(KNOWLEDGE_CONTRIBUTOR), kg@itsm.local(KNOWLEDGE_GATEKEEPER), am@itsm.local(ASSET_MANAGER)
- playwright는 매 TC마다 새 browser context로 수행, storage 초기화(테마 토글·유지 검증 TC 제외 — 해당 TC는 의도적으로 storage를 유지해 재방문 시나리오를 확인)

## 시나리오

### A. 빌드
- **TC-BUILD-001** · FE `npm run build`(tsc -b && vite build) 통과 — @docs/03_develop/plan/ui-revamp.md 5절
- **TC-BUILD-002** · BE `gradlew test` 통과(회귀 없음, FE 전용 변경이므로 기존 테스트 그대로 통과 기대) — @docs/03_develop/plan/ui-revamp.md 7절

### B. 다크모드 토글 (REQ-UIX-002, SCR-COM-010)
- **TC-THEME-001** · 최초 진입(저장값 없음) 시 라이트 테마 기본 적용 확인 — @docs/01_analyze/prd/ui-revamp.md (REQ-UIX-002), @docs/02_plan/screen/common.md (SCR-COM-010)
  - 절차: storage 초기화한 새 context로 로그인 후 앱 진입 → `document.documentElement`/`html`의 `data-theme` 확인
  - 기대 결과: `data-theme` 미설정 또는 `light`, 라이트 색상 렌더링
- **TC-THEME-002** · 헤더 테마 토글 버튼 클릭 시 즉시 다크 전환 — REQ-UIX-002 Event-driven
  - 절차: 테마 토글 버튼(아이콘, `aria-label="테마 전환"`) 클릭
  - 기대 결과: 즉시 `data-theme="dark"`로 전환, 배경·텍스트·테두리·사이드바 색상이 2.1절 다크 값으로 렌더링(예: `--background`=#161A1F, `--sidebar`=#10141A), 아이콘이 달→해로 변경
- **TC-THEME-003** · 다크 선택 후 새로고침 시 유지 — REQ-UIX-002 Ubiquitous(재방문 유지)
  - 절차: TC-THEME-002 상태에서 페이지 새로고침(같은 context, storage 유지)
  - 기대 결과: 새로고침 후에도 `data-theme="dark"` 유지, localStorage에 테마 선호값 저장 확인
- **TC-THEME-004** · 유효하지 않은/없는 저장값일 때 라이트 기본값 — REQ-UIX-002 Unwanted
  - 절차: `localStorage`에 테마 키를 잘못된 값으로 설정 후 재진입
  - 기대 결과: 라이트 테마로 폴백 렌더링

### C. Lozenge 배지 (REQ-UIX-007)
- **TC-BADGE-001** · 목록 화면 상태 배지 Lozenge 스타일(4px radius+테두리, subtle) — @docs/01_analyze/feature/ui-revamp.md (FEAT-UIX-007), @docs/02_plan/screen/common.md (SCR-COM-007)
  - 절차: 인시던트 목록(`/incidents`)에서 상태 배지 요소의 computed style(`border-radius`, `border-width`, `background-color`) 확인
  - 기대 결과: `border-radius` ≈4px(radius.small), 테두리(border) 존재, 텍스트 라벨 병행 표시(색상 단독 아님)
- **TC-BADGE-002** · 우선순위 배지 동일 규격 확인(강조 P1은 bold 허용) — REQ-UIX-007
  - 절차: 인시던트 목록의 우선순위 배지(P1 포함) computed style 확인
  - 기대 결과: 4px radius+테두리 유지, P1은 bold(불투명 배경) 허용, 나머지는 subtle

### D. 공통 컴포넌트 API 불변 (REQ-UIX-008, REQ-UIX-009)
- **TC-COMP-001** · Button variant/size 기존 호출부 정상 동작(콘솔 에러 없음) — FEAT-UIX-008
  - 절차: 여러 화면(로그인, 목록 "신규 생성", 상세 상태 전이 버튼)에서 버튼 클릭 동작 확인 및 브라우저 콘솔 에러 확인
  - 기대 결과: 기존과 동일하게 동작, TypeScript 빌드(TC-BUILD-001)로 prop 인터페이스 불변 확인, 런타임 콘솔 에러 없음
- **TC-COMP-002** · Dialog/AlertDialog가 overlay elevation 토큰 적용 — FEAT-UIX-005/008
  - 절차: 파괴적 동작 확인 다이얼로그(예: 로그아웃 확인) 오픈, computed `background-color`/`box-shadow` 확인
  - 기대 결과: `--popover` 배경 + overlay 그림자(라이트 `0 4px 12px rgba(31,41,55,0.16)`) 적용
- **TC-COMP-003** · 레이아웃 셸 구조(헤더/사이드바/콘텐츠/푸터) 개편 전후 동일 — REQ-UIX-009
  - 절차: 앱 진입 후 헤더(56px)·사이드바(240px, 접기 가능)·푸터 존재 확인, 사이드바 접기 토글 동작 확인
  - 기대 결과: 구조 유지, 사이드바 접기 시 폭 240px→64px 전환(모션은 F항목에서 별도 확인)

### E. 접근성 — 포커스 링 · 아이콘 라벨 (REQ-UIX-010, REQ-UIX-011)
- **TC-A11Y-001** · 키보드 포커스 시 2px 링+색상 토큰 동시 렌더링 — @docs/01_analyze/feature/ui-revamp.md (FEAT-UIX-011)
  - 절차: Tab 키로 로그인 버튼/목록 "신규 생성" 버튼에 포커스 이동, computed `outline`/`box-shadow`(포커스 링 구현 방식) 확인
  - 기대 결과: 2px 두께 + `--ring` 색상 토큰이 동시에 적용(색상만 또는 두께만 있는 경우는 결함)
- **TC-A11Y-002** · 아이콘 단독 버튼 접근성 라벨 — FEAT-UIX-010 Unwanted
  - 절차: 테마 토글·알림 벨·사이드바 접기 버튼의 `aria-label` 속성 확인
  - 기대 결과: 3개 버튼 모두 텍스트 라벨 없이 아이콘만 사용하므로 `aria-label` 필수 존재

### F. 모션 (REQ-UIX-006)
- **TC-MOTION-001** · 모달 진입/퇴장이 transform/opacity만 사용 — @docs/01_analyze/feature/ui-revamp.md (FEAT-UIX-006)
  - 절차: 확인 다이얼로그 오픈/닫기 시 computed `transition-property` 확인
  - 기대 결과: `transform`/`opacity`만 포함, `width`/`height` 미포함
- **TC-MOTION-002** · `prefers-reduced-motion: reduce` 시 즉시 전환 — REQ-UIX-006 State-driven
  - 절차: playwright `emulateMedia({ reducedMotion: 'reduce' })` 설정 후 모달 오픈
  - 기대 결과: 전환 duration 0(즉시 표시), 애니메이션 지연 없음

### G. 색상 대비 (REQ-UIX-012)
- **TC-CONTRAST-001** · 라이트 테마 본문 텍스트 대비 — FEAT-UIX-012
  - 절차: `--foreground`(#1F2937) on `--background`(#F9FAFB) 대비율 계산
  - 기대 결과: 4.5:1 이상
- **TC-CONTRAST-002** · 다크 테마 본문 텍스트 대비 — FEAT-UIX-012
  - 절차: `--foreground`(#E5E7EB) on `--background`(#161A1F) 대비율 계산
  - 기대 결과: 4.5:1 이상
- **TC-CONTRAST-003** · 보조 텍스트(`--muted-foreground`) 라이트/다크 대비 — FEAT-UIX-012
  - 절차: 라이트 `--muted-foreground`(#6B7280) on `--background`(#F9FAFB), 다크 `--muted-foreground`(#9CA3AF) on `--background`(#161A1F) 대비율 계산
  - 기대 결과: 4.5:1 이상(24px 미만 텍스트 기준)

### H. 8개 도메인 화면 회귀 스모크 (전 도메인 자동 반영 확인, 기존 기능 회귀 없음)
- **TC-DOM-001** · auth/admin — 로그인(`/login`), 사용자 목록·상세(`/admin/users`) 토큰 반영 + 로그인 기능·역할 부여 기능 회귀 없음 — @docs/02_plan/screen/common.md
- **TC-DOM-002** · service-request — 포털/큐 목록(`/service-requests`), 상세 화면 토큰 반영 + 상태 배지 Lozenge 확인, 큐 조회 회귀 없음 (agent@itsm.local)
- **TC-DOM-003** · incident — 목록·상세(`/incidents`) 토큰 반영 + 상태 전이 버튼 기능 회귀 없음 (im@itsm.local)
- **TC-DOM-004** · problem — 목록·상세(`/problems`) 토큰 반영 + KEDB 배지 렌더링 회귀 없음 (pm@itsm.local)
- **TC-DOM-005** · change — 목록·상세(`/changes`) 토큰 반영 + 상태 배지(6단계) 및 승인 화면 회귀 없음 (cm@itsm.local)
- **TC-DOM-006** · knowledge — 목록·상세(`/knowledge`) 토큰 반영 + 상태 배지(DRAFT/IN_REVIEW/PUBLISHED) 회귀 없음 (kc@itsm.local)
- **TC-DOM-007** · asset — 목록·상세(`/assets`) 토큰 반영 + 생애주기 배지 회귀 없음 (am@itsm.local)
- **TC-DOM-008** · admin RBAC — 권한 없는 화면 접근 시 403(SCR-COM-006) 토큰 반영 확인 — @docs/02_plan/screen/common.md (SCR-COM-006)

### I. 다크모드 도메인 스팟체크 (2개 대표 화면)
- **TC-DARK-001** · 인시던트 목록(다크) 렌더링 정상(배경/텍스트/배지 대비 유지) — REQ-UIX-002 State-driven
- **TC-DARK-002** · 자산 상세(다크) 렌더링 정상(카드/모달 elevation 다크값 적용) — REQ-UIX-002 State-driven
