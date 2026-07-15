# 통합 테스트 결과 — common (승인 대상자 역할 기반 동적 상세조회 권한) (20260715-142838)

## 요약
- 총 14건 · 성공 14 · 실패 0
- TC-COM-013은 최초 실행 시 COMPLIANCE 부분 FAIL, developer-FE 수정(감사 로그 조회 분리) 후 재테스트하여 PASS로 전환(아래 "재테스트" 참조)
- 코드 리뷰 중 dev-lead가 추가 발견한 ESM 결함(승인자 동적 상세조회 권한이 댓글 작성 권한까지 부여하던 문제)도 수정 확인(아래 "코드 리뷰 발견 사항 재확인" 참조)

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-COM-001 | PASS | 매칭 규칙 없는 SRM 요청(비밀번호 초기화, SRM-2026-0010)에 cab@(APPROVER)로 상세 조회 → 403(정적 전체조회 폐지 확인) | curl |
| TC-COM-002 | PASS | 매칭 규칙 있는 SRM 요청(노트북 신청, SRM-2026-0012, 기존 시드 규칙 도메인=SERVICE_REQUEST/요청유형=1/요청자역할=END_USER/승인자역할=APPROVER)에 cab@로 조회 → 200 | curl |
| TC-COM-003 | PASS | 매칭 규칙 없는 변경(CHG-2026-0001)에 cab@로 조회 → 403 | curl |
| TC-COM-004 | PASS | domain=CHANGE·requestSubtypeKey=NORMAL·승인자역할=APPROVER 규칙 생성 후 cab@로 재조회 → 200 | curl |
| TC-COM-005 | PASS | 무역할 사용자(user@itsm.local, END_USER)로 인시던트(INC-2026-0001) 조회 → 403(구 결함 "인증만 하면 전체조회 가능" 정리 확인) | curl |
| TC-COM-006 | PASS | im@(INCIDENT_MANAGER)/agent@(SERVICE_DESK_AGENT) 각각 조회 → 200(기존 역할 회귀 없음) | curl |
| TC-COM-007 | PASS | domain=INCIDENT 규칙(승인자역할=APPROVER) 생성 후 cab@(APPROVER만 보유, SERVICE_DESK_AGENT/INCIDENT_MANAGER 없음)로 조회 → 200(정적 조건 불충족이어도 동적 매칭 OR로 허용) | curl |
| TC-COM-008 | PASS | domain=PROBLEM 규칙 생성 후 cab@ → 200, pm@(PROBLEM_MANAGER) → 200(매니저 전용 조건 유지 확인) | curl |
| TC-COM-009 | PASS | domain=VULNERABILITY 규칙 생성 후 cab@ → 200, vm@(VULNERABILITY_MANAGER) → 200 | curl |
| TC-COM-010 | PASS | (백엔드) 시정조치 1건 보유 요구사항(COMP-2026-0002)은 규칙 생성 후 cab@ → 200, 시정조치 0건 요구사항(COMP-2026-0001)은 규칙 생성 후에도 cab@ → 403(매칭 문맥 없음, 설계대로), co@(COMPLIANCE_OFFICER) 둘 다 200 | curl. **단, FE 렌더링에서 별도 결함 발견 — 실패 항목 분석 참조** |
| TC-COM-011 | PASS | domain=ESM 규칙 생성 후 cab@ → 200, 요청자 본인(user@) → 200, 동일 부서 DEPT_COORDINATOR(legal-coord@) → 200 | curl |
| TC-COM-012 | PASS | 자산 관련 역할이 없는 kc@(KNOWLEDGE_CONTRIBUTOR)로 자산 상세(AST-0001) 조회 → 200(회귀 없음, no-op 확인) | curl |
| TC-COM-013 | 부분FAIL → **PASS**(재테스트) | 최초 실행 시 COMPLIANCE만 부분 실패(아래 "실패 항목 분석 — 최초 실행" 참조). developer-FE 수정(`ComplianceDetailPage.tsx` 감사 로그 조회를 상세조회와 분리) 후 재테스트: domain=COMPLIANCE 규칙 재생성 → cab@(APPROVER)로 `/compliance/requirements/2` 진입 시 요구사항 상세(제목·근거·시정조치)가 정상 렌더링, 감사 로그 섹션은 403을 삼키고 "관련 감사 로그가 없습니다" 빈 상태로 정상 표시(전체 실패 없음) 확인 | playwright snapshot(재테스트) |
| TC-COM-014 | PASS | cab@(APPROVER만 보유)로 `/incidents/1` 직접 이동 → `/403`으로 리다이렉트(라우트 가드에 ROLE_APPROVER 미추가 확인) | playwright snapshot |

