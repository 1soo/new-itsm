# 통합 테스트 시나리오 — service-request (SRM)

> 실행 타임스탬프: 20260709-122918 · 도메인: service-request

## 사전 조건
- 빌드 테스트 통과(BE gradle test, FE vite build)
- BE(:8080)·FE dev(:5173) 기동, PostgreSQL healthy
- baseline seed: 기본 큐(미분류 is_default) + IT 서비스 큐, 데모 카탈로그 2건(노트북 신청=승인필요/approver_role APPROVER, 비밀번호 초기화=승인불필요)
- 역할 테스트 계정(SYSTEM_ADMIN이 생성·역할부여): requester(END_USER), agent(SERVICE_DESK_AGENT), approver(APPROVER), owner(PROCESS_OWNER)
- 격리: playwright 매 항목 새 context. API는 계정당 1회 로그인 세션 유지(jti 단일). baseline은 상대 검증(생성 후 포함), 절대 개수 assert 금지.

## 시나리오

### A. 빌드
- TC-BUILD-001 · BE `gradlew test` 통과 — 근거: @docs/01_analyze/feature/service-request.md 전 FEAT
- TC-BUILD-002 · FE `npm run build` 통과 — 근거: 동 화면 FEAT

### B. 카탈로그 (FEAT-SRM-001 / API-SRM-001~004)
- TC-CAT-001 · 목록 조회(200, 인증) — @docs/.../api_spec API-SRM-001
- TC-CAT-002 · 상세(formSchema 포함) 200 / 없음 404 — API-SRM-002
- TC-CAT-003 · 생성 PROCESS_OWNER 201 — REQ-SRM-001 정상
- TC-CAT-004 · 생성 이름/양식 누락 400 — FEAT-SRM-001 Unwanted
- TC-CAT-005 · 생성 비-PROCESS_OWNER 403 — FEAT-SRM-001 Unwanted(권한)
- TC-CAT-006 · 수정 PROCESS_OWNER 200 / 비권한 403 — API-SRM-004

### C. 지식 추천 (FEAT-SRM-011 / API-SRM-005)
- TC-KN-001 · 추천 조회 200, KM 미구축이므로 빈 배열([]) 정상 — REQ-SRM-011, 주의사항

### D. 큐 (API-SRM-016)
- TC-QUEUE-001 · Agent 큐 목록 200 [{id,name,isDefault,openCount}] — API-SRM-016
- TC-QUEUE-002 · 비-Agent(END_USER) 큐 목록 403 — RBAC

### E. 요청 제출/조회 (FEAT-SRM-002/003/004 / API-SRM-006~008)
- TC-REQ-001 · 요청 생성 201, ticketKey=SRM-YYYY-#### , status=SUBMITTED — REQ-SRM-002 정상
- TC-REQ-002 · 필수 양식 필드 미입력 400 — FEAT-SRM-002 Unwanted
- TC-REQ-003 · 유효하지 않은 카탈로그 400 — create 검증
- TC-REQ-004 · 목록 scope=mine 본인 요청 포함 200 — API-SRM-007
- TC-REQ-005 · 목록 scope=all: Agent 200 / END_USER 403 — API-SRM-007 RBAC
- TC-REQ-006 · 목록 ?queue={id} 필터(Agent) 200 — API-SRM-016 연계
- TC-REQ-007 · 상세: 본인 200(allowedTransitions·sla·approval·comments·timeline·linkedArticles[]) — API-SRM-008
- TC-REQ-008 · 상세: 타인 요청 무권한 사용자 403 / 없음 404 — API-SRM-008 RBAC

### F. 상태 전이 머신 (FEAT-SRM-006/007 / API-SRM-010)
- TC-ST-001 · 승인불필요 정상 경로 SUBMITTED→VALIDATED→ROUTED→IN_FULFILLMENT→FULFILLED→CLOSED (Agent) — REQ-SRM-006/007
- TC-ST-002 · 허용되지 않은 전이(예: SUBMITTED→IN_FULFILLMENT) 400 — API-SRM-010
- TC-ST-003 · 권한 없는 전이(END_USER가 VALIDATED 시도) 403 — FEAT-SRM-006 Unwanted
- TC-ST-004 · 종료된 요청 재종료(재전이) 400 — FEAT-SRM-007 Unwanted
- TC-ST-005 · CLOSED 전이는 요청자도 가능(요청자 CLOSE) — allowedTransitions 규칙

