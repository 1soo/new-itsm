# 통합 테스트 시나리오 — approval-engine (Stage 2: CHANGE 게이트 실연동)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- Stage 1 산출물(공용 승인 엔진, SCR-ADMIN-007/008, SCR-COM-014) 정상 동작 전제
- 테스트 계정(공통 비밀번호 `Admin@1234`): `tester-admin@itsm.local`(SYSTEM_ADMIN), `tester-cm@itsm.local`(CHANGE_MANAGER), `tester-approver@itsm.local`(APPROVER), `tester-user@itsm.local`(END_USER)

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/api_spec/change.md, @docs/02_plan/api_spec/common.md
- 절차: 1) `./gradlew build`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-CHGADM-001 · 관리자 요청유형 후보(CHANGE)
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-024)
- 절차: 1) `GET /api/v1/admin/approval-processes/domains/CHANGE/request-subtypes`
- 기대 결과: 200, STANDARD/NORMAL/EMERGENCY 3건 정확 반환

### TC-CHGGATE-001 · CHANGE 승인 프로세스 규칙 생성(tier=2, NORMAL 유형 전용)
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-027)
- 절차: 1) `POST /api/v1/admin/approval-processes` domain=CHANGE, requestSubtypeKey=NORMAL, steps=[{decisionMode:AND, roleIds:[APPROVER,CHANGE_MANAGER]}]
- 기대 결과: 201 정상 생성

### TC-CHGGATE-002 · CHANGE 게이트 E2E — REQUESTED~APPROVAL 통과, IMPLEMENTATION 전이 시 409
- 근거: @docs/02_plan/api_spec/change.md (API-CHG-004 409), @docs/02_plan/api_spec/common.md (0절)
- 전제: type=NORMAL RFC 생성
- 절차: 1) RFC 생성 2) REVIEW→PLANNING→APPROVAL 순차 전이(정상 통과) 3) IMPLEMENTATION 전이 시도
- 기대 결과: 3) 409 + approvalRequestId 반환, 승인 인스턴스 IN_PROGRESS 생성 확인(API-COM-004)

### TC-CHGGATE-003 · SCR-COM-014에서 CHANGE 티켓 요약 노출
- 근거: @docs/02_plan/api_spec/common.md (API-COM-003), @docs/02_plan/screen/common.md (SCR-COM-014)
- 절차: 1) APPROVER로 `GET /api/v1/approvals?scope=mine` 조회
- 기대 결과: ticketType=CHANGE, ticketKey·summary·요청자 정상 노출

### TC-CHGGATE-004 · 승인 결정 처리 및 재전이 허용
- 근거: @docs/02_plan/api_spec/common.md (API-COM-005), @docs/02_plan/api_spec/change.md (API-CHG-004)
- 절차: 1) AND 차수 양쪽 역할 모두 승인 2) IMPLEMENTATION 재시도
- 기대 결과: 1) 마지막 승인 시 requestStatus=APPROVED 2) 200 전이 성공, 상세 조회 시 `approval.status`=APPROVED

### TC-CHGGATE-005 · 매칭 규칙 없음/0차 — 게이트 없이 통과
- 근거: @docs/02_plan/api_spec/common.md (0절 2번)
- 절차: 1) type=EMERGENCY(매칭 규칙 없음) RFC 생성 후 REVIEW~IMPLEMENTATION까지 전이
- 기대 결과: 게이트 없이 즉시 전이 허용(409 없음)

### TC-CHGUI-001 · IMPLEMENTATION 버튼 disabled+tooltip(승인 대기 중)
- 근거: dev-lead-2 지시, @docs/02_plan/screen/change.md (SCR-CHG-003 상태·인터랙션)
- 절차: 1) playwright 새 컨텍스트, CHANGE_MANAGER 로그인 2) 승인 대기 중인 NORMAL 변경 상세 진입
- 기대 결과: IMPLEMENTATION 버튼이 숨김이 아니라 disabled 상태 + 툴팁으로 승인 대기 안내 표시(SRM의 버튼 숨김 방식과 의도된 차이)

### TC-CHGUI-002 · 전이 실패(409) 시 승인 패널 즉시 갱신
- 근거: dev-lead-2 지시
- 절차: 1) 승인 대기 중 변경에서 IMPLEMENTATION 전이 시도(409 발생 유도) 2) 승인 패널 상태 확인
- 기대 결과: 새로고침 없이 승인 패널이 즉시 진행 상태(차수·역할별 결정 현황)를 반영

### TC-CHGREG-001 · CHANGE 목록/생성/분류/일정/지표 회귀
- 근거: @docs/02_plan/api_spec/change.md (API-CHG-001/002/005/010/012)
- 절차: 1) 목록 조회 2) RFC 생성 3) `PATCH .../classification` 4) `GET .../schedule` 5) `GET .../metrics`
- 기대 결과: 전부 정상 응답(200/201)

### TC-SRMREG-001 · SRM 게이트 패널 즉시 갱신 재확인(공용 로직 영향 확인)
- 근거: dev-lead-2 지시(공용 패널 갱신 수정이 SRM에도 영향 줬을 수 있음)
- 절차: 1) SRM 승인 대기 중인 서비스요청 상세에서 이행 전이 시도(409) 2) 승인 패널 갱신 확인
- 기대 결과: Stage 1 동작(정상 게이트 차단) 유지, 패널 즉시 갱신도 정상(회귀 없음)
