# 통합 테스트 시나리오 — common (배치3: 승인 대기함 상세보기 버튼, 유지보수 요청 2026-07-16)

## 사전 조건
- 빌드 테스트 통과(service-request 시나리오에서 1회 수행한 결과 공유 — 모노레포 공용 빌드)
- 계정: user@itsm.local(END_USER), agent@itsm.local(SERVICE_DESK_AGENT), admin@itsm.local(승인자로 동작 확인됨) — 공통 비밀번호 `Admin@1234`

## 시나리오

### TC-COM-001 · 빌드 테스트(공유)
- 근거: 통합테스트 선행 항목
- 절차: service-request 시나리오 TC-SRM-001 결과 참조(동일 빌드)
- 기대 결과: 오류 없이 성공

### TC-COM-002 · 승인 대기함 "상세보기" 버튼 노출 및 실제 상세 화면 이동
- 근거: @docs/02_plan/screen/common.md SCR-COM-014 "상세보기 버튼" 요소, API-COM-003/004(신규 API 없이 기존 `ticketType`/`ticketId` 필드 사용)
- 전제: SRM 요청을 승인 게이트가 걸리는 지점(라우팅→이행 시작)까지 전이시켜 승인 대기 건 1개 생성
- 절차:
  1. user@itsm.local로 요청 제출 → agent@itsm.local로 검증·배정·라우팅 처리
  2. 이행 시작 전이 시도 → 승인 대기 상태로 전환됨 확인
  3. admin@itsm.local로 로그인, `/approvals` 진입
  4. 해당 행에서 기존 "상세"(승인/반려 처리 모달) 버튼과 별개로 "상세보기" 버튼이 노출되는지 확인
  5. "상세보기" 버튼 클릭
- 기대 결과: "상세보기" 버튼 클릭 시 승인/반려 모달이 열리지 않고, 새 탭이 아닌 현재 탭에서 해당 티켓의 실제 상세 화면(`/service-requests/{id}`)으로 이동함
