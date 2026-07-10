# 통합 테스트 시나리오 — Common (사용자 가이드 전용 화면 /guide, SCR-COM-012 v0.8)

> 이전(모달) 버전 통합 테스트: `docs/04_test/common/20260711-075555/`(전건 PASS, 폐기됨). 이번 재테스트는 모달→전용 화면 전환분을 검증한다.

## 사전 조건
- 빌드 테스트 통과 (frontend `npm run build`)
- frontend http://localhost:5173, backend http://localhost:8080, docker `itsm-postgres` 기동 중
- POC 계정(비밀번호 공통 `Admin@1234`): cab@itsm.local(APPROVER, 단일 역할), am@itsm.local(ASSET_MANAGER, 단일 역할)
- 데이터 제약: 이전 모달 테스트와 동일하게, 현재 시드는 모든 계정이 단일 역할만 보유. "내 역할" 고정 로직은 서로 다른 두 단일 역할 계정으로 검증하고, 다중 역할 자체는 코드 검토로 대체한다(로직은 이전 모달 버전과 동일, Set 멤버십 기반).

## 시나리오

### TC-BUILD-001 · 프론트엔드 빌드
- 근거: @docs/01_analyze/feature/common.md (FEAT-COM-002)
- 절차:
  1. `source/frontend`에서 `npm run build` 실행(`react-markdown` 신규 의존성, `tabs.tsx` 제거분 포함)
- 기대 결과: 타입/컴파일 오류 없이 빌드 성공

### TC-GUIDE-001 · "?" 클릭 시 `/guide` 라우팅 이동(모달 아님) 및 뒤로가기 복귀
- 근거: @docs/01_analyze/prd/common.md (REQ-COM-002), @docs/02_plan/screen/common.md (SCR-COM-012 v0.8)
- 전제: cab@itsm.local 로그인(새 컨텍스트), 홈(`/`)에 위치
- 절차:
  1. 헤더 "?" 아이콘("사용자 가이드") 클릭
  2. 이동한 URL 확인
  3. 브라우저 뒤로가기 수행 후 URL 확인
- 기대 결과: 클릭 시 모달이 아니라 `/guide`로 라우팅 이동(URL 변경). 뒤로가기 시 이전 화면(`/`)으로 복귀

### TC-GUIDE-002 · 문서 헤더·최초 진입 시 최상단(개요) 노출
- 근거: @docs/02_plan/screen/common.md (SCR-COM-012 v0.8 — Confluence 문서 페이지 스타일)
- 전제: cab@itsm.local 로그인(새 컨텍스트)
- 절차:
  1. `/guide` 진입
  2. 상단 문서 헤더(H1) 텍스트, 최초 스크롤 위치에서 보이는 섹션 확인
- 기대 결과: 상단에 "사용자 가이드" 타이틀 노출. 진입 직후 "개요" 섹션이 최상단에 위치(스크롤 없이 최상단부터 표시)

### TC-GUIDE-003 · 좌측 TOC 3개 링크 — 클릭 시 스크롤 이동 및 활성 강조
- 근거: @docs/03_develop/plan/common.md ("좌측 TOC는 3개 링크만 존재, 도메인/역할 개별 하위 링크 없음")
- 전제: cab@itsm.local 로그인(새 컨텍스트), `/guide` 진입
- 절차:
  1. 좌측 TOC 링크 개수·라벨 확인
  2. "역할별 수행 내용과 방법" 링크 클릭
  3. 클릭 후 해당 섹션이 뷰포트에 들어오는지, 그 링크가 활성 강조되는지 확인
- 기대 결과: TOC 링크 정확히 3개("개요"/"도메인 및 원칙"/"역할별 수행 내용과 방법"), 도메인/역할 개별 하위 링크 없음. 클릭 시 해당 섹션으로 스크롤, 활성 링크가 클릭한 항목으로 강조 전환

### TC-GUIDE-004 · "개요" 섹션 — 아코디언 없이 원문 순차 렌더링(굵게 서식 포함)
- 근거: @docs/03_develop/plan/common.md ("개요"(1절)만 아코디언 없이 원문 그대로 순차 Markdown 렌더링)
- 전제: cab@itsm.local 로그인(새 컨텍스트), `/guide` 진입
- 절차:
  1. "개요" 섹션에 아코디언(버튼) 요소가 있는지 확인
  2. 본문 텍스트가 `user-guide-content.md`(v0.2) 1절 원문과 일치하는지, 굵게(`**...**`) 표기가 실제 `<strong>` 렌더링으로 반영되는지 확인
  3. 2절(도메인)·3절(역할) 텍스트가 개요 섹션 안에 포함되지 않는지 확인
