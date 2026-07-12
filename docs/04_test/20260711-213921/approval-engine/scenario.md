# 통합 테스트 시나리오 — approval-engine (Stage 3: KNOWLEDGE 게시 승인 게이트)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- Stage 1/2 산출물(공용 승인 엔진, SCR-ADMIN-007/008, SCR-COM-014) 정상 동작 전제
- 테스트 계정(공통 비밀번호 `Admin@1234`): `tester-admin@itsm.local`(SYSTEM_ADMIN), 신규 필요 시 KNOWLEDGE_CONTRIBUTOR/KNOWLEDGE_GATEKEEPER 테스트 계정 생성

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/api_spec/knowledge.md, @docs/02_plan/api_spec/common.md
- 절차: 1) `./gradlew build` 2) `npm run build`
- 기대 결과: 두 빌드 모두 성공

### TC-KMGATE-001 · 매칭 규칙 없음 — 검토 요청 즉시 PUBLISHED
- 근거: @docs/02_plan/api_spec/knowledge.md (API-KM-006), @docs/02_plan/screen/knowledge.md (SCR-KM-003)
- 전제: KNOWLEDGE 도메인에 매칭되는 승인 프로세스 규칙 없음
- 절차: 1) 기사 작성(DRAFT) 2) `PATCH .../status {targetStatus:"IN_REVIEW"}`
- 기대 결과: 200, `status="PUBLISHED"`, `approvalRequestId=null`(승인 절차 없음 안내)

### TC-KMADM-001 · KNOWLEDGE 승인 프로세스 규칙 생성(요청자 역할=KNOWLEDGE_CONTRIBUTOR 스코프)
- 근거: @docs/02_plan/api_spec/auth.md (API-AUTH-027)
- 절차: 1) `POST /api/v1/admin/approval-processes` domain=KNOWLEDGE, requesterRoleIds=[KNOWLEDGE_CONTRIBUTOR], steps=[{decisionMode:OR, roleIds:[KNOWLEDGE_GATEKEEPER]}]
- 기대 결과: 201 생성 성공

### TC-KMGATE-002 · 매칭 규칙 있음 — 검토 요청 시 IN_REVIEW+인스턴스 생성(항상 200)
- 근거: @docs/02_plan/api_spec/knowledge.md (API-KM-006)
- 전제: TC-KMADM-001 규칙 적용 대상 기여자로 신규 기사 작성
- 절차: 1) `PATCH .../status {targetStatus:"IN_REVIEW"}`
- 기대 결과: 200(409 아님), `status="IN_REVIEW"`, `approvalRequestId` 반환, 인스턴스 IN_PROGRESS 생성 확인(API-COM-004)

### TC-KMGATE-003 · 승인 패널 영속성 — 새로고침 후에도 유지
- 근거: @docs/02_plan/screen/knowledge.md (SCR-KM-003), @docs/02_plan/api_spec/knowledge.md (API-KM-002)
- 절차: 1) 기사 상세 재조회(API-KM-002)
- 기대 결과: `approval` 관련 상태가 유지되어 승인 패널이 "1차 대기중" 등으로 정상 표시(값 소실 없음)

### TC-KMGATE-004 · 게이트키퍼 승인 → 자동 PUBLISHED 전환
- 근거: @docs/02_plan/api_spec/common.md (API-COM-005), @docs/02_plan/api_spec/knowledge.md (API-KM-006)
- 절차: 1) KNOWLEDGE_GATEKEEPER로 승인 결정(APPROVE) 2) 기사 상세 재조회
- 기대 결과: 결정 즉시(사용자가 별도 재시도하지 않아도) 기사 status가 PUBLISHED로 자동 전환

### TC-KMGATE-005 · 반려 케이스 — 자동 DRAFT 복귀 + 사유 표시 + 재요청 시 신규 인스턴스
- 근거: @docs/02_plan/api_spec/knowledge.md (API-KM-006 반려 동작 계승)
- 절차: 1) 신규 기사 작성 후 검토 요청(IN_REVIEW) 2) 게이트키퍼 REJECT(사유 포함) 3) 기사 상세 조회(자동 DRAFT 복귀+반려사유 확인) 4) "검토 요청" 재클릭(신규 인스턴스 생성)
- 기대 결과: 3) status=DRAFT, 반려사유 노출(새로고침 후에도 유지) 4) 새 approvalRequestId 발급(이전 반려 인스턴스와 분리)

### TC-KMGATE-006 · SCR-COM-014 ticketKey 노출 확인
- 근거: dev-lead-2 지시(이전 버그 수정), @docs/02_plan/api_spec/common.md (API-COM-003)
- 절차: 1) GATEKEEPER로 `GET /api/v1/approvals?scope=mine` 조회
- 기대 결과: KNOWLEDGE 항목의 `ticketKey`가 "KM-{id}" 형식으로 정상 노출(undefined 아님)

### TC-KMREG-001 · SCR-KM-004 완전 제거 확인
- 근거: @docs/02_plan/screen/knowledge.md (SCR-KM-004 제거)
- 절차: 1) DB에서 `screen`/`screen_role`에 SCR-KM-004 존재 여부 조회
- 기대 결과: 화면·역할매핑 모두 제거됨(레코드 없음)

### TC-CROSSREG-001 · SRM/CHANGE 승인 흐름 스팟 체크(회귀 없음)
- 근거: dev-lead-2 지시
- 절차: 1) 기존 SRM 승인 대기(있으면) 상세 조회 2) 기존 CHANGE 승인 완료 건 상세 조회
- 기대 결과: 두 도메인 모두 기존 상태·approval 필드 값 그대로 유지, 오류 없음
