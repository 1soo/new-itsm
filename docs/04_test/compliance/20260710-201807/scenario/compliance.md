# 통합 테스트 시나리오 — 컴플라이언스 관리 (Compliance)

## 사전 조건
- 빌드 테스트 통과 (frontend `npm run build`, backend `./gradlew build` 또는 동등)
- dev 서버 기동: frontend(:5173) · backend(최신 코드) · DB(docker)
- 테스트 계정(공통 비밀번호 `Admin@1234`)
  - COMPLIANCE_OFFICER: `co@itsm.local`
  - 회귀 확인용 CHANGE_MANAGER: `cm@itsm.local`
- 시드 데이터: `COMP-2026-0001`(책임자 지정, 시정조치 없음 → COMPLIANT), `COMP-2026-0002`(책임자 미지정, DETECTED 시정조치 → NON_COMPLIANT), `COMP-2026-0003`(책임자 지정, RESOLVED 시정조치 → COMPLIANT)
- 변경 연계 테스트용 기존 변경 요청 존재(예: CHG-2026-0001 등)

## 시나리오

### TC-COMP-000 · 빌드 테스트
- 근거: 공통 개발 표준(react-development/spring-boot-development 빌드 테스트 규정)
- 절차:
  1. frontend 빌드 수행
  2. backend 빌드 수행
- 기대 결과: 두 빌드 모두 오류 없이 성공.

### TC-COMP-001 · 요구사항 등록 — 필수값 누락 400
- 근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-001 Unwanted), @docs/01_analyze/feature/compliance.md (FEAT-COMP-001), @docs/02_plan/api_spec/compliance.md (API-COMP-002)
- 전제: co@itsm.local 로그인
- 절차:
  1. SCR-COMP-002 이동
  2. 이름 또는 근거를 비운 채 등록 시도
- 기대 결과: 400 오류(생성 거부), 폼에 에러 표시.

### TC-COMP-002 · 요구사항 등록 성공
- 근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-001), @docs/02_plan/screen/compliance.md (SCR-COMP-002)
- 절차:
  1. 이름·근거·적용범위 입력 후 등록
- 기대 결과: 요구사항 생성(식별키 `COMP-YYYY-####` 반환), 상세 화면(SCR-COMP-003)으로 이동.

### TC-COMP-003 · 목록 필터·배지
- 근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-002 Unwanted), @docs/02_plan/screen/compliance.md (SCR-COMP-001)
- 절차:
  1. SCR-COMP-001 이동
  2. 준수상태(COMPLIANT/NON_COMPLIANT) 필터 적용
  3. 책임자 지정 여부 필터 적용
- 기대 결과: 필터 결과 정확, NON_COMPLIANT 항목 Danger 배지, 책임자 미지정 항목 "책임자 미지정" 배지 표시.

### TC-COMP-004 · 책임자 지정 — 존재하지 않는 사용자 400
- 근거: @docs/02_plan/api_spec/compliance.md (API-COMP-006)
- 전제: TC-COMP-002에서 생성한 요구사항 상세
- 절차:
  1. 존재하지 않는 사용자 ID로 책임자 지정 요청
- 기대 결과: 400 오류.

### TC-COMP-005 · 책임자 지정 성공
- 근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-002), @docs/02_plan/api_spec/compliance.md (API-COMP-006)
- 절차:
  1. 유효한 사용자로 책임자 지정
- 기대 결과: 지정 내역 저장·표시, "책임자 미지정" 배지 해제.

### TC-COMP-006 · 시정조치 등록 → 준수상태 NON_COMPLIANT 전환
- 근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-003), @docs/02_plan/api_spec/compliance.md (API-COMP-007, 0절 준수상태 계산)
- 전제: COMPLIANT 상태 요구사항(예: COMP-2026-0001)
- 절차:
  1. 시정조치 등록(내용 입력) → DETECTED 생성
  2. 요구사항 준수 상태 재조회
- 기대 결과: 시정조치 DETECTED로 생성, 요구사항 준수상태 NON_COMPLIANT로 전환(목록·상세 일관).

