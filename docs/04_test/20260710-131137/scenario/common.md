# 통합 테스트 시나리오 — Common (사이드바 · 알림벨 · 통합검색)

## 사전 조건
- 빌드 테스트 통과 (frontend `npm run build`, backend `gradlew build`)
- frontend http://localhost:5173, backend http://localhost:8080, docker `itsm-postgres` 기동 중
- POC 계정(비밀번호 공통 `Admin@1234`): agent@itsm.local(SERVICE_DESK_AGENT), im@itsm.local(INCIDENT_MANAGER), cm@itsm.local(CHANGE_MANAGER), kc@itsm.local(KNOWLEDGE_CONTRIBUTOR), kg@itsm.local(KNOWLEDGE_GATEKEEPER), am@itsm.local(ASSET_MANAGER), cab@itsm.local(APPROVER), user@itsm.local(END_USER), po@itsm.local(PROCESS_OWNER), admin@itsm.local(SYSTEM_ADMIN)
- 기존 시드 데이터(재현성 확인용, 2026-07-10 기준):
  - 변경(change_request): 키워드 "retest" 매칭 11건(CHG-2026-0002/0004/0005/0007~0014)
  - 지식(knowledge_article): 키워드 "retest" 매칭 3건(id 3 PUBLISHED, id 5/7 DRAFT, 작성자 kc@itsm.local)
  - 인시던트: 키워드 "retest" 매칭 4건(INC-2026-0014/0017/0018/0019)
  - 서비스요청 카탈로그명 "노트북 신청": requester_id=38 소유 9건(user@itsm.local 소유 없음)
  - CAB 승인 대기(APPROVAL/CAB): CHG-2026-0005/0006 2건, 서비스요청 승인 대기(APPROVAL_PENDING scope=mine 승인자향): 0건
  - 자산 만료 임박(30일 이내, 기준일 2026-07-10): AST-0001(warranty_expiry 2026-07-25), AST-0003(license_expiry 2026-07-20) = 2건

## 빌드 테스트

### TC-BUILD-001 · 프론트엔드/백엔드 빌드
- 근거: @docs/01_analyze/feature/ui-revamp.md, @docs/02_plan/screen/common.md
- 절차:
  1. `source/frontend`에서 `npm run build` 실행
  2. `source/backend`에서 `gradlew.bat build` 실행(또는 이미 최신 코드로 기동 중이면 컴파일 오류 없음만 확인)
- 기대 결과: 타입/컴파일 오류 없이 빌드 성공

## 시나리오 — 이슈 1: 사이드바 active 중복 버그

### TC-SIDEBAR-001 · 하위 경로 진입 시 하위 메뉴만 active(서비스요청 큐)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-003)
- 전제: agent@itsm.local(SERVICE_DESK_AGENT) 로그인, 사이드바에 "요청 목록"(/service-requests)과 "요청 큐"(/service-requests/queue) 동시 노출
- 절차:
  1. `/service-requests/queue`로 직접 이동
  2. 사이드바에서 "요청 목록"·"요청 큐" 두 메뉴의 활성(active) 강조 상태 확인
- 기대 결과: "요청 큐"만 active, "요청 목록"은 비활성

### TC-SIDEBAR-002 · 하위 경로 진입 시 하위 메뉴만 active(인시던트 지표)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-003)
- 전제: im@itsm.local(INCIDENT_MANAGER) 로그인
- 절차:
  1. `/incidents/metrics`로 직접 이동
  2. "인시던트"(/incidents)·"인시던트 지표"(/incidents/metrics) 활성 상태 확인
- 기대 결과: "인시던트 지표"만 active

### TC-SIDEBAR-003 · 하위 경로 진입 시 하위 메뉴만 active(변경 일정)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-003)
- 전제: cm@itsm.local(CHANGE_MANAGER) 로그인
- 절차:
  1. `/changes/schedule`로 직접 이동
  2. "변경"(/changes)·"변경 일정"(/changes/schedule) 활성 상태 확인
- 기대 결과: "변경 일정"만 active

