# 통합 테스트 시나리오 — asset (ITAM, 7/7 마지막 도메인)

> 실행 타임스탬프: 20260710-064414 · 도메인: asset
> 범위: API-ITAM-001~012 정상+오류(400/401/403/404), EAV 속성, 생애주기 이력, 만료 임박 계산, CI/CMDB 관계(자기참조 금지)+영향범위(BFS), 4종 티켓 연계(SERVICE_REQUEST/INCIDENT/PROBLEM/CHANGE), 지표
> **개발 중 확정 사항 반영**(docs/03_develop/plan/asset.md §8): (1) 자산 수정 만료일 과거 입력 시 400이 아닌 200+warning, (2) utilizationRate="전체 자산 중 OPERATION 비율(%)", (3) 생애주기 전이 순서 불문(정의된 값만 400), (4) 폐기 응답 status="RETIREMENT"(계약의 RETIRED 오탈자), (5) 상세 expiry 필드가 `{date,status}` 객체로 확장
> **마지막 도메인**: 7개 도메인 전체 회귀(특히 cross-domain 티켓 연계: SERVICE_REQUEST/INCIDENT/PROBLEM/CHANGE/KNOWLEDGE 전반) 포함

## 사전 조건

- 빌드 테스트 통과(BE `gradlew clean test`, FE `npm run build`)
- BE(:8080)·FE dev(:5173) 기동, PostgreSQL(`itsm-postgres`) healthy
- 테스트 계정:
  - `am@itsm.local` — ASSET_MANAGER (자산 등록/수정/폐기/생애주기 전이 전용)
  - `agent@itsm.local` — SERVICE_DESK_AGENT (자산 관리 계열 403 검증용, CI/연계/조회는 비-AM도 가능한지 확인)
  - `im@itsm.local`(INCIDENT_MANAGER), `pm@itsm.local`(PROBLEM_MANAGER), `cm@itsm.local`(CHANGE_MANAGER) — 4종 티켓 연계 검증용
- 근거 기준: @docs/01_analyze/prd/asset.md, @docs/01_analyze/feature/asset.md, @docs/02_plan/api_spec/asset.md, @docs/02_plan/screen/asset.md, @docs/02_plan/security/authorization/asset_manager.md, @docs/03_develop/plan/asset.md(§8 확정 사항)
- 격리: playwright 매 항목 새 context/storage 초기화. API는 계정당 로그인 세션 재사용(TTL 300s 내 그룹 실행).

## 시나리오

### A. 빌드
- **TC-BUILD-001** · BE `gradlew clean test` 통과(asset 패키지 + 7개 도메인 전체 JUnit) — @docs/01_analyze/feature/asset.md 전 FEAT
- **TC-BUILD-002** · FE `npm run build`(tsc + vite build) 통과 — @docs/02_plan/screen/asset.md SCR-ITAM-001~005

### B. 인증/인가 (401/403)
- **TC-AUTH-001** · 미인증 `GET /api/v1/assets` 401 — 공통 규약
- **TC-ITAM-RBAC-001** · agent(권한 없음) `POST /assets` 등록 시도 403 — asset_manager.md(등록은 AM 전용)
- **TC-ITAM-RBAC-002** · agent `PATCH /assets/{id}/retire` 폐기 시도 403 — asset_manager.md
- **TC-ITAM-RBAC-003** · agent `PATCH /assets/{id}/lifecycle` 전이 시도 403 — asset_manager.md
- **TC-ITAM-RBAC-004** · agent(비-AM)가 CI 등록/조회·자산 티켓 연계 시도 시 정상 수행(403 아님) — asset_manager.md(CI/연계는 역할 제한 없음, api_spec 인증만 요구)

