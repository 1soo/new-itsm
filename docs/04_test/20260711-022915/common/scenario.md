# 통합 테스트 시나리오 — Common (헤더 알림 팝오버 v2 · 2줄 레이아웃 재테스트)

> 원본(v1) 시나리오·결과: `docs/04_test/common/20260711-015654/`(10건 전건 PASS). 본 재테스트는 v2(2줄 레이아웃·제목/상대시간) 신규 항목 위주로 검증하고, v1 항목은 회귀 확인 위주로 축약 수행한다.

## 사전 조건
- 빌드 테스트 통과 (frontend `npm run build`, backend `gradlew.bat build`)
- frontend http://localhost:5173, backend http://localhost:8080, docker `itsm-postgres` 기동 중(BE는 DTO 변경분 반영을 위해 재기동 완료)
- POC 계정(비밀번호 공통 `Admin@1234`): cab@itsm.local(APPROVER), am@itsm.local(ASSET_MANAGER), user@itsm.local(END_USER), admin@itsm.local(SYSTEM_ADMIN)
- 기존 시드 데이터(재현성 확인용, API 직접 조회로 사전 확인, 2026-07-11 기준):
  - 서비스요청 승인 대기(cab): SRM-2026-0007, `catalogItemName`="노트북 신청", `requestedAt`=2026-07-09T03:32:25Z(테스트 시점 기준 약 1~2일 전)
  - 변경 승인 대기(cab): CHG-2026-0006, `summary`="E2E RFC 생성 테스트", `createdAt`=2026-07-09T14:23:01Z(약 1일 전)
  - 자산 만료 임박(am): AST-0004/0003/0002/0001, `expiryDate` 각각 2020-01-01/2026-07-20/2020-05-01/2026-01-01
  - 위 제목 필드(catalogItemName/summary/자산명) 모두 40자 이하 — 현재 시드로는 v2 2행 truncate(40자 초과) 자체를 실 데이터로 재현 불가(v1의 조합 문구와 달리 순수 제목만 사용하므로 길이가 짧음). `truncateNotificationText` 함수는 v1과 동일(변경 없음)하며 v1에서 42자 입력으로 이미 실증 확인(PASS) 되었으므로 로직 회귀 위험은 낮음 — 코드 검토로 대체하고 별도 TC 미실행.

## 시나리오

### TC-BUILD-001 · 프론트엔드/백엔드 빌드
- 근거: @docs/01_analyze/feature/common.md (FEAT-COM-001), @docs/03_develop/plan/common.md (v2 담당 범위 — dev_be DTO 변경)
- 절차:
  1. `source/frontend`에서 `npm run build` 실행
  2. `source/backend`에서 `gradlew.bat build` 실행(DTO 필드 추가에 따른 단위 테스트 포함)
- 기대 결과: 타입/컴파일 오류 및 테스트 실패 없이 빌드 성공

### TC-NOTIF-101 · 2줄 레이아웃 구조 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002 v0.4 — 알림 드롭다운 각 라인 2줄 구조)
- 전제: cab@itsm.local(APPROVER) 로그인(새 컨텍스트)
- 절차:
  1. 알림 벨 클릭 → 팝오버 오픈
  2. 각 알림 항목의 1행(도메인 라벨+우측 시간 표시)·2행(제목) 구조 확인
- 기대 결과: 항목마다 1행에 도메인 Lozenge(좌)와 시간 텍스트(우)가, 2행에 제목이 별도 줄로 렌더링됨. "상세 보기" 버튼 존재

### TC-NOTIF-102 · 서비스요청 항목 — 제목(catalogItemName)·상대 시간
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002 v0.4 알림 드롭다운 항목 매핑 — 서비스요청 승인), @docs/02_plan/api_spec/service-request.md (API-SRM-012 `catalogItemName`)
- 전제: cab@itsm.local(APPROVER) 로그인(새 컨텍스트). SRM-2026-0007 `catalogItemName`="노트북 신청", `requestedAt`=2026-07-09T03:32:25Z
- 절차:
  1. 알림 벨 클릭 → 팝오버 오픈
  2. "서비스요청 승인" 항목의 2행 제목, 1행 우측 시간 텍스트 확인
- 기대 결과: 2행 제목 = "노트북 신청"(기존 v1의 "티켓키 · 요청자 승인 요청" 조합 문구 아님). 1행 우측 = `requestedAt` 기준 상대 시간(테스트 시점 경과 약 24~48시간 구간이므로 "N일 전" 형식, 7일 미만이라 절대 날짜 아님)

### TC-NOTIF-103 · 변경 항목 — 제목(summary)·상대 시간
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002 v0.4 — 변경 승인), @docs/02_plan/api_spec/change.md (API-CHG-007 `summary`/`createdAt`)
- 전제: cab@itsm.local(APPROVER) 로그인(새 컨텍스트). CHG-2026-0006 `summary`="E2E RFC 생성 테스트", `createdAt`=2026-07-09T14:23:01Z
- 절차:
  1. 알림 벨 클릭 → 팝오버 오픈
  2. "변경 승인" 항목의 2행 제목, 1행 우측 시간 텍스트 확인
