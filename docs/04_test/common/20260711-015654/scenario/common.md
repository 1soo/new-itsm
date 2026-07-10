# 통합 테스트 시나리오 — Common (헤더 알림 팝오버)

## 사전 조건
- 빌드 테스트 통과 (frontend `npm run build`)
- frontend http://localhost:5173, backend http://localhost:8080, docker `itsm-postgres` 기동 중
- POC 계정(비밀번호 공통 `Admin@1234`): cab@itsm.local(APPROVER), am@itsm.local(ASSET_MANAGER), user@itsm.local(END_USER), admin@itsm.local(SYSTEM_ADMIN)
- 기존 시드 데이터(재현성 확인용, API 직접 조회로 사전 확인, 2026-07-11 기준):
  - 서비스요청 승인 대기(scope=mine, cab): 1건 — SRM-2026-0007(requester=req)
  - 변경 승인 대기(scope=mine, cab): 1건 — CHG-2026-0006(type=STANDARD, risk 미지정, requester=cm@itsm.local)
  - 자산 만료 임박(30일 이내, am): 4건 — AST-0004/0003/0002/0001(API 응답 순서, id 내림차순)
  - admin(SYSTEM_ADMIN, 역할 검증 우회): 승인 대기(scope=mine) 서비스요청/변경 각 0건(개인 배정 없음), 자산 만료 임박 4건
- 데이터 제약: 현재 시드로는 한 계정이 3개 알림 유형(서비스요청 승인·변경 승인·자산 만료)을 동시에 8건 초과로 보유하지 않아, "8건 상한(slice)" 자체의 UI 트리거는 실 데이터로 재현 불가 — 코드 검토(`AppLayout.tsx` `items.slice(0, NOTIFICATION_PREVIEW_SIZE)`)로 확인하고 별도 TC로 실행하지 않는다.

## 시나리오

### TC-BUILD-001 · 프론트엔드 빌드
- 근거: @docs/01_analyze/feature/common.md (FEAT-COM-001)
- 절차:
  1. `source/frontend`에서 `npm run build` 실행
- 기대 결과: 타입/컴파일 오류 없이 빌드 성공

### TC-NOTIF-001 · 알림 없음 — 빈 상태 안내 및 뱃지 숨김
- 근거: @docs/01_analyze/prd/common.md (REQ-COM-001), @docs/01_analyze/feature/common.md (FEAT-COM-001 예외 처리 — 표시할 알림이 없으면 빈 상태 안내), @docs/02_plan/screen/common.md (SCR-COM-002)
- 전제: user@itsm.local(END_USER, 승인자/자산관리자 아님) 로그인(새 컨텍스트)
- 절차:
  1. 로그인 후 헤더 알림 벨 상태 확인
  2. 알림 벨 클릭
- 기대 결과: 뱃지 미노출(0건). 팝오버가 열리고 "새로운 알림이 없습니다" 안내 표시

### TC-NOTIF-002 · 승인 대기 혼합 — 순서(서비스요청→변경)·텍스트 포맷·뱃지
- 근거: @docs/01_analyze/prd/common.md (REQ-COM-001), @docs/02_plan/screen/common.md (SCR-COM-002 알림 드롭다운 항목 매핑·정렬)
- 전제: cab@itsm.local(APPROVER) 로그인(새 컨텍스트). 서비스요청 승인 대기 1건 + 변경 승인 대기 1건
- 절차:
  1. 로그인 후 헤더 알림 벨 뱃지 숫자 확인
  2. 알림 벨 클릭 → 팝오버 목록 확인(항목 순서·도메인 라벨·텍스트)
- 기대 결과:
  - 뱃지 = 2
  - 1번째 항목: 도메인 라벨 "서비스요청 승인", 텍스트 "SRM-2026-0007 · req 승인 요청"
  - 2번째 항목: 도메인 라벨 "변경 승인", 텍스트(원문 42자 → 40자 초과로 말줄임) "CHG-2026-0006 · 표준/- · cm@itsm.local 승인 …"
  - 각 항목에 "상세 보기" 버튼 노출

### TC-NOTIF-003 · 자산 만료 임박 — 항목 4건·텍스트 포맷/truncate·뱃지·팝오버 크기
- 근거: @docs/01_analyze/prd/common.md (REQ-COM-001), @docs/02_plan/screen/common.md (SCR-COM-002 — 폭 320px 고정)
- 전제: am@itsm.local(ASSET_MANAGER) 로그인(새 컨텍스트). 자산 만료 임박 4건
- 절차:
  1. 로그인 후 헤더 알림 벨 뱃지 숫자 확인
  2. 알림 벨 클릭 → 팝오버 목록 확인(항목 4건, 도메인 라벨, 텍스트, 팝오버 너비)
