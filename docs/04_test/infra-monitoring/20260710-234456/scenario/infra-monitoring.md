# 통합 테스트 시나리오 — Infra Monitoring (IT 인프라 모니터링 & 용량관리, IOM)

## 사전 조건
- 빌드 테스트 통과 (frontend `npm run build`, backend `gradlew.bat build`)
- frontend http://localhost:5173, backend http://localhost:8080, docker `itsm-postgres` 기동 중
- POC 계정(비밀번호 공통 `Admin@1234`): io@itsm.local(INFRA_OPERATOR), am@itsm.local(ASSET_MANAGER, 회귀 403 확인용)
- 시드 데이터(2026-07-10 기준):
  - 자산: AST-0001(id=5, 가동률 목표 99% 설정됨), AST-0002(id=6, 가동률 목표 미설정)
  - 지표: AST-0001 CPU(45.20/92.30/97.50), UPTIME(99.95/98.10) / AST-0002 RESPONSE_TIME(550.00 등 임계치 초과분 포함)
  - 임계치: CPU upperLimit=90, RESPONSE_TIME upperLimit=500 (MEMORY/UPTIME 미설정)
  - 알림: id=1(AST-0001 CPU 92.30 UPPER), id=2(AST-0002 RESPONSE_TIME 550.00 UPPER), id=3(AST-0001 CPU 97.50 UPPER), 모두 acknowledged=false
  - 용량 계획: 인프라운영팀 서버 용량(65%), 네트워크팀 대역폭(96%), 클라우드 서비스 인스턴스(120%, Danger 케이스)

## 시나리오

### TC-IOM-BUILD-001 · 프론트엔드/백엔드 빌드
- 근거: `docs/03_develop/plan/infra-monitoring.md` 5절 완료 기준
- 절차: `source/frontend`에서 `npm run build`, `source/backend`에서 `gradlew.bat build` 실행
- 기대 결과: 타입/컴파일 오류 없이 빌드 성공

### TC-IOM-001 · 지표 등록 — 필수값·존재 검증
- 근거: @docs/01_analyze/prd/infra-monitoring.md (REQ-IOM-001 Unwanted), @docs/01_analyze/feature/infra-monitoring.md (FEAT-IOM-001), @docs/02_plan/api_spec/infra-monitoring.md (API-IOM-001)
- 전제: io@itsm.local 로그인
- 절차:
  1. assetId 없이 등록 시도(metricType/value만 입력)
  2. value 없이 등록 시도(assetId/metricType만 입력)
  3. 존재하지 않는 assetId(999999)로 등록 시도
  4. 유효한 assetId(5)·metricType(MEMORY)·value(50)로 등록
- 기대 결과: (1)(2) 400 (3) 404 (4) 201, 지표 레코드 생성

### TC-IOM-002 · 지표 등록 — 임계치 초과 알림 생성
- 근거: @docs/01_analyze/prd/infra-monitoring.md (REQ-IOM-003), @docs/01_analyze/feature/infra-monitoring.md (FEAT-IOM-003), @docs/02_plan/api_spec/infra-monitoring.md (API-IOM-001, 0절)
- 전제: io@itsm.local 로그인, CPU 임계치 upperLimit=90 설정됨
- 절차:
  1. assetId=5, metricType=CPU, value=95(임계치 초과)로 등록
  2. 응답의 `alertGenerated` 확인
  3. 알림 목록(GET /metric-alerts?assetId=5)에서 신규 알림 반영 확인
- 기대 결과: (1)(2) 201, `alertGenerated: true` (3) 신규 알림(threshold_type=UPPER) 목록에 노출

### TC-IOM-003 · 지표 등록 — 임계치 미설정 항목은 알림 미생성
- 근거: @docs/01_analyze/feature/infra-monitoring.md (FEAT-IOM-003 Unwanted), @docs/02_plan/api_spec/infra-monitoring.md (API-IOM-001, 0절)
- 전제: io@itsm.local 로그인, MEMORY 임계치 미설정
- 절차: assetId=5, metricType=MEMORY, value=99999(비정상적으로 큰 값)로 등록
- 기대 결과: 201, `alertGenerated: false`(임계치 비교 자체가 생략됨)

### TC-IOM-004 · 지표 시계열 조회 — 필터·빈 결과
- 근거: @docs/01_analyze/prd/infra-monitoring.md (REQ-IOM-002), @docs/02_plan/api_spec/infra-monitoring.md (API-IOM-002)
- 전제: io@itsm.local 로그인
- 절차:
  1. assetId=5로 조회, 측정 시각 순 정렬 확인
  2. 데이터 없는 미래 기간(from=2030-01-01, to=2030-01-31)으로 조회
- 기대 결과: (1) 시각 오름차순 정렬된 지표 목록 (2) 빈 배열

### TC-IOM-005 · 임계치 목록 조회·설정
- 근거: @docs/01_analyze/prd/infra-monitoring.md (REQ-IOM-003 Ubiquitous), @docs/02_plan/api_spec/infra-monitoring.md (API-IOM-003/004)
- 전제: io@itsm.local 로그인
- 절차:
  1. 임계치 목록 조회(CPU/RESPONSE_TIME 존재 확인)
  2. MEMORY 임계치 upperLimit=85로 설정(PUT)
  3. 재조회로 반영 확인