- 기대 결과: 2행 제목 = "E2E RFC 생성 테스트". 1행 우측 = `createdAt` 기준 "N일 전"(7일 미만)

### TC-NOTIF-104 · 자산 만료 항목 — 제목(자산명)·만료일 표시(상대 시간 아님)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002 v0.4 — 자산 만료 "{expiryDate} 만료" 형식, 상대 시간 아님)
- 전제: am@itsm.local(ASSET_MANAGER) 로그인(새 컨텍스트)
- 절차:
  1. 알림 벨 클릭 → 팝오버 오픈
  2. 4개 자산 항목의 2행 제목(자산명), 1행 우측 표시 확인
- 기대 결과: 2행 제목이 각 자산명(AST-0004="Retest AWS Reserved Instance" 등)과 일치. 1행 우측이 "N일 전" 등 상대 시간이 아니라 `{expiryDate} 만료` 형식(목록 화면과 동일 날짜 포맷, 예: "2026. 7. 20. 만료")

### TC-NOTIF-105 · 팝오버 재오픈 시 시간 라벨 재계산
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002 v0.4 — "계산 기준 시각은 팝오버를 연 시점"), @docs/03_develop/plan/common.md (v2 dev_fe — "팝오버 오픈 시점 기준으로 재계산")
- 전제: cab@itsm.local(APPROVER) 로그인(새 컨텍스트). 실 대기시간 경과로 재현이 제한적이라 브라우저 시계를 인위적으로 전진시켜 재계산 여부를 검증(Playwright `Date.now` 오버라이드)
- 절차:
  1. 알림 벨 클릭 → 팝오버 오픈, "서비스요청 승인"/"변경 승인" 항목의 시간 텍스트(1차) 기록
  2. 팝오버를 닫음
  3. 페이지 컨텍스트의 `Date.now`를 +8일(691200000ms) 오프셋으로 오버라이드
  4. 알림 벨 재클릭 → 팝오버 재오픈, 동일 항목의 시간 텍스트(2차) 확인
- 기대 결과: 1차는 "N일 전"(7일 미만) 형식이었으나, 2차는 오프셋 적용으로 경과 시간이 7일을 초과해 절대 날짜 형식(예: "2026. 7. 9.")으로 변경됨 — 데이터 로딩 시점이 아닌 팝오버 오픈 시점 기준으로 재계산됨을 확인

### TC-NOTIF-106 · v1 회귀 — 정렬·뱃지·상세 이동(승인 대기 혼합)
- 근거: @docs/01_analyze/prd/common.md (REQ-COM-001), @docs/02_plan/screen/common.md (SCR-COM-002 — 정렬 서비스요청→변경→자산, 뱃지 카운트, 상세 이동)
- 전제: cab@itsm.local(APPROVER) 로그인(새 컨텍스트)
- 절차:
  1. 알림 벨 뱃지 숫자 확인
  2. 팝오버 오픈 → 항목 순서(서비스요청→변경) 확인
  3. "서비스요청 승인" 항목 "상세 보기" 클릭 → 이동 경로·팝오버 닫힘 확인
- 기대 결과: 뱃지 = 2. 1번째 "서비스요청 승인", 2번째 "변경 승인" 순서 유지. 클릭 시 `/service-requests/8`로 이동하고 팝오버 닫힘(v1과 동일)

### TC-NOTIF-107 · v1 회귀 — 자산 계정 뱃지·팝오버 폭·상세 이동
- 근거: @docs/02_plan/screen/common.md (SCR-COM-002 — 뱃지 카운트, 폭 320px, 상세 이동)
- 전제: am@itsm.local(ASSET_MANAGER) 로그인(새 컨텍스트)
- 절차:
  1. 알림 벨 뱃지 숫자 확인
  2. 팝오버 오픈 → 너비 측정
  3. 첫 번째 자산 항목 "상세 보기" 클릭 → 이동 경로 확인
- 기대 결과: 뱃지 = 4. 팝오버 폭 320px. 클릭 시 `/assets/8`로 이동

### TC-NOTIF-108 · v1 회귀 — 알림 없음 빈 상태
- 근거: @docs/01_analyze/feature/common.md (FEAT-COM-001 예외 처리)
- 전제: user@itsm.local(END_USER) 로그인(새 컨텍스트)
- 절차:
  1. 알림 벨 상태 확인
  2. 알림 벨 클릭
- 기대 결과: 뱃지 미노출. 팝오버에 "새로운 알림이 없습니다" 표시

### TC-NOTIF-109 · v1 회귀 — SYSTEM_ADMIN 역할 우회 시 자산 카테고리만 노출
- 근거: 역할 우회 로직(`hasAnyRole`/`isSystemAdmin`), @docs/02_plan/screen/common.md (SCR-COM-002)
- 전제: admin@itsm.local(SYSTEM_ADMIN) 로그인(새 컨텍스트)
- 절차:
  1. 알림 벨 뱃지 숫자 확인
  2. 팝오버 오픈 → 항목 카테고리 확인
- 기대 결과: 뱃지 = 4. 팝오버 항목 전부 "자산 만료" 카테고리(개인 배정 승인 대기 0건이라 정상)
