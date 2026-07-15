# 통합 테스트 시나리오 — common (승인 프로세스 범위 우선순위 3축 재설계)

> 대상: 공용 승인 엔진(`approval_process`)의 도메인/요청유형/요청자역할 3축 독립 지정, 축 미지정=전체 매칭, 지정 축 개수 우선 + 동률 시 역할>요청유형>도메인 우선순위, 우선순위 충돌(409) 재정의
> 근거 문서: `docs/02_plan/database/common.md`(approval_process 4절), `docs/02_plan/api_spec/auth.md`(API-AUTH-025~028), `docs/01_analyze/feature/service-request.md`(FEAT-SRM-005, 승인 게이트 활용 도메인)
> 변경 파일: `source/backend/.../common/approval/domain/ApprovalProcess.java`, `.../common/approval/application/ApprovalGateService.java`, `.../auth/application/ApprovalProcessAdminService.java`, `source/db/sql/32_approval_process_priority_redesign.sql`

> **순서 의존**: `docs/04_test/20260715-112437/srm/scenario.md`(TC-SRM-PANEL-001/002, 승인 패널 미매칭 문구)를 **먼저** 수행한 뒤 이 시나리오를 진행한다(본 시나리오가 생성하는 tier=0 전체 미지정 규칙이 SERVICE_REQUEST 도메인 전체에 매칭되어, 이후에는 "미매칭" 상태를 재현할 수 없다).

## 사전 조건
- 빌드 테스트 통과 — auth 시나리오 TC-BUILD-001과 동일 산출물이므로 재수행하지 않음
- 테스트 계정(공통 비밀번호 `Admin@1234`): `admin@itsm.local`(SYSTEM_ADMIN), `user@itsm.local`(END_USER), `agent@itsm.local`(SERVICE_DESK_AGENT), `po@itsm.local`(PROCESS_OWNER), `cab@itsm.local`(APPROVER)
- 사전 데이터: 승인 프로세스 규칙 1건 존재(`노트북 신청 승인 규칙` — domain=SERVICE_REQUEST, requestSubtypeKey=노트북 신청 카탈로그(id=1), requesterRole=END_USER, 1차 승인자=PROCESS_OWNER, priorityTier=37, 3축 모두 지정), 서비스 카탈로그 "비밀번호 초기화"(id=2, 요청유형 스코프 지정 규칙 없음)
- 관리자 화면: `/admin/approval-processes`(SCR-ADMIN-007), `/admin/approval-processes/new`(SCR-ADMIN-008), 승인 대기함 `/approvals`(SCR-COM-014, 전 도메인 공용)

## 시나리오

### TC-PRI-001 · 전체 미지정(tier=0) 규칙 생성 및 매칭
- 근거: @docs/02_plan/database/common.md (approval_process 4절 — "축이 비어있으면 해당 축의 모든 값에 매칭"), @docs/02_plan/api_spec/auth.md (API-AUTH-027)
- 전제: `admin@itsm.local` 로그인 상태
- 절차:
  1. `/admin/approval-processes/new`에서 도메인="전체 도메인", 요청자 역할 미선택(0개), 규칙명="QA 우선순위: 전체 규칙", 1차 승인자 역할=`APPROVER`(승인자) 선택 후 생성
  2. 목록에서 우선순위 배지가 "전체 적용"으로 표시되는지 확인
  3. `user@itsm.local`로 로그인, 서비스 포털에서 "비밀번호 초기화"(카탈로그 id=2) 신규 요청 제출(요청 A)
  4. `agent@itsm.local`로 로그인, 요청 A를 VALIDATED → ROUTED → IN_FULFILLMENT로 전이 시도
  5. `cab@itsm.local`(APPROVER)로 로그인해 승인 대기함(`/approvals`)에서 요청 A 조회
- 기대 결과: 2번 배지 확인. 4번에서 ROUTED→IN_FULFILLMENT 전이 시 승인 대기 상태로 전환됨(1번 규칙에 매칭). 5번 `cab@itsm.local`의 대기함에 요청 A가 조회됨(요청자 역할 축이 비어 있어 END_USER가 제출한 요청도 매칭)

### TC-PRI-002 · 도메인만 지정(tier=11) 규칙 생성 — "전체 미지정" 대비 우선순위 역전
- 근거: @docs/02_plan/database/common.md (approval_process 4절 — priority_tier 산정식, 축 개수 우선), @docs/01_analyze 유지보수 요청 2번(전체규칙 vs 특정 도메인 규칙 우선순위 역전 케이스)
- 전제: TC-PRI-001의 tier=0 규칙이 존재하는 상태
- 절차:
  1. `admin@itsm.local`로 `/admin/approval-processes/new`에서 도메인="서비스 요청"(SERVICE_REQUEST, 구체 도메인 선택), 요청 유형="전체", 요청자 역할 미선택, 규칙명="QA 우선순위: 도메인 전용 규칙", 1차 승인자 역할=`SERVICE_DESK_AGENT`(서비스 데스크 상담원) 선택 후 생성
  2. 목록에서 우선순위 배지가 "도메인"으로 표시되는지 확인(tier=11 > tier=0)
  3. `user@itsm.local`로 "비밀번호 초기화" 신규 요청 제출(요청 B)
  4. `agent@itsm.local`로 요청 B를 VALIDATED → ROUTED → IN_FULFILLMENT로 전이 시도
  5. `agent@itsm.local`(SERVICE_DESK_AGENT)의 승인 대기함(`/approvals`)에서 요청 B 조회
  6. `cab@itsm.local`(APPROVER)의 승인 대기함에서도 조회(요청 B가 나타나지 않아야 함)
