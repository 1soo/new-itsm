# 통합 테스트 시나리오 — srm (서비스 카탈로그 커스텀 폼 빌더, form.io 유지보수 2026-07-17)

## 사전 조건
- 빌드 테스트 통과(백엔드 Gradle build, 프론트엔드 tsc+vite build)
- 계정: po@itsm.local(PROCESS_OWNER), user@itsm.local(END_USER) — 공통 비밀번호 `Admin@1234`
- 서버: 백엔드 `localhost:8080`, 프론트엔드 `localhost:5173` (기동 확인됨)
- 참고: `docs/02_plan/database/service-request.md` v0.5, `docs/02_plan/api_spec/service-request.md` v0.5 API-SRM-002/003/004/006, `docs/02_plan/api_spec/common.md` 0-2절, `docs/02_plan/screen/service-request.md` SCR-SRM-002/007, `docs/02_plan/screen/common.md` 8절, `docs/03_develop/plan/service-request.md` "2026-07-17 유지보수" 절

## 시나리오

### TC-SRM-001 · 빌드 테스트
- 근거: 통합테스트 선행 항목
- 절차: 1. 백엔드 `./gradlew build` 2. 프론트엔드 `npm run build`
- 기대 결과: 둘 다 오류 없이 성공

### TC-SRM-002 · 카탈로그 항목 폼 빌더로 자유배치 폼 설계·저장
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-007, @docs/03_develop/plan/service-request.md "2026-07-17 유지보수" FE절
- 전제: po@itsm.local 로그인, `/admin/service-catalog` "카탈로그 항목" 탭
- 절차:
  1. 신규 카탈로그 항목 생성(이름 입력) 또는 기존 항목 편집 진입
  2. 폼 빌더(`DynamicFormBuilder`) 팔레트에서 컬럼(Layout) 컴포넌트를 캔버스에 드래그앤드롭
  3. 컬럼 내부에 텍스트 필드(Basic) 1개, 패널(Layout) 1개를 배치하고 패널 안에 숫자 필드(Basic) 1개 배치
  4. 각 필드에 key/label 지정 후 "폼 저장"(또는 항목 "저장") 클릭
  5. 항목을 재조회(상세 진입)
- 기대 결과: 저장 성공, 재조회 시 컬럼/패널 중첩 레이아웃과 필드 구성이 그대로 유지됨(`formSchema` JSON에 components 트리 보존)

### TC-SRM-003 · 요청 제출 화면에 저장된 폼이 레이아웃 그대로 렌더링
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-002, @docs/02_plan/screen/common.md 8절
- 전제: TC-SRM-002에서 저장한 카탈로그 항목
- 절차: user@itsm.local로 `/portal`에서 해당 항목 선택 → `/portal/requests/new` 진입
- 기대 결과: `DynamicFormRenderer`가 컬럼/패널 레이아웃과 필드(텍스트/숫자)를 그대로 렌더링. 별도 가공 없이 관리자가 설계한 배치와 동일하게 표시됨

### TC-SRM-004 · 요청 제출 성공 시 formValues(submission.data) 그대로 저장
- 근거: @docs/02_plan/api_spec/service-request.md API-SRM-006, @docs/02_plan/database/service-request.md `service_request.form_values`
- 전제: TC-SRM-003 화면
- 절차: 모든 필드에 유효한 값 입력 후 제출 → 접수 후 요청 상세 조회
- 기대 결과: 제출 성공(접수번호 발급), 요청 상세에 입력한 값이 그대로 표시됨(`form_values` JSONB에 `submission.data` 그대로 저장)

### TC-SRM-005 · 서버 재검증 — required 위반 시 400
- 근거: @docs/02_plan/api_spec/common.md 0-2절, `FormSubmissionValidator`
- 전제: 클라이언트 검증 우회 목적으로 `POST /api/v1/service-requests`를 직접 호출(필수 필드 값 누락)
- 절차: TC-SRM-002 카탈로그 항목의 필수(required) 필드 값을 비운 채 formValues 페이로드로 직접 API 호출
- 기대 결과: 400 응답(필수 항목 누락)

### TC-SRM-006 · 서버 재검증 — minLength/maxLength/pattern/min/max 위반 시 400
- 근거: @docs/02_plan/api_spec/common.md 0-2절, `FormSubmissionValidator`, `docs/source/form_io/component-schema-and-validation.md` 3절
- 전제: minLength/maxLength/pattern/min/max 중 하나 이상이 설정된 필드가 있는 카탈로그 항목(필요 시 TC-SRM-002 항목에 규칙 추가)
- 절차: 각 규칙 위반 값(예: minLength 미만 문자열, max 초과 숫자)으로 `POST /api/v1/service-requests` 직접 호출
- 기대 결과: 위반 항목마다 400 응답(정상 값으로는 201 성공, 대조군으로 1회 확인)

### TC-SRM-007 · 기존 SRM 회귀 — 카테고리 CRUD
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-009, @docs/06_maintenance/20260716-125723/srm/history.md
- 전제: po@itsm.local, `/admin/service-catalog` "카테고리 관리" 탭
- 절차: 새 카테고리 생성 → 목록 노출 확인 → 이름 수정 → 사용 중인 카테고리 삭제 시도(409 확인) → 미사용 카테고리 삭제(성공)
- 기대 결과: 생성/수정/조회 정상, 사용 중 카테고리 삭제는 409(CATEGORY_IN_USE), 미사용 카테고리 삭제 성공

### TC-SRM-008 · 기존 SRM 회귀 — 담당자 배정
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-004
- 전제: 담당자 역할이 지정된 카탈로그 항목의 요청 1건, 상담원 계정
- 절차: `/service-requests/queue`에서 해당 요청 "배정" 버튼 클릭 → 역할 후보 목록에서 담당자 선택
- 기대 결과: 배정 성공, 목록/상세에 담당자 반영

### TC-SRM-009 · 기존 SRM 회귀 — 상태 전이(라벨·타임라인 actor)
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-005
- 전제: TC-SRM-004에서 생성한 요청(또는 임의 요청), 상담원 계정
- 절차: 요청 상세에서 검증 완료 → 라우팅 처리(담당자 배정 후) → 이행 시작 → 이행 완료 처리 → 종료 처리까지 순차 전이
- 기대 결과: 각 버튼 라벨이 동작 동사형(검증 완료/라우팅 처리/이행 시작/이행 완료 처리/종료 처리)으로 표시, 타임라인에 상태 라벨과 행위 주체자(actor)가 함께 기록됨

### TC-SRM-010 · 기존 SRM 회귀 — 승인 게이트
- 근거: @docs/02_plan/api_spec/service-request.md, `docs/04_test/20260711-195405/approval-engine/result/approval-engine.md`(SRM 승인 게이트 기존 검증 이력)
- 전제: SERVICE_REQUEST 도메인·해당 카탈로그 항목으로 매칭되는 승인 프로세스가 구성된 상태
- 절차: 요청을 이행 단계로 전이 시도(ROUTED→IN_FULFILLMENT) → 승인 대기 상태 확인 → 승인권자 승인 처리 → 이행 전이 재시도
- 기대 결과: 승인 대기 중에는 이행 전이 버튼 숨김/차단, 승인 완료 후 이행 전이 가능