- 기대 결과: 개요 섹션에 아코디언 없음(순수 텍스트), 원문과 일치, 굵게 서식 정상 렌더링. 2·3절 내용 미포함

### TC-GUIDE-005 · "도메인 및 원칙" 섹션 — 11개 아코디언, 기본 전부 접힘, v0.2 서술형 문구
- 근거: @docs/02_plan/screen/common.md (SCR-COM-012 v0.8), @docs/01_analyze/feature/user-guide-content.md (v0.2, 2절)
- 전제: cab@itsm.local 로그인(새 컨텍스트), `/guide` 진입
- 절차:
  1. "도메인 및 원칙" 섹션 아코디언 개수 확인
  2. "인증/계정/권한 (Auth & RBAC)" 항목 클릭해 펼침, 본문(핵심 원칙 포함) 확인
- 기대 결과: 11개 도메인 아코디언 전체 노출(역할 무관), 최초 전부 접힘. 클릭한 항목만 펼쳐지며 v0.2 서술형 본문(콘텐츠 문서 2.1절)과 일치, "핵심 원칙" 굵게 표기 렌더링

### TC-GUIDE-006 · "역할별 수행 내용과 방법" 섹션 — 내 역할 고정(계정 A: cab/APPROVER)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-012 v0.8 — 내 역할 최상단 고정+기본 펼침), @docs/01_analyze/feature/user-guide-content.md (v0.2, 3.4절)
- 전제: cab@itsm.local(APPROVER, 단일 역할) 로그인(새 컨텍스트), `/guide` 진입
- 절차:
  1. "역할별 수행 내용과 방법" 섹션 아코디언 총 개수, 최상단 항목·배지·펼침 상태 확인
  2. 최상단 항목(펼쳐진 상태) 본문이 v0.2 3.4절(정우진 부장 페르소나·구체 메뉴/버튼) 내용과 일치하는지 확인
- 기대 결과: 아코디언 총 16개. 최상단이 "APPROVER — 승인자 (CAB 멤버 포함)"이며 "내 역할" 배지+기본 펼침. 본문에 "정우진 부장", "승인 대기함", "CAB 승인 대기함" 등 v0.2 서술형 문구 포함. 나머지 15개는 접힌 상태

### TC-GUIDE-007 · "역할별 수행 내용과 방법" 섹션 — 내 역할 고정(계정 B: am/ASSET_MANAGER, 회귀)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-012 v0.8)
- 전제: am@itsm.local(ASSET_MANAGER, 단일 역할) 로그인(새 컨텍스트), `/guide` 진입
- 절차:
  1. "역할별 수행 내용과 방법" 섹션 최상단 항목·배지·본문 확인
- 기대 결과: 최상단이 "ASSET_MANAGER — 자산 관리자"이며 "내 역할" 배지+기본 펼침, 본문에 "임하윤 주임" 등 v0.2 3.11절 문구 포함. cab 계정과 다른 역할이 정확히 고정되어 계정별 데이터 분기가 하드코딩이 아님을 확인

### TC-GUIDE-008 · 정적 콘텐츠 — 네트워크 요청 없음
- 근거: @docs/03_develop/plan/common.md (신규 API 없음, 전부 정적 콘텐츠)
- 전제: cab@itsm.local 로그인(새 컨텍스트)
- 절차:
  1. 로그인 완료 후 네트워크 요청 캡처 시작
  2. `/guide` 진입, TOC 3개 링크 순회 클릭, 도메인/역할 아코디언 1개씩 펼침
- 기대 결과: `/guide` 진입 및 상호작용 과정에서 신규 XHR/fetch 요청 없음(정적 콘텐츠)

### TC-GUIDE-009 · 이전 모달 방식 완전 제거(회귀 없이 대체)
- 근거: @docs/03_develop/plan/common.md ("위 '사용자 가이드 모달' 섹션은 폐기되고 전용 화면으로 대체된다")
- 전제: cab@itsm.local 로그인(새 컨텍스트)
- 절차:
  1. "?" 아이콘 클릭 후 모달성 오버레이(`role="dialog"`) 존재 여부 확인
  2. 소스 코드 확인: `user-guide-modal.tsx`/`components/ui/tabs.tsx` 파일 잔존 여부, 헤더의 `guideOpen` state·`UserGuideModal` 렌더링 잔존 여부
- 기대 결과: "?" 클릭 시 다이얼로그(모달) 요소가 나타나지 않고 페이지 이동만 발생. 폐기 대상 파일(`user-guide-modal.tsx`, `components/ui/tabs.tsx`)이 삭제되어 존재하지 않고, `header.tsx`에 모달 관련 state·렌더링이 남아있지 않음(오브젼 코드 없음)
