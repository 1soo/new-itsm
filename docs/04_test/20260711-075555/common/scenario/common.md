# 통합 테스트 시나리오 — Common (사용자 가이드 모달, SCR-COM-012)

## 사전 조건
- 빌드 테스트 통과 (frontend `npm run build`)
- frontend http://localhost:5173, backend http://localhost:8080, docker `itsm-postgres` 기동 중
- POC 계정(비밀번호 공통 `Admin@1234`): cab@itsm.local(APPROVER, 단일 역할), am@itsm.local(ASSET_MANAGER, 단일 역할), admin@itsm.local(SYSTEM_ADMIN, 단일 역할)
- 데이터 제약: 현재 시드는 모든 POC 계정이 정확히 1개 역할만 보유(다중 역할 계정 없음, `user_role` 시드 확인). "역할 여러 개" 케이스는 실 데이터로 재현할 수 없어, 여러 단일 역할 계정으로 "내 역할" 고정 로직이 역할 코드마다 동일하게 동작함을 확인하고, 다중 선택 자체는 코드 검토(`user-guide-modal.tsx`의 `ROLES.filter(r => myRoleSet.has(r.code))` — 역할 개수와 무관하게 Set 멤버십으로 동일하게 동작)로 대체한다.

## 시나리오

### TC-BUILD-001 · 프론트엔드 빌드
- 근거: @docs/01_analyze/feature/common.md (FEAT-COM-002)
- 절차:
  1. `source/frontend`에서 `npm run build` 실행(신규 의존성 `@radix-ui/react-tabs`/`@radix-ui/react-accordion` 포함)
- 기대 결과: 타입/컴파일 오류 없이 빌드 성공

### TC-GUIDE-001 · "?" 아이콘 클릭 시 모달 오픈 및 기본 탭(개요)
- 근거: @docs/01_analyze/prd/common.md (REQ-COM-002), @docs/01_analyze/feature/common.md (FEAT-COM-002), @docs/02_plan/screen/common.md (SCR-COM-012)
- 전제: cab@itsm.local 로그인(새 컨텍스트)
- 절차:
  1. 헤더 "?" 아이콘("사용자 가이드") 클릭
  2. 모달 타이틀·기본 활성 탭 확인
  3. "개요" 탭 본문 텍스트 확인
- 기대 결과: 모달 오픈, 타이틀 "사용자 가이드", 기본 탭="개요", 본문이 `docs/01_analyze/feature/user-guide-content.md` 1절 문단과 일치

### TC-GUIDE-002 · "도메인 및 원칙" 탭 — 11개 아코디언, 기본 전부 접힘, 개별 펼침
- 근거: @docs/02_plan/screen/common.md (SCR-COM-012 — 도메인 아코디언, 역할 무관 전체 노출)
- 전제: cab@itsm.local 로그인(새 컨텍스트), 모달 오픈
- 절차:
  1. "도메인 및 원칙" 탭 클릭
  2. 아코디언 항목 개수 확인
  3. 임의 1개(예: "인증/계정/권한(auth)") 클릭해 펼침, 목적/핵심 원칙 텍스트 확인
- 기대 결과: 11개 도메인 아코디언 전체 노출(auth·service-request·incident·problem·change·knowledge·asset·esm·vulnerability·compliance·infra-monitoring), 최초 전부 접힘 상태, 클릭한 항목만 펼쳐지며 콘텐츠 문서 2절 표와 일치

### TC-GUIDE-003 · "역할별 수행 내용과 방법" 탭 — 내 역할 고정(단일 역할 계정 A)
- 근거: @docs/01_analyze/prd/common.md (REQ-COM-002 — 16개 역할), @docs/02_plan/screen/common.md (SCR-COM-012 — 내 역할 최상단 고정+기본 펼침)
- 전제: cab@itsm.local(APPROVER, 단일 역할) 로그인(새 컨텍스트), 모달 오픈
- 절차:
  1. "역할별 수행 내용과 방법" 탭 클릭
  2. 아코디언 총 개수, 최상단 항목, "내 역할" 배지, 펼침/접힘 상태 확인