### C. 자산 등록·수정 (FEAT-ITAM-001/003 / API-ITAM-002/004)
- **TC-ITAM-001** · 정상 등록(name+type=HARDWARE+owner+attributes) 201, `assetKey=AST-####`, `status=PLANNING` — REQ-ITAM-001 (Event-driven)
- **TC-ITAM-002** · name 누락 등록 400 — REQ-ITAM-001 (Unwanted)
- **TC-ITAM-003** · type 누락 등록 400 — FEAT-ITAM-001 (Unwanted)
- **TC-ITAM-004** · 목록 조회 200, `{content,page,size,totalElements}`, 생성분 포함(상대검증) — API-ITAM-001
- **TC-ITAM-005** · 상세 조회 200, `{attributes,expiry:{license,warranty,contract} 각 {date,status}, lifecycleHistory,linkedTickets,linkedCis}` 구조 확인 — API-ITAM-003(§8 확정: expiry 객체 확장)
- **TC-ITAM-006** · 존재하지 않는 id 상세 404 — API-ITAM-003
- **TC-ITAM-007** · 수정(name 변경) 200 — API-ITAM-004
- **TC-ITAM-008** · 수정 시 만료일 과거 입력 → 200 + `warning` 필드(400 아님, §8 확정) — API-ITAM-004
- **TC-ITAM-009** · 존재하지 않는 id 수정 404 — API-ITAM-004

### D. 생애주기 전이·폐기 (FEAT-ITAM-002 / API-ITAM-005/006)
- **TC-ITAM-010** · 임의 순서 전이(예: PLANNING→MAINTENANCE, 순서 불문 허용, §8 확정) 200, 이력 기록 — REQ-ITAM-002 (Event-driven)
- **TC-ITAM-011** · 정의되지 않은 단계(예: "UNKNOWN") 전이 400 — FEAT-ITAM-002 (Unwanted)
- **TC-ITAM-012** · 존재하지 않는 id 전이 404 — API-ITAM-005
- **TC-ITAM-013** · 폐기 처리 200, `status="RETIREMENT"`(§8 확정, 계약 RETIRED 아님) — REQ-ITAM-001 (Event-driven)
- **TC-ITAM-014** · 존재하지 않는 id 폐기 404 — API-ITAM-006

### E. 만료 추적 (FEAT-ITAM-004 / API-ITAM-001/003)
- **TC-ITAM-015** · licenseExpiry를 30일 이내 임박 날짜로 등록 → 목록/상세에서 `expiryStatus/status="EXPIRING"` — REQ-ITAM-004 (Event-driven)
- **TC-ITAM-016** · 만료일을 과거로 등록/수정 → `expiryStatus/status="EXPIRED"` — FEAT-ITAM-004 (Unwanted, 경고 성격)
- **TC-ITAM-017** · 만료일 없는 자산 → `expiryStatus/status=null` 또는 "OK"(무영향 확인) — API-ITAM-001/003

### F. 티켓 연계 (FEAT-ITAM-006 / API-ITAM-007) — 4종 전체
- **TC-ITAM-018** · 자산→SERVICE_REQUEST 연계 200 — REQ-ITAM-006 (Event-driven)
- **TC-ITAM-019** · 자산→INCIDENT 연계 200 — REQ-ITAM-006
- **TC-ITAM-020** · 자산→PROBLEM 연계 200 — REQ-ITAM-006
- **TC-ITAM-021** · 자산→CHANGE 연계 200 — REQ-ITAM-006
- **TC-ITAM-022** · 존재하지 않는 티켓 연계 400 — FEAT-ITAM-006 (Unwanted)
- **TC-ITAM-023** · 연계 후 자산 상세 `linkedTickets`에 4종 모두 반영 확인 — API-ITAM-003

### G. CI·CMDB 관계 (FEAT-ITAM-005 / API-ITAM-008/009/010)
- **TC-ITAM-024** · CI 등록(name+assetId 연결) 201 — REQ-ITAM-005 (Event-driven)
- **TC-ITAM-025** · CI 목록 조회 200, 생성분 포함 — API-ITAM-008
- **TC-ITAM-026** · CI 관계 등록(DEPENDS_ON, 유효 대상) 200 — REQ-ITAM-005 (Event-driven)
- **TC-ITAM-027** · 존재하지 않는 대상 CI와 관계 등록 400 — REQ-ITAM-005 (Unwanted)
- **TC-ITAM-028** · 자기참조 관계 등록(sourceCiId=targetCiId) 400 — DB 설계(자기참조 금지 CHECK)
- **TC-ITAM-029** · 자산 상세 `linkedCis`에 연결 CI 반영 확인 — API-ITAM-003

