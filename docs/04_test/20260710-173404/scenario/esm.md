# 통합 테스트 시나리오 — ESM (엔터프라이즈 서비스 관리)

## 사전 조건
- 빌드 테스트 통과 (frontend `npm run build`, backend `gradlew build`)
- frontend http://localhost:5173, backend http://localhost:8080, docker `itsm-postgres` 기동 중
- POC 계정(비밀번호 공통 `Admin@1234`): po@itsm.local(PROCESS_OWNER), user@itsm.local(END_USER), hr@itsm.local(HR_CASE_MANAGER, department=HR), legal-coord@itsm.local(DEPT_COORDINATOR, department=LEGAL), facilities-coord@itsm.local(DEPT_COORDINATOR, department=FACILITIES), it-coord@itsm.local(DEPT_COORDINATOR, department=IT), admin@itsm.local(SYSTEM_ADMIN)
- 시드 데이터(2026-07-10 기준):
  - 카탈로그: id=1 "신규 입사자 온보딩"(HR, ONBOARDING), id=2 "퇴사자 오프보딩 처리"(HR, OFFBOARDING), id=3 "계약서 검토 요청"(LEGAL), id=4 "좌석 배정 요청"(FACILITIES), id=5 "법인카드 발급 요청"(FINANCE)
  - 온보딩 템플릿(id=1) 하위작업: HR "인사 서류 접수 확인", IT "계정·장비 지급"
  - 오프보딩 템플릿(id=2) 하위작업: IT "계정 비활성화", FACILITIES "출입카드 회수"
  - 자산: am@itsm.local 소유 AST-0002(OPERATION/운영, 활성), AST-0004(PLANNING/계획, 활성), AST-0003(RETIREMENT/폐기, 비활성)
  - **알려진 데이터 제약**: DEPT_COORDINATOR 시드 계정 중 department=HR인 계정이 없음(hr@itsm.local은 HR_CASE_MANAGER 역할이며 DEPT_COORDINATOR가 아님). 따라서 온보딩 체크리스트의 HR 담당 하위작업("인사 서류 접수 확인")은 이번 테스트에서 완료 처리할 수 없어 온보딩 체크리스트의 "전체 완료→COMPLETED 자동전환"은 검증 대상에서 제외하고, IT 하위작업만 완료 후 "일부만 완료 시 IN_PROGRESS 유지"를 검증한다. 오프보딩은 IT+FACILITIES 계정으로 전체 완료 검증이 가능하다.

## 시나리오

### TC-ESM-BUILD-001 · 프론트엔드/백엔드 빌드
- 근거: `docs/03_develop/plan/esm.md` 5절 완료 기준
- 절차: `source/frontend`에서 `npm run build`, `source/backend`에서 `gradlew.bat build` 실행
- 기대 결과: 타입/컴파일 오류 없이 빌드 성공

## 1. 카탈로그 관리 (PROCESS_OWNER)

### TC-ESM-001 · 부서별 카탈로그 목록 조회
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-001), @docs/02_plan/screen/esm.md (SCR-ESM-006)
- 전제: po@itsm.local 로그인
- 절차: `/admin/esm-catalog`(또는 목록 API) 진입, department 필터로 HR/LEGAL/FACILITIES/FINANCE 조회
- 기대 결과: 부서별 시드 카탈로그 항목이 조회됨(HR 2건, LEGAL/FACILITIES/FINANCE 각 1건)

### TC-ESM-002 · 카탈로그 항목 생성 — 정상 및 department 누락 400
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-003)
- 전제: po@itsm.local 로그인
- 절차:
  1. 이름·담당 부서(FACILITIES)·양식 필드 1개 입력해 신규 항목 생성
  2. 담당 부서를 비운 채 생성 시도(API 직접 호출로 400 확인)
- 기대 결과: (1) 201 생성 성공 (2) department 누락 시 400

### TC-ESM-003 · 카탈로그 항목 생성 — 온보딩/오프보딩 템플릿 빈 채 저장 허용
- 근거: @docs/02_plan/api_spec/esm.md (0. 설계 배경), @docs/02_plan/screen/esm.md (SCR-ESM-006 — "템플릿 비어 있으면 저장은 허용하되 경고")
- 전제: po@itsm.local 로그인
- 절차: checklistTemplateType=ONBOARDING, checklistTemplate 빈 배열로 카탈로그 항목 생성
- 기대 결과: 201 저장 성공(경고는 FE 표시 영역이므로 저장 자체는 성공해야 함)

### TC-ESM-004 · PROCESS_OWNER 외 역할 카탈로그 생성 403
- 근거: @docs/02_plan/security/authorization/process_owner.md
- 전제: user@itsm.local(END_USER) 로그인
- 절차: `POST /api/v1/esm/catalog-items` 직접 호출
- 기대 결과: 403

