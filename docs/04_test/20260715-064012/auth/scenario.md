# 통합 테스트 시나리오 — auth/common (런타임 업그레이드 후 기능 회귀)

## 사전 조건
- 런타임 업그레이드(Java25/Boot4.1/PG18) 인프라 검증은 `docs/04_test/20260714-184455/runtime-upgrade/`에서 3/3 PASS 완료됨.
- 이번 라운드는 업그레이드된 스택(PG18 컨테이너 + Boot4.1 앱 + 프론트엔드) 위에서 **실제 UI/API 기능 동작**이 기존처럼 동작하는지 확인하는 목적(신규 기능 검증 아님).
- PG18 컨테이너(기존 볼륨 재사용) + 백엔드 bootRun + 프론트엔드 vite dev 기동 상태.
- 관리자 계정(admin@itsm.local / Admin@1234)으로 로그인.

## 시나리오

### TC-AUTH-101 · 로그인 및 세션 확립
- 근거: @docs/01_analyze/prd/auth.md, @docs/01_analyze/feature/auth.md
- 전제: 로그인 화면(/login) 접근 가능
- 절차:
  1. admin@itsm.local / Admin@1234로 로그인
  2. 로그인 후 랜딩 화면과 사이드바 내비게이션 렌더링 확인
- 기대 결과: 로그인 성공, RBAC에 따른 전체 도메인 사이드바 메뉴 노출(SYSTEM_ADMIN)

### TC-AUTH-102 · Access Token 메모리 저장 + Refresh Token 자동 재발급(하드 리로드 복원)
- 근거: @docs/06_maintenance/20260712-111803/auth/report.md (Access Token Client Memory 전환)
- 전제: 로그인된 상태
- 절차:
  1. `/` 로 하드 네비게이션(page.goto, 풀 리로드) 수행
  2. 콘솔에서 `/api/v1/auth/me` 401 발생 여부 확인
  3. 페이지가 대시보드로 정상 복원되는지 확인
  4. 관리자 감사 로그(`/admin/audit-logs`)에서 "토큰 재발급" 이벤트 기록 확인
- 기대 결과: 하드 리로드 직후 in-memory 토큰 소실로 1차 401 발생하나, httpOnly Refresh Token 쿠키로 자동 재인증되어 대시보드가 정상 렌더링됨. 감사 로그에 로그인/토큰 재발급 이벤트가 순서대로 기록됨(자동 재시도로 401이 해소되는 정상 흐름이며 결함 아님)

### TC-AUTH-103 · 관리자 계정 목록/감사 로그 조회
- 근거: @docs/01_analyze/feature/auth.md (SCR-ADMIN-001/005)
- 전제: SYSTEM_ADMIN 로그인 상태
- 절차:
  1. `/admin/users` 접근, 목록 데이터 렌더링 확인
  2. `/admin/audit-logs` 접근, 이벤트 목록 렌더링 확인
- 기대 결과: 두 화면 모두 데이터 정상 표시, 콘솔 에러 없음

### TC-COM-101 · 헤더 알림 정상 동작
- 근거: @docs/01_analyze/feature/common.md (헤더 알림)
- 전제: 로그인 상태
- 절차:
  1. 헤더 알림 버튼 클릭
  2. 알림 패널 렌더링 확인
- 기대 결과: "새로운 알림이 없습니다" 등 정상 렌더링, API 401 발생 시에도 자동 재시도로 최종 정상 응답