### TC-COMP-007 · 시정조치 상태 전이 — 순서 위반 400
- 근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-003 Unwanted), @docs/02_plan/api_spec/compliance.md (API-COMP-008)
- 전제: TC-COMP-006에서 생성한 DETECTED 시정조치
- 절차:
  1. DETECTED → RESOLVED로 직접 전이 시도(IN_PROGRESS 건너뜀)
- 기대 결과: 400 오류.

### TC-COMP-008 · 시정조치 순차 전이 → 준수상태 COMPLIANT 복귀
- 근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-003), @docs/02_plan/api_spec/compliance.md (API-COMP-008, 0절)
- 절차:
  1. DETECTED → IN_PROGRESS 전이
  2. IN_PROGRESS → RESOLVED 전이
  3. 해당 요구사항의 미해결 시정조치가 모두 RESOLVED인지 확인 후 준수상태 재조회
- 기대 결과: 순차 전이 성공, 미해결 시정조치 0건이 되어 요구사항 준수상태 COMPLIANT로 복귀.

### TC-COMP-009 · 요구사항 인라인 수정
- 근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-001), @docs/02_plan/api_spec/compliance.md (API-COMP-004)
- 절차:
  1. 상세에서 이름·근거·적용범위 수정 후 저장
- 기대 결과: 수정 내용 반영·재조회 시 유지.

### TC-COMP-010 · 변경 요청 연계 — 존재하지 않는 변경 400
- 근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-005 Unwanted), @docs/02_plan/api_spec/compliance.md (API-COMP-005)
- 절차:
  1. 존재하지 않는 변경 ID로 연계 시도
- 기대 결과: 400 오류.

### TC-COMP-011 · 변경 요청 연계 성공 + 변경 상세 역노출
- 근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-005), @docs/02_plan/api_spec/compliance.md (API-COMP-005, 0절 links 노출)
- 절차:
  1. 존재하는 변경 요청 ID로 연계
  2. 요구사항 상세에서 연계된 변경 확인
  3. cm@itsm.local로 로그인 후 해당 변경 상세(SCR-CHG-*) 조회
- 기대 결과: 연계 성공, 요구사항 상세에 연결된 변경 표시, 변경 상세 `links`에 `COMPLIANCE_REQUIREMENT` 타입으로 함께 노출.

### TC-COMP-012 · 컴플라이언스 감사 로그 조회
- 근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-004), @docs/02_plan/api_spec/compliance.md (API-COMP-009, 0절 감사로그 트랜잭션·조회범위)
- 절차:
  1. TC-COMP-002/009/006/008에서 발생한 요구사항의 감사 로그 목록 조회(SCR-COMP-003 감사 로그 영역)
- 기대 결과: `COMPLIANCE_REQ_CREATE`(등록)·`COMPLIANCE_REQ_UPDATE`(수정)·`COMPLIANCE_ACTION_STATUS_CHANGE`(전이) 이벤트가 모두 조회됨.

### TC-COMP-013 · 준수 현황 대시보드
- 근거: @docs/01_analyze/prd/compliance.md (REQ-COMP-006), @docs/02_plan/screen/compliance.md (SCR-COMP-004), @docs/02_plan/api_spec/compliance.md (API-COMP-010)
- 절차:
  1. SCR-COMP-004 이동
  2. KPI(준수율·미해결 시정조치 건수)와 요구사항별 상태 표 확인
- 기대 결과: 집계값이 실제 데이터와 일치. (데이터 없는 기간 필터 적용 시 0/빈 결과 표시도 확인)

### TC-COMP-014 · RBAC — 타 역할 접근 제어
- 근거: @docs/02_plan/security/authorization/compliance_officer.md (4절 접근 제어 규칙)
- 전제: cm@itsm.local(CHANGE_MANAGER) 로그인
- 절차:
  1. 사이드바에 컴플라이언스 메뉴 노출 여부 확인
  2. `/compliance` 등 컴플라이언스 화면 직접 접근 시도
  3. 컴플라이언스 API(예: `GET /api/v1/compliance/requirements`) 직접 호출
- 기대 결과: 사이드바 미노출, 화면 접근 시 403 처리, API 호출 403.