## 2. 부서 요청 제출 (END_USER)

### TC-ESM-005 · 일반 요청 제출(체크리스트 없음)
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-005)
- 전제: user@itsm.local 로그인
- 절차: `/esm/portal`에서 LEGAL "계약서 검토 요청" 선택 후 필수 필드 입력·제출
- 기대 결과: 201, `checklistId=null`, 상태=SUBMITTED

### TC-ESM-006 · 온보딩 요청 제출 — 대상자명 필수 + 체크리스트 자동 생성
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-005, 0. 설계 배경), @docs/02_plan/screen/esm.md (SCR-ESM-002)
- 전제: user@itsm.local 로그인
- 절차:
  1. "신규 입사자 온보딩" 제출 시 대상자명(targetUserName) 없이 제출 시도
  2. 대상자명("테스트 온보딩 대상자") 포함해 재제출
- 기대 결과: (1) 400 (2) 201, `checklistId` 값 존재, 체크리스트 하위작업 2건(HR "인사 서류 접수 확인", IT "계정·장비 지급") 생성 확인(체크리스트 상세 조회)

### TC-ESM-007 · 오프보딩 요청 제출 — 활성 자산마다 IT 자산회수 하위작업 자동 추가(폐기 자산 제외)
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-005, 0. 설계 배경 — REQ-ESM-006)
- 전제: user@itsm.local 로그인. 대상자명="am@itsm.local"(활성 자산 AST-0002/AST-0004 2건, 폐기 자산 AST-0003 1건 보유)
- 절차: "퇴사자 오프보딩 처리" 제출(대상자명=am@itsm.local, 최종 근무일 입력)
- 기대 결과: 201, 체크리스트 하위작업 = 템플릿 2건(IT "계정 비활성화", FACILITIES "출입카드 회수") + 자산회수 2건(AST-0002, AST-0004 대상, IT 부서 배정) = 총 4건. AST-0003(폐기) 관련 하위작업은 생성되지 않음

### TC-ESM-008 · 템플릿 없는 유형 제출 400
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-005 — 템플릿 미정의 시 400)
- 전제: po@itsm.local으로 TC-ESM-003에서 생성한 "템플릿 빈 온보딩" 카탈로그 항목 사용, user@itsm.local 로그인
- 절차: 해당 카탈로그 항목으로 요청 제출(대상자명 포함)
- 기대 결과: 400

## 3. 부서 요청 처리 (DEPT_COORDINATOR)

### TC-ESM-009 · 소속 부서 요청만 조회, 타 부서 접근 403
- 근거: @docs/02_plan/security/authorization/dept_coordinator.md, @docs/02_plan/api_spec/esm.md (API-ESM-006/007)
- 전제: legal-coord@itsm.local(LEGAL) 로그인
- 절차:
  1. `/esm/requests/queue`에서 LEGAL 요청(TC-ESM-005 결과) 조회
  2. TC-ESM-006에서 생성된 HR 온보딩 요청 상세를 URL로 직접 접근 시도
- 기대 결과: (1) LEGAL 요청 정상 조회 (2) HR 요청 접근 시 403

### TC-ESM-010 · 상태 전이(IN_PROGRESS/COMPLETED/REJECTED)·코멘트
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-008/009)
- 전제: legal-coord@itsm.local 로그인, TC-ESM-005 요청 상세
- 절차: 상태를 IN_PROGRESS로 전이 → 코멘트 등록 → COMPLETED로 전이
- 기대 결과: 각 전이 200 성공, 코멘트 201 등록, 최종 상태 COMPLETED로 조회됨

## 4. 체크리스트 하위작업 (DEPT_COORDINATOR, 여러 부서)

### TC-ESM-011 · 본인 담당 부서 하위작업만 조회/완료 처리
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-015/016), @docs/02_plan/security/authorization/dept_coordinator.md
- 전제: it-coord@itsm.local(IT) 로그인
- 절차: `/esm/checklist-tasks`에서 본인(IT) 배정 하위작업만 노출되는지 확인(TC-ESM-006 온보딩 IT 작업 + TC-ESM-007 오프보딩 IT 작업 3건 포함, FACILITIES/HR 작업 미노출)
- 기대 결과: IT 배정 작업만 목록에 노출

### TC-ESM-012 · 오프보딩 체크리스트 전체 완료 → COMPLETED 자동 전환
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-016 — "전체 하위 작업 완료 시 체크리스트 상태 COMPLETED 자동 갱신")
- 전제: it-coord@itsm.local(IT), facilities-coord@itsm.local(FACILITIES) 로그인 필요(순차)
- 절차:
  1. it-coord로 오프보딩 체크리스트의 IT 작업 3건(계정 비활성화, AST-0002 회수, AST-0004 회수) 모두 완료 처리 → 이 시점 체크리스트 상태 확인(FACILITIES 미완료이므로 IN_PROGRESS 예상)
  2. facilities-coord로 "출입카드 회수" 완료 처리
