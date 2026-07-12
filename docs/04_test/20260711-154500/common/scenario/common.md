# 통합 테스트 시나리오 — common (헤더 알림 5초 polling 전환, 유지보수)

> 대상: SCR-COM-002 헤더 알림을 세션당 1회 조회 → 5초 간격 polling으로 전환(FE 단독, `source/frontend/src/routes/AppLayout.tsx`). BE/DB/API 계약 변경 없음.
> 근거 문서: `docs/02_plan/screen/common.md`(SCR-COM-002 v0.11), `docs/02_plan/api_spec/common.md`(v0.2), `docs/03_develop/plan/common.md` "헤더 알림 5초 polling 전환" 절(완료 기준).

## 사전 조건

- 빌드 테스트 통과
- 로컬 환경: docker `itsm-postgres`, backend(:8080), frontend(:5173) 기동 상태
- 테스트 계정: `cab@itsm.local`(APPROVER, 서비스요청·변경 승인 알림 대상), `am@itsm.local`(ASSET_MANAGER, 자산 만료 알림 대상), `cm@itsm.local`(CHANGE_MANAGER, 승인 대상 생성용) — 공통 비밀번호 `Admin@1234`
- 사전 데이터 준비(API로 조성, backend 직접 호출): cab 기준 서비스요청 승인 대기 1건(SRM-2026-0007) + 변경 승인 대기 8건(9건째부터 8건 cap 폐지 확인용) = 총 9건. am 기준 자산 만료 임박 4건.

## 시나리오

### TC-BUILD-001 · 프론트엔드 빌드 테스트
- 근거: @docs/03_develop/plan/common.md "완료(테스트 통과) 기준"
- 전제: `source/frontend` 의존성 설치 완료
- 절차:
  1. `npm run build` 실행
- 기대 결과: TypeScript 컴파일·번들링 오류 없이 빌드 성공

### TC-POLL-001 · 5초 간격 polling 및 역할별 대상 범위
- 근거: @docs/02_plan/screen/common.md SCR-COM-002 "알림 5초 polling(신규...)" 문단, @docs/03_develop/plan/common.md 완료 기준 1번
- 전제: cab@itsm.local(APPROVER) 로그인 상태, 새 브라우저 컨텍스트
- 절차:
  1. cab@itsm.local 로그인
  2. 네트워크 요청 관찰 시작(`browser_network_requests`) 후 약 12~15초 대기
  3. `/api/v1/approvals?...type=service-request`, `/api/v1/approvals?...type=change`, `/api/v1/notifications/dismissals` 요청 타임스탬프 확인
- 기대 결과: 위 API가 약 5초 간격으로 반복 호출됨(2~3회 이상 관찰). am(ASSET_MANAGER) 로그인 시에는 자산 만료 API(`/api/v1/assets?expiringWithinDays=...`)가 동일 주기로 호출되고, 승인 대기 API는 호출되지 않음(역할별 대상 범위 유지)

### TC-POLL-002 · 백그라운드 tab 정지 및 포그라운드 복귀 재개
- 근거: @docs/02_plan/screen/common.md SCR-COM-002 polling 문단(Page Visibility API), @docs/03_develop/plan/common.md 완료 기준 2번
- 전제: cab@itsm.local 로그인 상태(TC-POLL-001과 별도 새 컨텍스트)
- 절차:
  1. 로그인 후 초기 polling 1회 완료 대기
  2. `document.hidden=true` + `visibilitychange` 이벤트 강제 발생(백그라운드 진입 모사)
  3. 약 10초 대기 후 네트워크 요청 발생 여부 확인(0건이어야 함)
  4. `document.hidden=false` + `visibilitychange` 이벤트 발생(포그라운드 복귀 모사)
  5. 복귀 직후 즉시 1회 재조회 발생 확인, 이후 5초 간격 polling 재개 확인
- 기대 결과: 백그라운드 동안 API 호출 0건, 포그라운드 복귀 시 즉시 1회 재조회 후 5초 간격 재개

