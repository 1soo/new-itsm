# 통합 테스트 결과 — approval-engine (Stage 3: KNOWLEDGE 도메인 게시 승인 게이트) (20260711-214200)

## 요약
- 총 9건 · 성공 9 · 실패 0 ✅ **전 항목 통과**

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 | PASS | `./gradlew compileJava -q`, `npm run build` 모두 성공 | |
| TC-KM-GATE-001 | PASS | 매칭 규칙 없음(기존 규칙의 requesterRoleIds를 임시로 무관 역할로 돌려 매칭 회피) 상태에서 검토 요청 시 200, `status="PUBLISHED"`, `approvalRequestId` 없음(인스턴스 미생성) | 확인 즉시 원래 스코프로 원복 |
| TC-KM-GATE-002 | PASS | 매칭 규칙 있음(id=2, tier=3, KNOWLEDGE_CONTRIBUTOR 스코프, AND 1역할 KNOWLEDGE_GATEKEEPER) 상태에서 검토 요청 시 200, `status="IN_REVIEW"`, `approvalRequestId=8` 반환. API-COM-004 상세에서 인스턴스 IN_PROGRESS 확인 | article id=10 |
| TC-KM-GATE-003 | PASS | KNOWLEDGE_GATEKEEPER APPROVE 결정 → 200(requestStatus=APPROVED) → 기사 상세(API-KM-002) 재조회 시 **사용자의 별도 전이 요청 없이** status가 PUBLISHED로 자동 전환됨(`KnowledgeApprovalDecisionCallback.onApproved` 정상 동작). ticketKey도 `KM-10` 형식으로 정확히 확인 | |
| TC-KM-GATE-004 | PASS | 신규 기사(id=11)로 IN_REVIEW 진입 후 KNOWLEDGE_GATEKEEPER REJECT(사유 "내용 보강 필요") → 200(requestStatus=REJECTED) → 기사 상태 자동 DRAFT 전환, API-COM-004 상세 조회로 반려 사유 정상 확인 | |
| TC-KM-GATE-005 | PASS | DRAFT로 돌아온 기사(id=11)를 재차 검토 요청 → 새 인스턴스(id=10, IN_PROGRESS) 생성, 이전 REJECTED 인스턴스(id=9)는 이력으로 그대로 보존(재사용 아님, DB 직접 확인) | |
| TC-KM-UI-001 | PASS | ArticleEditPage(SCR-KM-003)에서 승인 대기 중 기사 진입 시 "승인 현황: 1차 대기중" 표시, 페이지 전체 재진입(새로고침 상당) 후에도 동일하게 복원됨(클라이언트 캐시가 아닌 API-COM-004 재조회 확인) | |
| TC-COM014-002 | PASS | KNOWLEDGE_GATEKEEPER 계정으로 `/approvals` 진입 시 "지식" 배지 + `KM-11 · Stage3 matched-rule test article B (reject)` 형태의 ticketKey·제목, 요청자명("Tester KM3 Contributor") 정확히 노출. 타 에이전트의 동시 진행 중인 KM 테스트 데이터(KM-5, KM-12~15)와 충돌 없이 함께 표시됨 | |
| TC-KM-REGR-001 | PASS | DB `screen` 테이블에 `SCR-KM-004` 레코드 자체가 존재하지 않음(완전 제거 확인), KNOWLEDGE_GATEKEEPER 사이드바에도 구 검토 승인함 메뉴 없이 "지식베이스 검색/목록"·"지식 지표 대시보드"만 노출(공용 "승인 대기함(전 도메인 공용)"으로 대체) | |

## 실패 항목 분석

없음(전 항목 통과).

## 테스트 환경 조성 참고

- 병렬 에이전트 세션 충돌 회피를 위해 전용 계정 `tester_km3_kc@itsm.local`(KNOWLEDGE_CONTRIBUTOR)·`tester_km3_kg@itsm.local`(KNOWLEDGE_GATEKEEPER, 비밀번호 `Test@1234`)을 신규 생성해 사용했다.
- 기존 공유 규칙(id=2, "KM E2E 테스트 승인")의 `requesterRoleIds`를 "매칭없음" 케이스 검증을 위해 잠시 SYSTEM_ADMIN으로 변경했다가 검증 직후 원래 값(KNOWLEDGE_CONTRIBUTOR)으로 즉시 원복했다(Stage 2 CHANGE 테스트에서 사용한 것과 동일한 임시 전환 방식 — soft delete 대신 스코프만 일시 변경해 다른 에이전트의 데이터 보존).
- 테스트용 기사(id 9~11)가 생성되어 남아있다(9=PUBLISHED, 10=PUBLISHED, 11=DRAFT). 실제 업무 데이터가 아니므로 정리가 필요하면 dev-lead-2에 확인 요청.
