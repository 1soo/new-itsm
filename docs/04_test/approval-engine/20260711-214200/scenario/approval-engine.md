# 통합 테스트 시나리오 — approval-engine (Stage 3: KNOWLEDGE 도메인 게시 승인 게이트)

## 사전 조건
- 빌드 테스트 통과(backend/frontend)
- backend(:8080)에 Stage 3 코드(`KnowledgeService`↔`ApprovalGateService.evaluateAndCreateIfNeeded`, `KnowledgeApprovalDecisionCallback`) 반영 확인
- 병렬 에이전트 세션 충돌 회피를 위한 전용 계정(비밀번호 `Test@1234`): `tester_km3_kc@itsm.local`(KNOWLEDGE_CONTRIBUTOR), `tester_km3_kg@itsm.local`(KNOWLEDGE_GATEKEEPER)
- 기존 공유 규칙(id=2, tier=3, KNOWLEDGE_CONTRIBUTOR 스코프, AND 1역할 KNOWLEDGE_GATEKEEPER)이 존재 — "매칭없음" 테스트 시에는 이 규칙의 `requesterRoleIds`를 임시로 무관 역할로 변경해 매칭을 피한 뒤 원복한다(다른 에이전트 데이터 보존)

## 시나리오

### TC-BUILD-001 · 백엔드/프론트엔드 빌드 테스트
- 근거: @docs/02_plan/api_spec/knowledge.md, @docs/02_plan/api_spec/common.md
- 절차: 1) `./gradlew build -x test`(backend) 2) `npm run build`(frontend)
- 기대 결과: 두 빌드 모두 오류 없이 성공

### TC-KM-GATE-001 · 매칭 규칙 없음 — 검토 요청 시 즉시 게시
- 근거: @docs/02_plan/api_spec/knowledge.md (API-KM-006), @docs/02_plan/api_spec/common.md (0절 2번)
- 전제: 기존 규칙(id=2)의 requesterRoleIds를 무관 역할로 임시 변경(매칭 회피)
- 절차: 1) tester_km3_kc로 기사 작성(DRAFT) 2) `PATCH .../status {targetStatus:"IN_REVIEW"}`
- 기대 결과: 200, 응답 `status="PUBLISHED"`, `approvalRequestId=null`. 승인 인스턴스 생성 없음

### TC-KM-GATE-002 · 매칭 규칙 있음 — IN_REVIEW 대기 생성
- 근거: 상동
- 전제: 기존 규칙(id=2) 원복(KNOWLEDGE_CONTRIBUTOR 스코프, AND 1역할 KNOWLEDGE_GATEKEEPER)
- 절차: 1) tester_km3_kc로 신규 기사 작성(DRAFT) 2) `PATCH .../status {targetStatus:"IN_REVIEW"}`
- 기대 결과: 200, 응답 `status="IN_REVIEW"`, `approvalRequestId` 존재. API-COM-004 상세 조회 시 인스턴스 IN_PROGRESS

### TC-KM-GATE-003 · 승인(APPROVE) 결정 → 자동 게시(콜백)
- 근거: @docs/02_plan/api_spec/common.md (API-COM-005), `KnowledgeApprovalDecisionCallback.java`
- 전제: TC-KM-GATE-002 인스턴스
- 절차: 1) tester_km3_kg(KNOWLEDGE_GATEKEEPER)로 `POST /api/v1/approvals/{id}/decisions {decision:"APPROVE"}` 2) 기사 상세(API-KM-002) 재조회
- 기대 결과: 1) 200, requestStatus=APPROVED 2) 기사 상태가 사용자의 재전이 없이 자동으로 PUBLISHED로 전환됨

### TC-KM-GATE-004 · 반려(REJECT) 결정 → 자동 DRAFT 전환 + 사유
- 근거: 상동
- 전제: TC-KM-GATE-002 절차를 반복해 신규 기사로 새 인스턴스 생성
- 절차: 1) tester_km3_kg로 `POST .../decisions {decision:"REJECT", reason:"내용 보강 필요"}` 2) 기사 상세 재조회
- 기대 결과: 1) 200, requestStatus=REJECTED 2) 기사 상태 자동 DRAFT 전환, 반려 사유는 API-COM-004 상세 조회로 확인 가능

### TC-KM-GATE-005 · 반려 후 재요청 시 신규 인스턴스 생성
- 근거: @docs/02_plan/api_spec/common.md (0절, evaluateAndCreateIfNeeded는 이전 인스턴스 상태 재사용 안 함)
- 전제: TC-KM-GATE-004에서 DRAFT로 돌아온 기사
- 절차: 1) 동일 기사로 다시 `PATCH .../status {targetStatus:"IN_REVIEW"}` 2) API-COM-003으로 해당 기사의 승인 인스턴스 개수 확인
- 기대 결과: 이전 인스턴스(REJECTED)와 별개로 새 인스턴스(IN_PROGRESS)가 생성됨(재사용 아님)

### TC-KM-UI-001 · ArticleEditPage 승인 패널 — 새로고침 후에도 상태 복원(영속)
- 근거: `source/frontend/src/features/knowledge/ArticleEditPage.tsx`(SCR-KM-003)
- 전제: TC-KM-GATE-002 상태(인스턴스 IN_PROGRESS)의 기사
- 절차: 1) playwright 새 컨텍스트로 tester_km3_kc 로그인 2) 해당 기사 편집 페이지 진입 3) 페이지 새로고침(F5 상당, 재진입)
- 기대 결과: 새로고침 전후 모두 승인 패널에 차수 진행 상태(대기중)가 동일하게 표시됨(클라이언트 상태가 아니라 API-COM-004 재조회로 복원)

### TC-COM014-002 · SCR-COM-014 승인 대기함 — KNOWLEDGE ticketKey(KM-{id}) 노출
- 근거: `KnowledgeApprovalTicketSummaryProvider.java`, @docs/02_plan/api_spec/common.md (API-COM-003)
- 전제: 진행 중인 KNOWLEDGE 승인 인스턴스 존재
- 절차: 1) playwright로 tester_km3_kg(KNOWLEDGE_GATEKEEPER) 로그인 2) `/approvals` 진입
- 기대 결과: 목록에 "지식"(KNOWLEDGE) 유형 배지와 `KM-{articleId}` 형태의 ticketKey, 기사 제목, 작성자명(tester_km3_kc의 name) 정확히 노출

### TC-KM-REGR-001 · 구 SCR-KM-004(게이트키퍼 검토 승인함) 완전 제거 확인
- 근거: 27_approval_engine_seed.sql, `docs/02_plan/screen/knowledge.md`
- 절차: 1) DB에서 `screen_role`/`screen`에 SCR-KM-004 잔존 여부 확인 2) tester_km3_kg 사이드바에 구 검토 승인함 메뉴 미노출 확인
- 기대 결과: SCR-KM-004 screen 레코드 없음(또는 존재해도 screen_role 매핑 없음), 사이드바에 미노출(공용 "승인 대기함"으로 완전 대체)