### G. 승인 (FEAT-SRM-005 / API-SRM-011~012)
- TC-APR-001 · 승인필요 요청 ROUTED 전이 시 APPROVAL_PENDING 전환·Approval(approver_role) 생성 — 상태머신 부수효과
- TC-APR-002 · 승인대기 중 이행(IN_FULFILLMENT) 시도 409 — API-SRM-010 409
- TC-APR-003 · approver_role 미보유(END_USER/AGENT) 승인 시도 403 — API-SRM-011 403
- TC-APR-004 · 반려 사유 누락 400 — API-SRM-011 400
- TC-APR-005 · APPROVER 승인 200 → status ROUTED, approval APPROVED — REQ-SRM-005
- TC-APR-006 · 승인 후 Agent IN_FULFILLMENT→FULFILLED→CLOSED 정상 — 승인 후 이행
- TC-APR-007 · 반려 200 → status REJECTED(종료), 이후 전이 400 — FEAT-SRM-005 Unwanted
- TC-APR-008 · 이미 결정된 승인 재처리 409 — API-SRM-011 "이미 결정됨 409"
- TC-APR-009 · 승인 대기 목록(API-SRM-012): APPROVER 역할 공유함에 PENDING 포함 200 / 비-Approver 403

### H. 코멘트 (FEAT-SRM-009 / API-SRM-013)
- TC-CM-001 · 요청자 코멘트 등록 201, 상세 comments 반영 — REQ-SRM-009
- TC-CM-002 · 무권한 사용자 코멘트 403 — FEAT-SRM-009 Unwanted

### I. CSAT (FEAT-SRM-010 / API-SRM-014)
- TC-CSAT-001 · 종료 요청에 요청자 CSAT(1~5) 제출 200 — REQ-SRM-010
- TC-CSAT-002 · 미종료 요청 CSAT 400 — FEAT-SRM-010 Unwanted
- TC-CSAT-003 · 중복 CSAT 409 — API-SRM-014
- TC-CSAT-004 · 요청자 아닌 사용자 CSAT 403 — API-SRM-014

### J. 지표 (FEAT-SRM-012 / API-SRM-015)
- TC-MET-001 · PROCESS_OWNER 지표 200 {csatAvg,avgResponseMinutes,avgResolveMinutes,slaComplianceRate} — REQ-SRM-012
- TC-MET-002 · 비-PROCESS_OWNER(AGENT) 403 — API-SRM-015 RBAC

### K. 배정 (API-SRM-009)
- TC-ASG-001 · Agent 배정(본인) 200 — REQ-SRM-006
- TC-ASG-002 · 비-Agent(END_USER) 배정 403 — FEAT-SRM-006 Unwanted
- TC-ASG-003 · 없는 사용자 배정 404 / 없는 요청 404

### L. 인증
- TC-AUTH-001 · 미인증 SRM API 401 — 공통 인증

### M. FE E2E (playwright, 매 항목 새 context)
- TC-E2E-001 · 포털→요청 제출(동적 양식) 성공 — SCR-SRM-001/002
- TC-E2E-002 · 내 요청 목록 표시 — SCR-SRM-003
- TC-E2E-003 · 상담원 큐 화면(큐 목록·배정) — SCR-SRM-004
- TC-E2E-004 · 요청 상세: 상태 전이·코멘트 — SCR-SRM-005
- TC-E2E-005 · 승인 대기함(승인/반려) — SCR-SRM-006
- TC-E2E-006 · 종료 요청 CSAT 제출 — SCR-SRM-005
- TC-E2E-007 · 카탈로그 관리(양식 빌더) 생성 — SCR-SRM-007
- TC-E2E-008 · 지표 대시보드 — SCR-SRM-008
</content>
