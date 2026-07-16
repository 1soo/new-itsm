# 통합 테스트 시나리오 — service-request (배치1: 전이버튼 라벨·타임라인 actor, 유지보수 요청 2026-07-16)

## 사전 조건
- 빌드 테스트 통과(백엔드 Gradle build, 프론트엔드 tsc+vite build) — 이번 실행에서 1회 수행하고 각 도메인 시나리오에서 동일 결과를 공유한다(모노레포 공용 빌드).
- 계정: user@itsm.local(END_USER), agent@itsm.local(SERVICE_DESK_AGENT) — 공통 비밀번호 `Admin@1234`
- 서비스 카탈로그에 최소 1개 이상의 요청 유형 존재(신규 요청 제출용)

## 시나리오

### TC-SRM-001 · 빌드 테스트
- 근거: 통합테스트 선행 항목
- 절차: 1. 백엔드 `./gradlew build` 2. 프론트엔드 `npm run build`
- 기대 결과: 둘 다 오류 없이 성공

### TC-SRM-002 · 상태 전이 버튼 라벨(동작 동사형) 표시 — 전체 흐름
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-005 "전이 버튼 라벨" 표, @docs/03_develop/plan/service-request.md "전이 버튼 라벨" 절
- 전제: user@itsm.local로 신규 요청 제출, agent@itsm.local 로그인
- 절차:
  1. 요청 상세에서 각 단계 전이 직전에 버튼 텍스트 확인 후 클릭: SUBMITTED→VALIDATED("검증 완료"), VALIDATED→ROUTED("라우팅 처리", 담당자 배정 후), ROUTED→IN_FULFILLMENT("이행 시작"), IN_FULFILLMENT→FULFILLED("이행 완료 처리")
  2. user@itsm.local로 재로그인 후 FULFILLED→CLOSED("종료 처리") 버튼 확인 후 클릭
- 기대 결과: 각 단계 버튼 텍스트가 도착 상태명이 아닌 표의 동작 동사형 라벨과 정확히 일치

### TC-SRM-003 · 전이 완료 토스트 문구 회귀 확인(도착 상태명 유지)
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-005 "전이 버튼 라벨" 절(토스트는 `statusLabel` 유지)
- 전제: TC-SRM-002에서 최소 1회 이상 전이 수행
- 절차: 전이 완료 후 노출되는 토스트 문구 확인
- 기대 결과: 토스트 문구는 버튼 라벨(동작 동사형)이 아닌 기존 도착 상태명 표기를 그대로 사용(예: "검증됨" 등, 버튼 라벨 "검증 완료"와 달라야 정상)

### TC-SRM-004 · 타임라인 actor·라벨 표시
- 근거: @docs/02_plan/screen/service-request.md SCR-SRM-005 "타임라인 actor 표시" 절, @docs/02_plan/api_spec/service-request.md API-SRM-008(`timeline[].actor`)
- 전제: TC-SRM-002에서 발생한 상태 변경 타임라인 존재
- 절차: 요청 상세 타임라인 영역에서 각 상태 변경 항목 확인
- 기대 결과: 각 항목에 행위 수행자 이름(또는 조회 실패 시 email 폴백)이 표시되고, 메시지에 상태 코드(예: `VALIDATED`)가 아닌 한글 라벨(예: "검증됨")이 포함됨