### H. CI 영향 범위 조회 (FEAT-ITAM-007 / API-ITAM-011)
- **TC-ITAM-030** · 의존 관계 있는 CI 영향 범위 조회 200, `[{ciId,name,relationType,depth}]` 반환 — REQ-ITAM-007 (Event-driven)
- **TC-ITAM-031** · 관계 없는 CI 영향 범위 조회 200, 빈 목록 — FEAT-ITAM-007 (Unwanted)
- **TC-ITAM-032** · 다단계 의존(A→B→C) 등록 후 영향 범위에 depth 2 이상 항목 포함 확인(BFS) — REQ-ITAM-007

### I. 자산 지표 (FEAT-ITAM-008 / API-ITAM-012)
- **TC-ITAM-033** · 지표 조회 200, `{utilizationRate,expiringCount,typeDistribution}` 구조, OPERATION 상태 자산 존재 시 utilizationRate>0 확인(§8 확정 산식) — REQ-ITAM-008 (Ubiquitous)
- **TC-ITAM-034** · 데이터 없는 상태(자산 0건 필터) 조회 200, `utilizationRate=0` — FEAT-ITAM-008 (Unwanted)

### J. FE E2E (playwright, http://localhost:5173, 매 항목 새 context/storage)
- **TC-E2E-001** · AM 로그인 → 자산 등록(SCR-ITAM-002) 유형 선택 시 속성 동적 필드·만료일 입력 → 등록 성공 후 상세 이동 — SCR-ITAM-002
- **TC-E2E-002** · 자산 목록(SCR-ITAM-001) 필터(유형·상태·소유자·만료임박)·배지 표시 — SCR-ITAM-001
- **TC-E2E-003** · 자산 상세(SCR-ITAM-003) 생애주기 전이·만료 정보 강조(임박/경과)·티켓 연계·폐기 확인 다이얼로그 — SCR-ITAM-003
- **TC-E2E-004** · CI·CMDB 관계 뷰(SCR-ITAM-004) CI 등록·관계 추가·영향 범위 패널 표시 — SCR-ITAM-004
- **TC-E2E-005** · 자산 지표 대시보드(SCR-ITAM-005) KPI 카드 + 유형 분포 표시 — SCR-ITAM-005
- **TC-E2E-006** · 비-AM(agent) 로그인 시 자산 등록/폐기/전이 버튼·API 403, 목록/CI 조회는 가능 — asset_manager.md 인가

### K. 7개 도메인 전체 회귀 (마지막 도메인 완료 확인)
- **TC-REG-001** · SRM(서비스 요청)↔ASSET 연계: 서비스 요청에서 자산 링크 반영 확인(자산 상세 linkedTickets에 SERVICE_REQUEST 노출, TC-ITAM-018/023과 동일 근거로 갈음)
- **TC-REG-002** · INCIDENT↔PROBLEM↔CHANGE↔KNOWLEDGE 연계 사슬이 asset 도입 후에도 정상 동작(회귀, 기존 도메인 대표 API 1~2건 스팟체크)
- **TC-REG-003** · 7개 역할(END_USER/SERVICE_DESK_AGENT/INCIDENT_MANAGER/PROBLEM_MANAGER/CHANGE_MANAGER/APPROVER/KNOWLEDGE_CONTRIBUTOR/KNOWLEDGE_GATEKEEPER/ASSET_MANAGER) 사이드바 메뉴가 설계된 역할별로 정상 노출되는지 스팟체크(신규 ASSET_MANAGER 메뉴 추가로 인한 회귀 없는지)