- 기대 결과: 요청 B는 도메인=SERVICE_REQUEST·요청유형 무관 축이 더 구체적인 tier=11 규칙(승인자=SERVICE_DESK_AGENT)에 매칭되고, tier=0 전체 규칙(승인자=APPROVER)은 더 이상 적용되지 않는다 — 5번 `agent@itsm.local` 대기함에는 조회되고, 6번 `cab@itsm.local` 대기함에는 조회되지 않아 "특정 도메인 규칙이 전체 미지정 규칙보다 우선 적용"됨을 확인

### TC-PRI-003 · 3축 모두 지정(tier=37, 기존 규칙) — 부분 지정 규칙 대비 우선순위 확인
- 근거: @docs/02_plan/database/common.md (approval_process 4절), @docs/01_analyze 유지보수 요청 2번(3축 모두 지정 vs 부분 지정 케이스)
- 전제: TC-PRI-001(tier=0)·TC-PRI-002(tier=11) 규칙과 사전 데이터의 `노트북 신청 승인 규칙`(tier=37, domain=SERVICE_REQUEST+requestSubtypeKey=노트북 신청+requesterRole=END_USER)이 모두 존재하는 상태
- 절차:
  1. `user@itsm.local`로 서비스 포털에서 "노트북 신청"(카탈로그 id=1) 요청 제출(희망 모델·신청 사유 입력, 요청 C)
  2. `agent@itsm.local`로 요청 C를 VALIDATED → ROUTED → IN_FULFILLMENT로 전이 시도
  3. `po@itsm.local`(PROCESS_OWNER)의 승인 대기함(`/approvals`)에서 요청 C 조회
  4. `agent@itsm.local`(SERVICE_DESK_AGENT)·`cab@itsm.local`(APPROVER) 각각의 승인 대기함에서도 조회(요청 C가 나타나지 않아야 함)
- 기대 결과: 요청 C는 도메인·요청유형·요청자역할 3축이 모두 일치하는 tier=37 규칙(승인자=PROCESS_OWNER)에 매칭되며, 동시에 매칭 가능한 tier=11(도메인 전용, 승인자=SERVICE_DESK_AGENT)·tier=0(전체, 승인자=APPROVER) 규칙보다 우선 적용된다 — 3번 `po@itsm.local` 대기함에만 조회되고 4번 나머지 두 계정 대기함에는 조회되지 않음

### TC-PRI-004 · 우선순위 충돌 409(전체 미지정 규칙 중복 생성 차단)
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-027, 409 우선순위 충돌 — tier=0 전체 미지정 규칙은 시스템에 1개만 허용)
- 전제: TC-PRI-001에서 생성한 tier=0 규칙이 여전히 존재하는 상태
- 절차:
  1. `admin@itsm.local`로 `/admin/approval-processes/new`에서 도메인="전체 도메인", 요청자 역할 미선택, 규칙명="QA 우선순위: 중복 전체 규칙", 1차 승인자 역할 아무 것이나 1개 선택 후 "생성 완료" 클릭
- 기대 결과: 저장이 거부되고 폼 상단에 인라인 오류(우선순위 충돌, 겹치는 규칙명 안내)가 표시되며, 목록에는 반영되지 않는다(409)

### TC-PRI-005 · 규칙 삭제와 진행 중 승인 인스턴스 무관 확인
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-029, "이미 진행 중인 승인 인스턴스는... 삭제의 영향을 받지 않는다")
- 전제: TC-PRI-001에서 생성한 tier=0 규칙, 그리고 그 규칙으로 생성된 요청 A의 승인 인스턴스(IN_PROGRESS)가 존재
- 절차:
  1. `admin@itsm.local`로 `/admin/approval-processes`에서 "QA 우선순위: 전체 규칙"(tier=0) 삭제
  2. `cab@itsm.local`로 승인 대기함(`/approvals`) 재조회
  3. 요청 A를 승인 처리
- 기대 결과: 1번 규칙 삭제 후에도 2번 요청 A는 대기함에서 사라지지 않고 그대로 조회된다(생성 시점 스냅샷으로 진행). 3번 승인 처리 성공, 요청 A 상태가 갱신됨

### 사후 정리
- `admin@itsm.local`로 TC-PRI-002에서 생성한 "QA 우선순위: 도메인 전용 규칙"(tier=11) 삭제
- `agent@itsm.local`로 승인 대기함에서 요청 B 승인 처리
- `po@itsm.local`로 승인 대기함에서 요청 C 승인 처리
- (auth 시나리오 정리) `admin@itsm.local`로 `qa.audit@itsm.local` 테스트 계정은 이미 비활성 상태로 남겨둠(추가 정리 불필요)