### TC-SIDEBAR-004 · 지식 그룹 회귀 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-003)
- 전제: kc@itsm.local(KNOWLEDGE_CONTRIBUTOR) 로그인(지식베이스·기사 작성 메뉴 동시 노출)
- 절차:
  1. `/knowledge/new`로 직접 이동
  2. "지식베이스"(/knowledge)·"기사 작성"(/knowledge/new) 활성 상태 확인
- 기대 결과: "기사 작성"만 active

### TC-SIDEBAR-005 · 자산 그룹 회귀 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-003)
- 전제: am@itsm.local(ASSET_MANAGER) 로그인(자산·CI 관계 메뉴 동시 노출)
- 절차:
  1. `/assets/cis`로 직접 이동
  2. "자산"(/assets)·"CI·CMDB 관계"(/assets/cis) 활성 상태 확인
- 기대 결과: "CI·CMDB 관계"만 active

### TC-SIDEBAR-006 · 최상위 경로 직접 진입 회귀 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-003)
- 전제: agent@itsm.local(SERVICE_DESK_AGENT) 로그인
- 절차:
  1. `/service-requests`로 직접 이동
  2. 사이드바 활성 상태 확인
- 기대 결과: "요청 목록"만 active(다른 메뉴 중복 활성 없음)

## 시나리오 — 이슈 2-A: 헤더 알림벨(뱃지 + 대기함 이동)

### TC-NOTIF-001 · Approver 뱃지 = SRM+CAB 승인 대기 합, 대기 있는 함으로 이동
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002), @docs/02_plan/api_spec/change.md (API-CHG-007 승인 대기), @docs/02_plan/api_spec/service-request.md (API-SRM-012 승인 대기)
- 전제: cab@itsm.local(APPROVER) 로그인. 시드 기준 CAB 승인 대기 2건(CHG-2026-0005/0006), SRM 승인 대기 0건
- 절차:
  1. 로그인 후 헤더 알림 벨 뱃지 숫자 확인
  2. 알림 벨 클릭
- 기대 결과: 뱃지 = 2. 클릭 시 CAB 승인 대기 건수만 있으므로 `/approvals/changes`로 이동

### TC-NOTIF-002 · Asset Manager 뱃지 = 만료 임박 자산 건수, 클릭 시 필터 적용 이동
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002), @docs/02_plan/api_spec/asset.md (`expiringWithinDays`)
- 전제: am@itsm.local(ASSET_MANAGER) 로그인. 시드 기준 만료 임박(30일 이내) 자산 2건(AST-0001, AST-0003)
- 절차:
  1. 로그인 후 헤더 알림 벨 뱃지 숫자 확인
  2. 알림 벨 클릭
- 기대 결과: 뱃지 = 2. 클릭 시 `/assets`로 이동하며 만료 임박(30일) 필터가 적용된 목록(AST-0001, AST-0003 포함)이 표시됨

### TC-NOTIF-003 · 두 권한 모두 없는 계정은 뱃지 없음
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002 — "알림 없으면 뱃지 숨김")
- 전제: user@itsm.local(END_USER) 로그인
- 절차:
  1. 로그인 후 헤더 알림 벨 상태 확인
- 기대 결과: 뱃지 미노출(0건)

## 시나리오 — 이슈 2-B: 헤더 통합검색(지식+티켓 교차 도메인)

### TC-SEARCH-001 · 미리보기 드롭다운 및 결과 클릭 이동
- 근거: @docs/02_plan/api_spec/search.md (API-SEARCH-001), @docs/02_plan/screen/common.md (SCR-COM-002)
- 전제: cm@itsm.local(CHANGE_MANAGER) 로그인
- 절차:
  1. 헤더 검색창에 "retest" 입력(디바운스 대기)
  2. 드롭다운 미리보기 목록 확인(최대 8건)
  3. 미리보기 항목 하나 클릭
- 기대 결과: 드롭다운에 CHANGE 도메인 결과가 최대 8건 노출. 클릭한 항목의 변경 상세 화면(`/changes/{id}`)으로 이동

### TC-SEARCH-002 · 전체 결과 보기(페이지네이션)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-011), @docs/02_plan/api_spec/search.md (API-SEARCH-001)
- 전제: cm@itsm.local(CHANGE_MANAGER) 로그인
- 절차:
  1. 헤더 검색창에 "retest" 입력 후 Enter
  2. `/search?keyword=retest` 화면에서 결과 목록·페이지네이션 확인
