# 통합 테스트 시나리오 — esm (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- backend(:8080)/frontend(:5173) dev 서버 기동
- 테스트 계정(공통 비밀번호 `Admin@1234`): `user@itsm.local`(END_USER, 포털·제출·내 요청), `it-coord@itsm.local`(DEPT_COORDINATOR/IT, 처리 큐·내 하위 작업), `po@itsm.local`(PROCESS_OWNER, 카탈로그 관리·지표), `hr@itsm.local`(HR_CASE_MANAGER, HR 케이스)
- 본 phase는 **레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환**(BE/DB 변경 없음). esm은 통합검색(SCR-COM-011) 대상 도메인이 아니므로 통합검색 TC는 시나리오에서 제외
- `status.ts`(departmentLabel/requestStatusLabel/hrCaseStatusLabel/checklistStatusLabel/checklistTaskStatusLabel/checklistTypeLabel/checklistTemplateTypeLabel)에 falsy 가드 적용 확인, `format.ts`는 라벨 없는 순수 `ko-KR` 포맷터로 변경 없음(사전 확인 완료)
- dev-lead 사전 공지: 체크리스트 유형(온보딩/오프보딩)이 `ChecklistDetailPage.tsx`/`MyChecklistTasksPage.tsx` 두 화면에서 하드코딩 삼항 연산자로 중복 구현돼 있던 것을 `checklistTypeLabel(t, type)` 공용 헬퍼로 통합, `EsmCatalogManagePage.tsx`의 템플릿 유형(3종, NONE 포함)도 별도 헬퍼 신설, `DeptRequestSubmitPage.tsx`의 `validateForm`에 `t` 인자 추가 — 소스 리뷰로 3건 모두 정상 반영 확인
- playwright는 매 TC마다 새 창(새 context)에서 수행, storage 초기화
- 11개 화면 규모 감안, 화면별 TC를 세분화하되 기능 회귀는 TC-ESM-I18N 항목에 통합 포함

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/screen/common.md (6절 i18n 아키텍처), @docs/03_develop/plan/esm.md ("i18n 다국어 전환" 절)
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-ESM-I18N-001 · 부서 서비스 포털(SCR-ESM-001) 텍스트 전환
- 근거: @docs/02_plan/screen/esm.md (SCR-ESM-001)
- 전제: user@itsm.local 로그인, English 전환
- 절차: 1) 부서 포털 진입 2) 부서 탭(HR/Legal/Facilities/Finance)·검색바·카탈로그 카드 확인
- 기대 결과: 부서 탭 라벨 4종, 검색바 placeholder, 카탈로그 카드 텍스트 영어 전환

### TC-ESM-I18N-002 · 부서 요청 제출(SCR-ESM-002) 텍스트 전환 및 회귀 — 필수 항목 오류
- 근거: @docs/02_plan/screen/esm.md (SCR-ESM-002)
- 전제: user@itsm.local 로그인, English 전환
- 절차: 1) 카탈로그 카드 클릭해 제출 화면 진입 2) 필수 필드 미입력 상태로 제출 시도 3) 값 입력 후 제출
- 기대 결과: 동적 폼 라벨·제출 버튼 영어 전환. 필수 미입력 시 "{label} is required." 류 오류 영어 전환(회귀 없음). 제출 성공 토스트 영어 전환(온보딩/오프보딩 유형이면 체크리스트 자동 생성 안내 토스트 포함)

### TC-ESM-I18N-003 · 내 부서 요청 목록(SCR-ESM-003) 텍스트 전환
- 근거: @docs/02_plan/screen/esm.md (SCR-ESM-003)
- 전제: user@itsm.local 로그인, English 전환
- 절차: 1) 내 요청 목록 진입 2) 필터(부서/상태/기간)·표 헤더·부서 배지·상태 배지 확인
- 기대 결과: 필터·표 헤더(접수번호/부서/유형/상태/갱신일) 영어 전환, 부서 배지 4종, 상태 배지 4종(Submitted/In Progress/Completed/Rejected) 전환 확인

### TC-ESM-I18N-004 · 부서 요청 처리 큐(SCR-ESM-004) 텍스트 전환 및 회귀
- 근거: @docs/02_plan/screen/esm.md (SCR-ESM-004)
- 전제: it-coord@itsm.local 로그인, English 전환
- 절차: 1) 처리 큐 진입 2) 표 헤더·상태 배지 확인
- 기대 결과: 표 헤더·상태 배지 영어 전환. 담당 부서 외 요청 조회 불가(403) 회귀 없음

### TC-ESM-I18N-005 · 부서 요청 상세(SCR-ESM-005) 텍스트 전환 및 회귀 — 상태 전이·코멘트·승인 패널
- 근거: @docs/02_plan/screen/esm.md (SCR-ESM-005)
- 전제: it-coord@itsm.local 로그인, English 전환, TC-ESM-I18N-002에서 제출한 요청으로 진입
- 절차: 1) 상태 전이(제출→처리중) 2) 코멘트 입력·등록 3) 승인 패널(공용) 확인(매칭 프로세스 없으면 패널 미노출, 있으면 차수 진행 상태) 4) 온보딩/오프보딩 요청이면 연계 체크리스트 카드 확인
- 기대 결과: 상태 전이 버튼·코멘트 입력 라벨·연계 체크리스트 카드 텍스트 영어 전환. 상태 전이·코멘트 등록 토스트 영어. 담당 부서 처리자가 아니면 상태 전이 버튼 비노출 회귀 없음

