# 통합 테스트 시나리오 — incident (배치1: 전이버튼 라벨·타임라인 actor, 유지보수 요청 2026-07-16)

## 사전 조건
- 빌드 테스트 통과(service-request 시나리오에서 1회 수행한 결과 공유 — 모노레포 공용 빌드)
- 계정: agent@itsm.local(SERVICE_DESK_AGENT), im@itsm.local(INCIDENT_MANAGER) — 공통 비밀번호 `Admin@1234`

## 시나리오

### TC-INC-001 · 빌드 테스트(공유)
- 근거: 통합테스트 선행 항목
- 절차: service-request 시나리오 TC-SRM-001 결과 참조(동일 빌드)
- 기대 결과: 오류 없이 성공

### TC-INC-002 · 상태 전이 버튼 라벨(동작 동사형) 표시
- 근거: @docs/02_plan/screen/incident.md SCR-INC-003 "전이 버튼 라벨" 표
- 전제: agent@itsm.local로 신규 인시던트 등록
- 절차:
  1. 상세에서 NEW→IN_PROGRESS 버튼 텍스트 확인("대응 시작") 후 클릭
  2. IN_PROGRESS→RESOLVED 버튼 텍스트 확인("해결 처리") 후 해결 처리(시간 정보 입력 후 전이)
  3. RESOLVED→CLOSED 버튼 텍스트 확인("종료 처리") 후 클릭
- 기대 결과: 각 단계 버튼 텍스트가 도착 상태명이 아닌 표의 동작 동사형 라벨과 정확히 일치

### TC-INC-003 · 전이 완료 토스트 문구 회귀 확인(도착 상태명 유지)
- 근거: @docs/02_plan/screen/incident.md SCR-INC-003 "전이 버튼 라벨" 절
- 전제: TC-INC-002에서 최소 1회 이상 전이 수행
- 절차: 전이 완료 토스트 문구 확인
- 기대 결과: 토스트는 버튼 라벨이 아닌 기존 도착 상태명(`statusLabel`)을 그대로 사용

### TC-INC-004 · 타임라인 actor·라벨 표시 및 다른 이벤트 유형 회귀 확인
- 근거: @docs/02_plan/screen/incident.md SCR-INC-003 "타임라인 actor 표시" 절, @docs/02_plan/api_spec/incident.md(`timeline[].actor`)
- 전제: TC-INC-002의 상태 변경 타임라인 + 별도로 상태 업데이트(내부/외부 구분) 1건 등록
- 절차:
  1. 타임라인에서 상태 변경 항목의 actor·메시지 확인
  2. 상태 업데이트(다른 이벤트 유형) 항목의 메시지 확인
- 기대 결과: 상태 변경 항목은 actor(이름/email 폴백) 표시 + 메시지가 상태 코드가 아닌 한글 라벨. 상태 업데이트 등 다른 이벤트 유형 메시지는 기존과 동일(코드 노출 없음, 회귀 없음)
