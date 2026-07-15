# 통합 테스트 시나리오 — service-request (srm)

> 대상: 요청 상세(SCR-SRM-005) 승인 패널 미매칭 문구를 요청 상태(게이트 평가 전/후) 기준으로 분리(유지보수 요청)
> 근거 문서: `docs/02_plan/screen/service-request.md`(SCR-SRM-005), `docs/01_analyze/feature/service-request.md`(FEAT-SRM-005)
> 변경 파일: `source/frontend/src/features/service-request/RequestDetailPage.tsx`

> **순서 의존**: 이 시나리오는 `docs/04_test/20260715-112437/common/scenario.md`(승인 프로세스 3축 우선순위)보다 **먼저** 수행한다. common 시나리오가 SERVICE_REQUEST 도메인 전체에 매칭되는 승인 프로세스 규칙(tier=0 전체 미지정 규칙 등)을 새로 생성하면, 이후에는 어떤 서비스 요청도 "매칭 없음" 상태가 나오지 않아 TC-SRM-PANEL-002(미매칭 문구)를 검증할 수 없게 된다.

## 사전 조건
- 빌드 테스트 통과(frontend) — auth 시나리오 TC-BUILD-001과 동일 산출물이므로 재수행하지 않음(auth 시나리오 결과 참조)
- 테스트 계정(공통 비밀번호 `Admin@1234`): `user@itsm.local`(END_USER), `agent@itsm.local`(SERVICE_DESK_AGENT)
- 사전 데이터: 서비스 카탈로그 "비밀번호 초기화"(카탈로그 id=2) — 이 항목은 현재 어떤 승인 프로세스 규칙과도 매칭되지 않는 상태(도메인 전용/전체 미지정 규칙이 아직 없음)

## 시나리오

### TC-SRM-PANEL-001 · 게이트 평가 전(SUBMITTED) — 중립(미확정) 문구
- 근거: @docs/01_analyze/feature/service-request.md (FEAT-SRM-005), @docs/02_plan/screen/service-request.md (SCR-SRM-005, "승인 패널 미매칭 문구 분리")
- 전제: `user@itsm.local` 로그인 상태
- 절차:
  1. 서비스 포털에서 "비밀번호 초기화" 카탈로그 항목 선택, 대상 계정 ID에 임의 값 입력 후 제출
  2. 생성된 요청 상세 화면(상태=제출됨/SUBMITTED) 진입, 우측 승인 패널 문구 확인
- 기대 결과: 요청 상태가 SUBMITTED이며, 승인 패널에 "이행 단계로 전환 시 승인 절차 적용 여부가 결정됩니다"류의 중립 문구가 표시된다("이 요청에는 승인 절차가 없습니다"라는 확정적 문구가 아직 노출되지 않음)

### TC-SRM-PANEL-002 · 게이트 평가 후(IN_FULFILLMENT) 매칭 없음 — 기존 확정 문구
- 근거: 상동
- 전제: TC-SRM-PANEL-001에서 생성한 요청
- 절차:
  1. `agent@itsm.local` 로그인, 해당 요청 상세에서 상태 전이 버튼으로 VALIDATED → ROUTED → IN_FULFILLMENT까지 순서대로 전이(각 단계 매칭되는 승인 프로세스가 없으므로 대기 없이 즉시 전이됨)
  2. IN_FULFILLMENT 전이 완료 후 승인 패널 문구 확인
- 기대 결과: 상태가 IN_FULFILLMENT로 전환된 뒤에는 승인 패널 문구가 "이 요청에는 승인 절차가 없습니다"(게이트 평가 후 미매칭 확정 문구)로 표시된다(TC-SRM-PANEL-001의 중립 문구와 다른 문구로 전환됨)