### TC-POLL-003 · merge 정책 — 팝오버 열림 중 신규 알림 추가·기존 항목 유지
- 근거: @docs/02_plan/screen/common.md SCR-COM-002 "merge" 문단, @docs/03_develop/plan/common.md 완료 기준 3·4번
- 전제: cab@itsm.local 로그인, 알림 벨 팝오버 오픈 상태(9건 표시)
- 절차:
  1. 팝오버를 연 채로 현재 표시된 항목 스냅샷 기록
  2. 백엔드 API로 cab 대상 신규 변경 승인 대기 1건 생성(팝오버는 닫지 않음)
  3. 5~10초 대기 후 팝오버 내용 재확인(닫았다 열지 않음)
  4. 기존에 표시되던 항목이 그대로 있는 상태에서 신규 항목이 목록 뒤에 추가되었는지 확인
  5. 반대로 표시 중인 기존 항목 하나를 백엔드에서 승인 처리(원본 데이터에서 제거, 예: 다른 경로로 처리)한 뒤 다음 poll(5~10초) 후에도 해당 항목이 팝오버에서 사라지지 않고 유지되는지 확인
- 기대 결과: 팝오버가 열려 있는 동안에도 polling이 계속되어 신규 항목이 뒤에 추가되고, 원본에서 사라진 기존 표시 항목은 확인처리 전까지 제거되지 않음

### TC-POLL-004 · 확인처리 이력 재조회에 따른 필터링과 벨 뱃지 서버 기준 재계산
- 근거: @docs/02_plan/api_spec/common.md API-COM-002, @docs/02_plan/screen/common.md "벨 뱃지 카운트는... 매 polling 주기마다..." 문단, @docs/03_develop/plan/common.md 완료 기준 5·6번
- 전제: cab@itsm.local 로그인, 알림 팝오버에 다건 표시 중
- 절차:
  1. 팝오버에서 개별 항목 하나 X 버튼으로 확인처리
  2. 확인처리 직후 목록에서 즉시 제거·뱃지 -1 확인(낙관적 갱신)
  3. 다음 polling 주기(5~10초) 대기 후 해당 항목이 재등장하지 않는지 확인(`/api/v1/notifications/dismissals` GET 응답에 포함되어 필터링됨)
  4. 벨 뱃지 값이 merge된 표시 목록 길이와 무관하게 서버 기준 전체 대기 건수(확인처리 제외)로 매 polling마다 재계산되는지, TC-POLL-003에서 원본이 제거됐지만 화면엔 남아있는 항목이 있다면 뱃지 수치가 화면 표시 건수보다 작아질 수 있음을 확인
- 기대 결과: 확인처리된 알림은 재조회에서 계속 제외되고, 뱃지 카운트는 표시 목록 크기와 별개로 서버 기준으로 갱신됨

### TC-POLL-005 · 팝오버 표시 상한 폐지(9건 이상 스크롤 노출)
- 근거: @docs/02_plan/screen/common.md SCR-COM-002 "상위 8건 cap 폐지" 문단, @docs/03_develop/plan/common.md 완료 기준 5번
- 전제: cab@itsm.local 로그인(사전 조성된 9건: SR 1건 + CHG 8건)
- 절차:
  1. 알림 벨 클릭, 팝오버 오픈
  2. 표시된 항목 수 확인(9건 이상)
  3. 목록 영역 스크롤 동작 확인(`max-h-80 overflow-auto`)
- 기대 결과: 8건 초과분도 잘리지 않고 전체 노출되며 스크롤로 접근 가능

### TC-POLL-006 · API 연속 실패 시 backoff 없는 고정 재시도·상태 유지
- 근거: @docs/02_plan/screen/common.md SCR-COM-002 "연속 조회 실패 시에도..." 문단, @docs/03_develop/plan/common.md 완료 기준 7번
- 전제: cab@itsm.local 로그인, 초기 polling 성공 후 알림 목록 표시된 상태
- 절차:
  1. `browser_evaluate` 또는 라우트 차단으로 `/api/v1/approvals`, `/api/v1/notifications/dismissals` 요청을 강제 실패(예: `page.route`로 abort)시킴
  2. 약 15초(3회 주기) 대기하며 화면에 표시된 알림 목록·뱃지가 그대로 유지되는지 확인
  3. 오류 토스트가 표시되지 않는지 확인
  4. 요청 차단 해제 후 다음 5초 주기에 정상 복구되는지 확인
- 기대 결과: 연속 실패 동안 backoff 없이 5초 고정 간격으로 계속 재시도하고, 기존 표시 상태 유지, 에러 토스트 없음. 차단 해제 후 정상 반영
