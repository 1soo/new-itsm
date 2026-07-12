# 통합 테스트 시나리오 — approval-engine (Stage 2: CHANGE 도메인 게이트 연동)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- backend(:8080)에 Stage 2 코드(`ChangeService.checkGate` 연동) 반영 확인
- 테스트 계정(공통 비밀번호 `Admin@1234`): `admin@itsm.local`(SYSTEM_ADMIN)
- 병렬 에이전트 세션 충돌(단일 JTI 정책) 회피를 위해 전용 테스트 계정 신규 생성(비밀번호 `Test@1234`): `tester_chg2_cm@itsm.local`(CHANGE_MANAGER), `tester_chg2_appr@itsm.local`(APPROVER)

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/api_spec/change.md, @docs/02_plan/api_spec/common.md
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-CHGADM-001 · 승인 프로세스 규칙 생성 — CHANGE, requestSubtype=STANDARD, tier=2, OR 1차
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-024/027)
- 절차: 1) `GET /api/v1/admin/approval-processes/domains/CHANGE/request-subtypes`(STANDARD/NORMAL/EMERGENCY 확인) 2) `POST /api/v1/admin/approval-processes` body: domain=CHANGE, requestSubtypeKey="STANDARD", requesterRoleIds=[], name="표준 변경 승인", steps=[{decisionMode:"OR", roleIds:[APPROVER]}]
- 기대 결과: 201, priorityTier=2, steps 1개(OR, APPROVER)

### TC-CHGADM-002 · 승인 프로세스 규칙 수정 — steps AND 2역할로 확장
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-028)
- 전제: TC-CHGADM-001 규칙 존재
- 절차: 1) `PATCH /api/v1/admin/approval-processes/{id}` body: steps=[{decisionMode:"AND", roleIds:[APPROVER, CHANGE_MANAGER]}]
- 기대 결과: 200, 차수 1개 AND(역할 2개)로 갱신, 기존 steps 삭제 재삽입 시 500(TC-ADM-006류 회귀) 없이 정상 처리

### TC-CHG-GATE-001 · STANDARD 변경 요청 IMPLEMENTATION 전이 게이트 — 409 + 인스턴스 생성
- 근거: @docs/02_plan/api_spec/common.md (0절 게이트), @docs/02_plan/api_spec/change.md (API-CHG-004)
- 전제: TC-CHGADM-002에서 CHANGE/STANDARD에 AND(APPROVER+CHANGE_MANAGER) 규칙 존재
- 절차: 1) tester_chg2_cm으로 STANDARD 변경 요청 생성 2) REVIEW→PLANNING→APPROVAL 순차 전이 3) `PATCH .../status {targetStatus:"IMPLEMENTATION"}` 시도
- 기대 결과: 409, 응답에 `approvalRequestId` 포함, API-COM-004 상세 조회 시 인스턴스 status=IN_PROGRESS

### TC-CHG-GATE-002 · AND 결정 — 양쪽 역할 모두 승인 후 재전이 허용
- 근거: @docs/02_plan/api_spec/common.md (0절 결정 처리, API-COM-005)
- 전제: TC-CHG-GATE-001 인스턴스
- 절차: 1) tester_chg2_appr(APPROVER)로 APPROVE 2) 상세 조회로 stepStatus=PENDING(CHANGE_MANAGER 슬롯 대기) 확인 3) tester_chg2_cm(CHANGE_MANAGER)으로 APPROVE 4) `PATCH .../status {targetStatus:"IMPLEMENTATION"}` 재시도
- 기대 결과: 3) 이후 stepStatus=APPROVED, requestStatus=APPROVED. 4) 200, IMPLEMENTATION 전이 허용

### TC-CHG-UI-001 · ChangeDetailPage 승인 패널·버튼 disable(게이트 대기 중)
- 근거: @docs/02_plan/screen/change.md, `source/frontend/src/features/change/ChangeDetailPage.tsx`
- 전제: TC-CHG-GATE-001 상태(인스턴스 IN_PROGRESS)의 변경 요청
- 절차: 1) playwright 새 컨텍스트로 tester_chg2_cm 로그인 2) 해당 변경 상세 페이지 진입
- 기대 결과: "구현" 상태 전이 버튼 disabled(tooltip "승인 완료 전에는 구현 단계로 전이할 수 없습니다"), 승인 패널에 진행 중 차수·역할별 결정 현황 표시

### TC-CHG-UI-002 · ChangeDetailPage 승인 완료 후 버튼 활성화
- 근거: 상동
- 전제: TC-CHG-GATE-002로 인스턴스 APPROVED 완료된 변경 요청
- 절차: 1) 같은 변경 상세 페이지 새로고침
- 기대 결과: "구현" 버튼 활성화(클릭 가능), 승인 패널 상태 APPROVED로 표시

### TC-COM014-001 · SCR-COM-014 승인 대기함 — CHANGE 티켓 요약 노출
- 근거: @docs/02_plan/api_spec/common.md (API-COM-003), `ChangeApprovalTicketSummaryProvider.java`
- 전제: 진행 중인 CHANGE 승인 인스턴스 존재(TC-CHG-GATE-001 재현 또는 신규 1건)
- 절차: 1) playwright 새 컨텍스트로 tester_chg2_appr(APPROVER) 로그인 2) `/approvals` 진입
- 기대 결과: 목록에 CHANGE 티켓 유형 배지, ticketKey·summary(요약)·요청자명(tester_chg2_cm의 name)이 정확히 노출

### TC-CHG-REGR-001 · CHANGE 목록/생성/상세 회귀(공용 로직 변경 영향 확인)
- 근거: @docs/02_plan/api_spec/change.md
- 절차: 1) `GET /api/v1/changes` 2) 매칭 규칙 없는 EMERGENCY 유형으로 신규 생성 3) 상세 조회
- 기대 결과: 정상 동작, EMERGENCY는 매칭 규칙 없어 `approval` 필드 빈 값(`{}`)이며 IMPLEMENTATION까지 게이트 없이 통과(회귀 없음)

### TC-SRM-REGR-001 · SRM 승인 게이트 회귀(공용 엔진 변경 영향 스팟 체크)
- 근거: @docs/02_plan/api_spec/service-request.md, @docs/02_plan/api_spec/common.md
- 절차: 1) 기존 SRM 진행 중/완료 승인 인스턴스 데이터가 Stage 2 배포 후에도 유지되는지 API-COM-003/004로 조회
- 기대 결과: Stage 1에서 생성된 SRM 인스턴스 상태가 그대로 보존(전체 재실행은 불필요, 데이터 보존 확인으로 갈음)