## 실패 항목 분석 — 최초 실행(수정 전, 참고용)

### TC-COM-013(COMPLIANCE 부분) — APPROVER 동적 매칭 성공에도 FE 상세 화면이 "찾을 수 없음"으로 오표시

- **증상**: 백엔드는 canApproverView 매칭으로 정상 200을 반환하는데도(TC-COM-010에서 확인), cab@itsm.local(APPROVER)로 `/compliance/requirements/2`에 진입하면 화면에 "요구사항을 찾을 수 없습니다."가 표시된다.
- **재현 방법**:
  1. SYSTEM_ADMIN으로 domain=COMPLIANCE, 승인자역할=APPROVER인 승인 프로세스 규칙 생성(시정조치 1건 이상 보유한 요구사항이 매칭 대상)
  2. cab@itsm.local(APPROVER) 로그인, `/compliance/requirements/2`(시정조치 보유 요구사항) 이동
  3. 네트워크 탭 확인: `GET /api/v1/compliance/requirements/2` → 200(정상), `GET /api/v1/compliance/audit-logs?requirementId=2` → 403
  4. 화면에는 "요구사항을 찾을 수 없습니다." 표시(정상 200 응답을 받았음에도)
- **근본 원인**: `source/frontend/src/features/compliance/ComplianceDetailPage.tsx`(L48-79) `refreshDetail()`이 `Promise.all([complianceApi.get(id), complianceApi.auditLogs({requirementId: id})])`로 두 호출을 묶어서 처리한다. 감사 로그 조회(API-COMP-009)는 실제로는 COMPLIANCE_OFFICER 전용으로 구현되어 있어(명세상 "인증 필요"로만 기재되어 있으나 구현은 더 제한적), 이번 유지보수로 새로 상세조회 권한을 얻은 APPROVER가 호출하면 403이 난다. `Promise.all`은 하나라도 실패하면 전체가 reject되므로, 정상적으로 200을 받은 요구사항 상세까지 함께 버려지고 `catch` 블록이 `notFound=true`로 처리해버린다.
- **영향**: 이번 유지보수의 목적(COMPLIANCE 도메인 APPROVER 동적 상세조회 허용)이 백엔드에서는 구현됐지만, 실제 사용자는 화면에서 요구사항 상세를 볼 수 없어 기능이 사실상 동작하지 않는다.
- **관련 문서**: `docs/02_plan/api_spec/common.md` 0-1절(COMPLIANCE 동적 판정 적용 대상), `docs/03_develop/plan/common.md` "승인 대상자 역할 기반 동적 상세조회 권한" COMPLIANCE 행.
- **제안**: `ComplianceDetailPage.tsx`의 감사 로그 조회를 요구사항 상세 조회와 분리하거나(감사 로그 실패를 무시하고 빈 배열로 처리), 감사 로그 조회 실패 시에도 전체를 `notFound` 처리하지 않도록 개별 `catch` 적용(FE 담당).

## 코드 리뷰 발견 사항 재확인 — ESM 승인자 동적 상세조회 권한의 댓글 작성 권한 유출(2026-07-15, dev-lead 코드 리뷰 중 발견)

- **배경**: 코드 리뷰(Standards축) 중 `EsmRequestService.assertCanView()`가 상세조회(API-ESM-007)와 댓글 작성(API-ESM-009) 양쪽에서 공용으로 쓰이고 있어, 이번 유지보수로 `canApproverView` OR 조건을 추가하면 상세조회뿐 아니라 댓글 작성 권한까지 의도치 않게 매칭된 APPROVER에게 부여되는 문제가 발견됨. `assertCanView`(공용, 댓글 등)와 `assertCanViewDetail`(상세조회 전용, canApproverView OR 포함)로 분리하는 수정 완료.
- **재확인 절차**: domain=ESM·승인자역할=APPROVER 매칭 규칙 생성 → cab@itsm.local(APPROVER, 요청자 본인도 DEPT_COORDINATOR도 아님)로
  1. `GET /api/v1/esm/requests/3` → 200(상세조회는 동적 매칭으로 허용)
  2. `POST /api/v1/esm/requests/3/comments` → 403(댓글 작성은 여전히 거부, 동적 매칭이 영향 주지 않음 확인)
- **결과**: PASS. 재확인용으로 생성한 규칙은 삭제 완료.