- 기대 결과: 시드 기준 CHANGE 도메인 11건이 20건 단위 페이지네이션으로 표시(1페이지에 전부 표시). CHANGE 외 도메인(INCIDENT/PROBLEM/KNOWLEDGE) 결과는 노출되지 않음(CHANGE_MANAGER는 CHG API만 접근 가능)

### TC-SEARCH-003 · END_USER RBAC — 도메인 1차 필터 + 게시 기사만 + 본인 SRM만
- 근거: @docs/02_plan/api_spec/search.md (0.1절 RBAC), @docs/02_plan/security/authorization/end_user.md
- 전제: user@itsm.local(END_USER) 로그인. 사전 단계로 END_USER가 서비스 카탈로그에서 "VPN Access"(사유 입력) 신규 요청 1건 제출(본인 소유 SRM 티켓 확보)
- 절차:
  1. "retest" 검색 → 결과 확인(시드 기준 KNOWLEDGE PUBLISHED 1건만 매칭 대상, INCIDENT/PROBLEM/CHANGE는 매칭 데이터가 있어도 결과에서 제외되어야 함)
  2. "노트북 신청" 검색 → 결과 확인(해당 카탈로그 요청은 전부 다른 사용자 소유)
  3. "VPN Access" 검색 → 결과 확인(방금 제출한 본인 요청 포함 여부)
- 기대 결과:
  - "retest": KNOWLEDGE 결과 1건(id=3, PUBLISHED)만 노출, INCIDENT/PROBLEM/CHANGE 결과 0건(도메인 자체가 접근 불가라 매칭 데이터가 있어도 제외)
  - "노트북 신청": 0건(본인 소유 아님 — SRM 행 단위 스코프가 본인 요청만 허용함을 확인)
  - "VPN Access": 본인이 방금 제출한 요청 1건만 노출(타 사용자의 기존 VPN Access 요청은 제외)

### TC-SEARCH-004 · SERVICE_DESK_AGENT RBAC — SRM 전체·인시던트·지식 검색
- 근거: @docs/02_plan/api_spec/search.md (0.1절 RBAC), @docs/02_plan/security/authorization/service_desk_agent.md
- 전제: agent@itsm.local(SERVICE_DESK_AGENT) 로그인
- 절차:
  1. "노트북 신청" 검색 → 타인 소유 요청 포함 여부 확인(scope=all 상당)
  2. "retest" 검색 → INCIDENT·KNOWLEDGE 결과 확인, PROBLEM·CHANGE 결과 제외 확인
- 기대 결과:
  - "노트북 신청": 시드 기준 9건(requester_id=38 소유) 모두 노출(본인 소유 여부 무관)
  - "retest": KNOWLEDGE 1건(PUBLISHED) + INCIDENT 4건 노출, PROBLEM/CHANGE 결과 0건

### TC-SEARCH-005 · 크래시 없이 빈 결과 반환(APPROVER/PROCESS_OWNER/ASSET_MANAGER/SYSTEM_ADMIN)
- 근거: @docs/02_plan/api_spec/search.md (0.1절 RBAC — 접근 불가 도메인 조용히 스킵)
- 전제: cab@itsm.local(APPROVER), po@itsm.local(PROCESS_OWNER), am@itsm.local(ASSET_MANAGER), admin@itsm.local(SYSTEM_ADMIN) 각각 로그인
- 절차:
  1. 각 계정으로 "retest" 검색 수행
- 기대 결과: 4개 계정 모두 500 오류·크래시 없이 빈 결과(0건) 정상 반환("검색 결과가 없습니다" 안내)

### TC-SEARCH-006 · 상태 배지·정렬 일치 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-011)
- 전제: cm@itsm.local(CHANGE_MANAGER) 로그인
- 절차:
  1. "retest" 검색 → `/search?keyword=retest` 전체 결과 확인
  2. 결과 목록의 상태 배지 값과 DB `change_request.status` 값 비교
  3. `updatedAt` 내림차순 정렬 여부 확인
- 기대 결과: 각 행의 상태 배지가 실제 변경 상태와 일치, 결과가 `updatedAt` 기준 최신순으로 정렬됨