### TC-ESM-I18N-006 · 부서별 카탈로그 관리(SCR-ESM-006) 텍스트 전환 및 회귀 — 템플릿 유형(NONE 포함)
- 근거: @docs/02_plan/screen/esm.md (SCR-ESM-006)
- 전제: po@itsm.local 로그인, English 전환
- 절차: 1) 카탈로그 관리 화면 진입 2) 항목 편집 폼에서 담당 부서 셀렉트·양식 필드 빌더 확인 3) 체크리스트 템플릿 유형 셀렉트(NONE/Onboarding/Offboarding) 확인, Onboarding 선택 시 템플릿 빌더 노출 확인
- 기대 결과: 담당 부서 셀렉트 4종, 필드 빌더 라벨, 체크리스트 템플릿 유형 3종(None/Onboarding/Offboarding) 전부 영어 전환. 담당 부서 미지정 저장 시 400 오류 메시지 영어 전환(회귀 없음)

### TC-ESM-I18N-007 · HR 케이스 목록(SCR-ESM-007) 텍스트 전환 및 회귀 — 케이스 접수
- 근거: @docs/02_plan/screen/esm.md (SCR-ESM-007)
- 전제: hr@itsm.local 로그인, English 전환
- 절차: 1) HR 케이스 목록 진입 2) 상태 필터·표 헤더·상태 배지(Intake/Documentation/Investigation/Resolution) 확인 3) "케이스 접수" 버튼으로 신규 케이스 생성
- 기대 결과: 필터·표 헤더·상태 배지 전부 영어 전환. 케이스 접수 폼 라벨 영어 전환, 생성 성공 토스트 영어

### TC-ESM-I18N-008 · HR 케이스 상세(SCR-ESM-008) 텍스트 전환 및 회귀 — 4단계 순차 전이
- 근거: @docs/02_plan/screen/esm.md (SCR-ESM-008)
- 전제: hr@itsm.local 로그인, English 전환, TC-ESM-I18N-007에서 생성한 케이스로 진입
- 절차: 1) 상태 전이(접수→기록→조사→해결) 순차 클릭 2) 상태 이력 타임라인 확인
- 기대 결과: 상태 전이 버튼·상태 이력 타임라인 라벨 전부 영어 전환. 정의된 순서 외 전이 시도 시(해당 시나리오상 발생하지 않으면 생략) 400 회귀 없음. 매 전이마다 토스트 영어

### TC-ESM-I18N-009 · 온보딩/오프보딩 체크리스트 상세(SCR-ESM-009) 텍스트 전환 — 체크리스트 유형 라벨
- 근거: @docs/02_plan/screen/esm.md (SCR-ESM-009), dev-lead 지시(체크리스트 유형 라벨 일관성 확인)
- 전제: TC-ESM-I18N-002에서 온보딩/오프보딩 유형 요청 제출로 생성된 체크리스트 확인 필요(없으면 이 TC에서 신규 제출)
- 절차: 1) 체크리스트 상세 진입 2) 체크리스트 유형(Onboarding/Offboarding)·대상자·전체 진행률 배지 확인 3) 하위 작업 표(담당 부서·설명·상태) 확인 4) 오프보딩이면 회수 자산 링크 확인
- 기대 결과: 체크리스트 유형 라벨이 English로 정상 전환(원시값 노출 없음), 전체 진행률 배지(In Progress/Completed), 하위 작업 상태(Pending/Done) 전부 영어 전환

### TC-ESM-I18N-010 · 내 하위 작업 목록(SCR-ESM-010) 텍스트 전환 및 회귀 — 체크리스트 유형 라벨 일관성
- 근거: @docs/02_plan/screen/esm.md (SCR-ESM-010), dev-lead 지시(두 화면 간 체크리스트 유형 라벨 일관성 확인)
- 전제: it-coord@itsm.local 로그인(TC-ESM-I18N-009 체크리스트에 IT 담당 하위 작업이 있는 경우) 또는 해당 부서 계정, English 전환
- 절차: 1) 내 하위 작업 목록 진입 2) 필터(상태)·표(체크리스트 유형/대상자/작업 설명/상태) 확인, TC-ESM-I18N-009와 동일 체크리스트 유형 라벨 비교 3) "완료 처리" 버튼으로 하위 작업 완료
- 기대 결과: 체크리스트 유형 라벨이 SCR-ESM-009와 동일하게 표시(일관성 확인). 완료 처리 시 토스트 영어, 모든 하위 작업 완료 시 체크리스트 전체 상태 자동 완료 전환(TC-ESM-I18N-009에서 재확인, 회귀 없음)

### TC-ESM-I18N-011 · ESM 지표 대시보드(SCR-ESM-011) 텍스트 전환
- 근거: @docs/02_plan/screen/esm.md (SCR-ESM-011)
- 전제: po@itsm.local 로그인, English 전환
- 절차: 1) 지표 대시보드 진입 2) 기간·부서 필터·KPI 카드(부서별 처리량/평균 처리시간/완료율) 확인
- 기대 결과: 필터·KPI 카드 라벨 전부 영어 전환

### TC-ESM-FORMAT-REG-001 · 날짜 포맷 회귀(ko-KR 유지)
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015 "확정된 결정 2")
- 전제: English 상태
- 절차: 1) 내 요청 목록/HR 케이스 상세의 갱신일·이력 시각 표시 확인
- 기대 결과: 날짜/시각 포맷이 ko-KR 로케일 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음

### TC-ESM-KO-ROUNDTRIP-001 · 한국어 재전환 라운드트립 확인
- 근거: @docs/02_plan/screen/common.md (SCR-COM-015)
- 전제: English 상태
- 절차: 1) 지구본 아이콘으로 한국어 재전환 2) 부서 포털/내 요청 목록/체크리스트 상세 중 1~2개 재확인
- 기대 결과: 새로고침 없이 즉시 한국어로 복귀, 기존 문구와 동일(누락·깨짐 없음)