- 기대 결과: (1) IT만 완료 시 `checklistStatus=IN_PROGRESS` (2) FACILITIES까지 완료 시 마지막 응답 `checklistStatus=COMPLETED`, 체크리스트 상세 조회 시 상태=COMPLETED

### TC-ESM-013 · 온보딩 체크리스트 부분 완료 — IN_PROGRESS 유지(회귀, HR 담당자 부재로 전체완료는 검증 제외)
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-016)
- 전제: it-coord@itsm.local(IT) 로그인
- 절차: TC-ESM-006 온보딩 체크리스트의 IT 작업("계정·장비 지급") 완료 처리
- 기대 결과: `checklistStatus=IN_PROGRESS`(HR 작업 미완료로 전체완료 아님)

### TC-ESM-014 · 타 부서 하위작업 완료 시도 403
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-016 — "403 배정 부서 아님")
- 전제: legal-coord@itsm.local(LEGAL) 로그인
- 절차: IT 배정 하위작업(TC-ESM-007의 "계정 비활성화")을 완료 처리 시도
- 기대 결과: 403

## 5. HR 케이스 (HR_CASE_MANAGER)

### TC-ESM-015 · 케이스 접수 및 목록/상세 조회
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-010/011/012)
- 전제: hr@itsm.local 로그인
- 절차: "케이스 접수" 버튼으로 신규 케이스 생성(제목·대상자 입력) → 목록에서 확인 → 상세 진입
- 기대 결과: 201 생성(status=INTAKE), 목록·상세에 정상 노출

### TC-ESM-016 · 순차 전이만 허용(역행/건너뛰기 400)
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-013), @docs/02_plan/screen/esm.md (SCR-ESM-008)
- 전제: hr@itsm.local 로그인, TC-ESM-015 케이스(INTAKE)
- 절차:
  1. INTAKE→INVESTIGATION(건너뛰기) 시도
  2. INTAKE→DOCUMENTATION(정상) → DOCUMENTATION→INVESTIGATION(정상) → INVESTIGATION→RESOLUTION(정상)
  3. RESOLUTION→DOCUMENTATION(역행) 시도
- 기대 결과: (1) 400 (2) 각 단계 200 성공, 순서대로 진행 (3) 400

### TC-ESM-017 · HR_CASE_MANAGER 외 전 역할 403(SYSTEM_ADMIN 포함)
- 근거: @docs/02_plan/security/authorization/hr_case_manager.md ("SYSTEM_ADMIN 포함 그 외 역할 403")
- 전제: admin@itsm.local(SYSTEM_ADMIN), po@itsm.local(PROCESS_OWNER) 각각 로그인
- 절차: `GET /api/v1/esm/hr-cases` 호출 및 `/esm/hr-cases` 화면 진입 시도
- 기대 결과: 두 계정 모두 403(화면 접근 포함), 사이드바에 "HR 케이스" 메뉴 자체가 노출되지 않음

## 6. ESM 지표

### TC-ESM-018 · 지표 조회 — 데이터 없어도 0, 필터 동작
- 근거: @docs/02_plan/api_spec/esm.md (API-ESM-017)
- 전제: po@itsm.local 로그인
- 절차:
  1. 존재하지 않는 미래 기간(from/to)으로 지표 조회
  2. department=HR로 필터 조회(TC-ESM-006/007 데이터 반영 확인)
- 기대 결과: (1) 모든 값 0 정상 응답(오류 없음) (2) HR 부서 requestCount 등 값이 0이 아니게 반영

## 7. RBAC 회귀 — 사이드바 메뉴 노출

### TC-ESM-019 · 역할별 ESM 메뉴 그룹 노출
- 근거: `docs/03_develop/plan/esm.md` FE 절, @docs/02_plan/security/authorization/{end_user,dept_coordinator,process_owner,hr_case_manager}.md
- 전제: user@itsm.local, legal-coord@itsm.local, po@itsm.local, hr@itsm.local 각각 로그인
- 절차: 각 계정 사이드바에서 "부서 서비스"(END_USER/DEPT_COORDINATOR/PROCESS_OWNER 항목 조합)와 "HR 케이스"(HR_CASE_MANAGER 전용) 그룹 노출 여부 확인
- 기대 결과: END_USER는 포털·내 요청만, DEPT_COORDINATOR는 처리 큐·내 하위작업만, PROCESS_OWNER는 카탈로그 관리·지표만, HR_CASE_MANAGER는 별도 "HR 케이스" 그룹만 노출(부서 요청 그룹 미노출)