- 기대 결과: (1) 기존 2건 확인 (2) 200 (3) MEMORY 임계치 반영, 이후 MEMORY 지표 등록 시 알림 생성 대상이 됨

### TC-IOM-006 · 알림 목록 필터·확인 처리
- 근거: @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-003), @docs/02_plan/api_spec/infra-monitoring.md (API-IOM-005/006)
- 전제: TC-IOM-002에서 생성된 알림 존재
- 절차:
  1. assetId/acknowledged 필터로 알림 목록 조회
  2. 신규 생성된 알림 1건 확인 처리(PATCH acknowledge)
  3. 존재하지 않는 알림 id(999999) 확인 처리 시도
  4. FE 알림 목록에서 확인된 알림이 흐리게 표시되는지 확인
- 기대 결과: (1) 필터별 정상 축소 (2) 200, acknowledged=true로 전환 (3) 404 (4) 확인된 알림 시각적 구분(흐리게) 표시

### TC-IOM-007 · SLA 대비 가동률 — 목표 있음/없음
- 근거: @docs/01_analyze/prd/infra-monitoring.md (REQ-IOM-005), @docs/02_plan/api_spec/infra-monitoring.md (API-IOM-007/008)
- 전제: io@itsm.local 로그인, AST-0001(목표 99% 설정됨)/AST-0002(목표 미설정)
- 절차:
  1. AST-0001(assetId=5) 가동률 현황 조회
  2. AST-0002(assetId=6) 가동률 현황 조회(목표 미설정)
  3. AST-0002에 가동률 목표(targetPercentage=95) 신규 설정(PUT) 후 재조회
  4. 존재하지 않는 자산 가동률 목표 설정 시도
- 기대 결과: (1) targetPercentage/actualPercentage/met 모두 값 존재 (2) met=null, actualPercentage만 표시(또는 UPTIME 지표 없어 null) (3) 설정 후 met 값(true/false) 산출 (4) 404

### TC-IOM-008 · 용량 계획 — 필수값 검증·활용률 계산
- 근거: @docs/01_analyze/prd/infra-monitoring.md (REQ-IOM-004), @docs/02_plan/api_spec/infra-monitoring.md (API-IOM-009/010)
- 전제: io@itsm.local 로그인
- 절차:
  1. capacity 없이 등록 시도
  2. demand 없이 등록 시도
  3. teamOrService="QA팀 테스트 환경", capacity=100, demand=150으로 등록(활용률 150%)
  4. 목록 조회로 활용률(1.5, 100% 초과) 확인, 기존 120% 케이스(클라우드 서비스 인스턴스)도 Danger 배지 확인
- 기대 결과: (1)(2) 400 (3) 201 (4) utilizationRate=1.5 정상 계산, FE에서 100% 초과 항목 Danger 배지 표시

### TC-IOM-009 · 인프라 지표 리포팅 — 집계·빈 결과
- 근거: @docs/01_analyze/prd/infra-monitoring.md (REQ-IOM-006), @docs/02_plan/api_spec/infra-monitoring.md (API-IOM-011)
- 전제: io@itsm.local 로그인
- 절차:
  1. 필터 없이 전체 리포팅 조회(avgUptime/avgCpu/avgMemory/avgResponseTime/avgCapacityUtilization 값 확인)
  2. assetId=5로 필터링해 조회
  3. 데이터 없는 미래 기간으로 조회
- 기대 결과: (1)(2) 실제 시드 데이터 기반 평균값 산출 (3) 0/빈 결과, 오류 없음

### TC-IOM-010 · FE E2E — 지표 등록→대시보드→임계치/알림→용량계획→리포팅
- 근거: @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-001~005)
- 전제: io@itsm.local 로그인(새 브라우저 컨텍스트)
- 절차:
  1. 사이드바에서 인프라모니터링 메뉴 진입, SCR-IOM-001에서 지표 등록(임계치 초과 값 입력) 후 토스트 확인
  2. SCR-IOM-002 대시보드에서 자산·기간 선택, 시계열 표시 확인, SLA 대비 가동률 카드 확인(목표 설정 폼 포함)
  3. SCR-IOM-003에서 임계치 설정 폼·알림 목록·확인 처리 버튼 동작 확인
  4. SCR-IOM-004에서 용량 계획 등록 폼·목록·활용률 배지 확인
  5. SCR-IOM-005에서 기간·자산 필터+KPI 카드 확인
- 기대 결과: 각 화면 정상 렌더링·데이터 반영, 임계치 초과 등록 시 토스트 안내

### TC-IOM-011 · RBAC — INFRA_OPERATOR 외 전 역할 403 + 사이드바 미노출
- 근거: @docs/02_plan/security/authorization/infra_operator.md
- 전제: am@itsm.local(ASSET_MANAGER) 로그인(새 브라우저 컨텍스트)
- 절차:
  1. 사이드바에 인프라모니터링 관련 메뉴 노출 여부 확인
  2. `GET /api/v1/infra/metrics` 직접 호출
  3. 인프라모니터링 화면 URL 직접 진입
- 기대 결과: (1) 메뉴 미노출 (2) 403 (3) `/403` 리다이렉트