- 기대 결과:
  - 뱃지 = 4
  - 4개 항목 모두 도메인 라벨 "자산 만료", 각 텍스트가 `{assetKey} · {name} · {expiryDate} 만료 예정` 포맷(4건 모두 40자 초과라 말줄임표로 종료)
  - 팝오버 폭 320px(트리거인 벨 버튼 폭에 종속되지 않음)

### TC-NOTIF-004 · "상세 보기" 클릭 — 서비스요청 상세 이동 및 팝오버 닫힘
- 근거: @docs/01_analyze/prd/common.md (REQ-COM-001 — 상세보기 클릭 시 해당 상세 화면 이동), @docs/02_plan/screen/common.md (SCR-COM-002)
- 전제: cab@itsm.local(APPROVER) 로그인(새 컨텍스트)
- 절차:
  1. 알림 벨 클릭 → 팝오버 오픈
  2. "서비스요청 승인" 항목의 "상세 보기" 버튼 클릭
- 기대 결과: `/service-requests/8`로 이동하고 팝오버가 닫힘

### TC-NOTIF-005 · "상세 보기" 클릭 — 변경 상세 이동 및 팝오버 닫힘
- 근거: @docs/01_analyze/prd/common.md (REQ-COM-001), @docs/02_plan/screen/common.md (SCR-COM-002)
- 전제: cab@itsm.local(APPROVER) 로그인(새 컨텍스트)
- 절차:
  1. 알림 벨 클릭 → 팝오버 오픈
  2. "변경 승인" 항목의 "상세 보기" 버튼 클릭
- 기대 결과: `/changes/9`로 이동하고 팝오버가 닫힘

### TC-NOTIF-006 · 라인 클릭(버튼 외 영역) — 자산 상세 이동 및 팝오버 닫힘
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002 — "각 알림 라인의 '상세 보기' 버튼(또는 라인 클릭)")
- 전제: am@itsm.local(ASSET_MANAGER) 로그인(새 컨텍스트)
- 절차:
  1. 알림 벨 클릭 → 팝오버 오픈
  2. 첫 번째 자산 항목의 "상세 보기" 버튼이 아닌 라인(도메인 배지/텍스트 영역) 클릭
- 기대 결과: 해당 자산 상세(`/assets/{id}`)로 이동하고 팝오버가 닫힘

### TC-NOTIF-007 · 벨 클릭 시 즉시 이동 대신 팝오버 오픈(회귀 — 기존 즉시 이동 동작 대체 확인)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002 — "벨 클릭 시 ... 즉시 target으로 navigate하지 않고 팝오버를 연다")
- 전제: cab@itsm.local(APPROVER) 로그인(새 컨텍스트)
- 절차:
  1. 로그인 후 현재 URL 기록
  2. 알림 벨 클릭 직후 URL 변화 여부 확인
- 기대 결과: URL이 즉시 변경되지 않고(이전 즉시 이동 동작 없음) 팝오버만 열림

### TC-NOTIF-008 · 외부 클릭으로 팝오버 닫힘
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002 — "외부 클릭/Esc로 닫힘")
- 전제: cab@itsm.local(APPROVER) 로그인(새 컨텍스트)
- 절차:
  1. 알림 벨 클릭 → 팝오버 오픈 확인
  2. 팝오버 외부 영역(예: 로고) 클릭
- 기대 결과: 팝오버가 닫힘

### TC-NOTIF-009 · SYSTEM_ADMIN 역할 우회 — 승인 대기 미배정 시 자산 카테고리만 노출(회귀)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002), 역할 우회 로직(`hasAnyRole`/`isSystemAdmin`)
- 전제: admin@itsm.local(SYSTEM_ADMIN, 역할 검증 전체 우회) 로그인(새 컨텍스트). scope=mine 개인 배정 승인 대기 0건, 자산 만료 임박 4건
- 절차:
  1. 로그인 후 헤더 알림 벨 뱃지 숫자 확인
  2. 알림 벨 클릭 → 팝오버 목록 확인
- 기대 결과: 뱃지 = 4. 팝오버 항목 전부 "자산 만료" 카테고리만 노출(서비스요청/변경 승인 항목 없음 — 개인 배정 없음에 따른 정상 동작, 오류 아님)