- 기대 결과: 아코디언 총 16개(정의된 전체 역할). 최상단이 "승인자(CAB 멤버 포함)"이며 "내 역할" 배지 표시, 기본 펼쳐진 상태(페르소나·수행 내용 텍스트가 콘텐츠 문서 3절과 일치). 나머지 15개는 그 아래 접힌 상태로 나열, 개별 클릭 시 펼쳐짐

### TC-GUIDE-004 · "역할별 수행 내용과 방법" 탭 — 내 역할 고정(단일 역할 계정 B, 회귀)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-012)
- 전제: am@itsm.local(ASSET_MANAGER, 단일 역할) 로그인(새 컨텍스트), 모달 오픈
- 절차:
  1. "역할별 수행 내용과 방법" 탭 클릭
  2. 최상단 항목·배지·펼침 상태 확인
- 기대 결과: 최상단이 "자산 관리자"이며 "내 역할" 배지 표시, 기본 펼쳐짐. cab 계정과 다른 역할이 정확히 고정되어 계정별로 로직이 하드코딩되지 않았음을 확인

### TC-GUIDE-005 · 배경 클릭으로 모달 닫힘
- 근거: @docs/02_plan/screen/common.md (SCR-COM-012 — 배경 클릭/Esc/닫기 버튼으로 닫힘)
- 전제: cab@itsm.local 로그인(새 컨텍스트), 모달 오픈
- 절차:
  1. 모달 배경(오버레이) 영역 클릭
- 기대 결과: 모달이 닫힘

### TC-GUIDE-006 · Esc로 모달 닫힘
- 근거: @docs/02_plan/screen/common.md (SCR-COM-012)
- 전제: cab@itsm.local 로그인(새 컨텍스트), 모달 오픈
- 절차:
  1. Esc 키 입력
- 기대 결과: 모달이 닫힘

### TC-GUIDE-007 · 닫기 버튼으로 닫힘 및 재오픈 시 "개요" 탭으로 리셋
- 근거: @docs/02_plan/screen/common.md (SCR-COM-012 — "?" 클릭 시 항상 기본 탭="개요")
- 전제: cab@itsm.local 로그인(새 컨텍스트)
- 절차:
  1. 모달 오픈 → "도메인 및 원칙" 탭으로 전환
  2. 닫기(X) 버튼 클릭
  3. "?" 아이콘 재클릭으로 모달 재오픈
  4. 활성 탭 확인
- 기대 결과: 1차 닫기(X) 정상 동작. 재오픈 시 직전 탭이 아니라 "개요" 탭이 기본으로 표시됨

### TC-GUIDE-008 · 탭 전환 시 네트워크 요청 없음(정적 콘텐츠)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-012 — "콘텐츠는 정적 데이터로 제공되며 탭 전환 시 서버 재조회는 없다")
- 전제: cab@itsm.local 로그인(새 컨텍스트), 모달 오픈
- 절차:
  1. 모달 오픈 후 "개요"→"도메인 및 원칙"→"역할별 수행 내용과 방법" 순으로 탭 전환하며 네트워크 요청 캡처
- 기대 결과: 탭 전환 중 신규 XHR/fetch 요청 없음(정적 콘텐츠, 로그인 시 발급된 요청 이후 추가 호출 없음)

### TC-GUIDE-009 · 헤더 아이콘 배치 순서
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002 — "통합검색 - '?' - 테마 토글 - 알림 벨 - 사용자 메뉴" 순서)
- 전제: cab@itsm.local 로그인(새 컨텍스트)
- 절차:
  1. 헤더 우측 아이콘 버튼들의 DOM 순서 확인("사용자 가이드"/테마 토글/"알림"/사용자 메뉴)
- 기대 결과: "?"(사용자 가이드) 아이콘이 테마 토글 왼쪽에 배치되어 명시된 순서와 일치
