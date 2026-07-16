---
date: 20260711-195405
domain: approval-engine
result: pass
keywords: [SRM 승인 게이트, 관리자 승인 프로세스 CRUD, soft-delete 유니크 제약, 승인 대기함/알림]
---

# 통합 테스트 결과 — approval-engine (Stage 1) (20260711-195405)

## 요약
- 1차: 총 25건 · 성공 23 · 실패 1(TC-ADM-006) · 범위조정 1(TC-SRM-009)
- 추가 발견 결함 1건(시나리오 외, TC-ADM-006 조사 중 발견) — soft-delete 후 동일 스코프 재생성 500
- **2차 재테스트(버그 수정 반영 후)**: TC-ADM-006 및 추가 결함 모두 수정 확인, 전체 25건 전부 PASS

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-BUILD-001 | PASS | `./gradlew build`/`test`, `npm run build` 모두 성공 | |
| TC-ADM-001 | PASS | 9개 도메인 정확히 반환(AUTH/COMMON/INFRA_MONITORING 제외), SERVICE_REQUEST/CHANGE만 hasRequestSubtype=true | |
| TC-ADM-002 | PASS | SERVICE_REQUEST→카탈로그 2건, CHANGE→STANDARD/NORMAL/EMERGENCY, INCIDENT→빈 배열 | |
| TC-ADM-003 | PASS | tier=2 OR/AND 규칙 생성 201, steps 정확 반영 | |
| TC-ADM-004 | PASS | 동일 (domain,requestSubtypeKey) tier=2 재생성 시도 409 | |
| TC-ADM-005 | PASS | 목록 조회에 priorityTier/stepCount 정확 반영 | |
| TC-ADM-006 | **PASS(재테스트)** | 수정 후 재검증: steps 1개→AND 변경, 2단계로 확장, 원복, `requesterRoleIds` 단독 교체(동일 값 재교체 포함) 모두 200 정상. 최초 시도는 FAIL이었음(아래 결함 참조, developer-be 수정 완료) | |
| TC-ADM-007 | PASS | 생성→삭제(soft delete) 후 목록에서 제외 확인 | |
| TC-ADM-008 | PASS | END_USER로 승인프로세스 API 접근 시 403(GET/POST 모두) | |
| TC-SRM-001 | PASS | AND 규칙 매칭 → IN_FULFILLMENT 전이 409 + approvalRequestId 반환, 인스턴스 IN_PROGRESS 생성 확인 | |
| TC-SRM-002 | PASS | APPROVER `scope=mine`에 노출, 무관 역할(PROCESS_OWNER)에는 미노출 | |
| TC-SRM-003 | PASS | 사유 없는 REJECT → 400(REJECT_REASON_REQUIRED) | |
| TC-SRM-004 | PASS | AND 차수: 첫 역할 승인 후 stepStatus=PENDING 유지, 두 역할 모두 승인 시 APPROVED | |
| TC-SRM-005 | PASS | 이미 결정한 역할 재결정 시 409(APPROVAL_ALREADY_DECIDED) | |
| TC-SRM-006 | PASS | 전체 승인 완료 후 IN_FULFILLMENT 재시도 200 | |
| TC-SRM-007 | PASS | OR 차수: 역할 1건 결정만으로 stepStatus/requestStatus 즉시 APPROVED | |
| TC-SRM-008 | PASS | AND 차수 중 한 역할 REJECT → 즉시 REJECTED, 이후 재시도도 409 유지 | |
| TC-SRM-009 | PASS(범위 조정) | 0차 승인 규칙 매칭 시 게이트 없이 즉시 200 확인. "매칭 규칙 자체가 없음" 조건은 테스트 중 도메인 기본(tier=1) 규칙을 이미 생성해둔 상태라 별도 재현 못 함(아래 비고) | |
| TC-NOTI-001 | PASS | playwright 새 컨텍스트, APPROVER 로그인 후 알림벨에 "서비스요청 승인" 2건 정상 노출(뱃지 카운트 일치) | |
| TC-NOTI-002 | PASS | 개별 X 확인처리 즉시 목록 제거, 뱃지 -1, 5초 polling 후에도 재노출 안 됨 | |
| TC-CHG-001 | PASS | 목록/생성/상세 정상, 상세 `approval` 필드 매칭 규칙 없을 때 값 없음(Jackson non_null 설정으로 필드 자체 생략 — 비고 참조) | |
| TC-CHG-002 | PASS | REVIEW→PLANNING→APPROVAL→IMPLEMENTATION 전 구간 게이트 없이 통과(Stage 1 CHANGE 미연동 정상 동작), UI 상세 패널도 "이 변경에는 승인 절차가 없습니다" 정상 표시 | |
| TC-CHG-003 | PASS | SCR-CHG-004/SCR-SRM-006 screen·screen_role 완전 제거 확인(DB), 사이드바에 미노출. 구 CAB 전용 엔드포인트 없음(공용 `{approvalRequestId}` 패턴에 흡수되어 타입 불일치 400) | |
| TC-PERM-001 | PASS | PROCESS_OWNER가 승인 필드 없이 카탈로그 항목 생성 201, DB에도 approval_required/approver_role 컬럼 자체 제거 확인 | |
| TC-PERM-002 | PASS | 무관 역할(PROCESS_OWNER) 결정 시도 403 | |

## 1차 실패 항목 분석 (수정 완료, 재테스트로 해소)

### TC-ADM-006 — 승인 프로세스 수정(steps 교체) 500 → 수정 확인
- **원인**: `ApprovalProcessAdminService.update()`(`source/backend/src/main/java/com/itsm/auth/application/ApprovalProcessAdminService.java:164~169`)가 기존 `approval_process_step`을 삭제한 뒤 같은 트랜잭션 내에서 즉시 재삽입하는데, Hibernate 기본 flush 순서(Insert가 Delete보다 먼저 실행)로 인해 이전 `step_no=1` 행이 지워지기 전에 새 `step_no=1` insert가 먼저 나가 `uq_approval_process_step(approval_process_id, step_no)` 유니크 제약 위반 발생.
- **재현**: steps 1개 이상인 규칙을 생성 → 동일 id로 `PATCH .../{id}`에 `steps` 포함 호출 시 100% 재현.
- **범위 확인**: `requesterRoleIds`만 교체하는 경로(동일 값 재교체 포함)는 최초부터 정상 200 — steps 교체 경로만 결함이었음.
- **조치**: developer-be가 delete 직후 flush를 강제하도록 수정, 347개 유닛테스트 통과 확인 후 8080 재기동.
- **재검증(2차)**: 1단계→AND 유지 재저장, 1단계→2단계(OR+AND) 확장, 2단계→1단계 원복, `requesterRoleIds` 단독 교체까지 전부 200 정상 확인. 기존 SR 게이트 데이터(6건)도 재기동 후 상태 그대로 보존되어 회귀 없음 확인.

## 추가 발견 결함(시나리오 외, 수정 완료)

### soft-delete 후 동일 스코프 규칙 재생성 시 500 → 수정 확인
- TC-ADM-006 재현 확인 과정에서 PATCH 대신 DELETE 후 POST로 우회 시도하다 발견.
- **원인**: `source/db/sql/26_approval_engine_schema.sql`의 부분 유니크 인덱스가 `is_deleted`를 고려하지 않음(`uq_approval_process_domain_tier1`, `uq_approval_process_domain_subtype_tier2` 모두 `WHERE priority_tier = N`만 있고 `AND is_deleted = false` 없음). 애플리케이션 레벨 충돌 검증은 삭제된 행을 걸러 "충돌 없음"으로 판단해 INSERT를 시도하지만, DB 부분 유니크 인덱스는 soft-delete된 물리적 행과 충돌해 `DataIntegrityViolationException`(500) 발생.
- **영향**: 관리자가 규칙 삭제 후 같은 스코프로 재생성하는 정상 운영 시나리오가 항상 막힘.
- **조치**: developer-db가 `28_approval_engine_index_fix.sql`로 두 인덱스 WHERE 절에 `AND is_deleted = false` 추가, fresh 컨테이너로 재검증.
- **재검증(2차)**: 규칙 생성(KNOWLEDGE 도메인 tier=1) → soft delete(200) → 동일 스코프로 재생성 시 201 정상 확인(기존엔 500).

## 2차 재테스트에서 추가 확인한 사항(회귀 없음)
- SRM AND 승인 전체 라이프사이클(제출→VALIDATED→ROUTED→게이트409→AND 양쪽 승인→재전이 200)을 신규 서비스요청으로 처음부터 다시 수행, 정상 동작 확인(수정된 빌드 기준).
- 헤더 알림·승인 대기함(SCR-COM-014) 재확인: playwright 새 컨텍스트로 APPROVER 재로그인 후 큐 2건 정상 노출, 알림 드롭다운은 1건만 노출되는데 이는 버그가 아니라 1차 테스트에서 해당 항목(approvalRequestId=5)을 이미 개별 확인처리(dismiss)했던 이력이 정상적으로 유지된 것(확인처리는 알림 표시에만 영향, 승인 대기함 큐 데이터는 별개로 계속 노출됨 — 설계대로 동작).
- CHANGE 상세(`approval:{}`, 구 CAB 엔드포인트 여전히 없음)·권한(END_USER 403, 무관 역할 403) 모두 재확인, 회귀 없음.

## 비고 — 테스트 중 확인한 환경/설계 특이사항(결함 아님)
- **단일 활성 세션(JTI) 정책**: 같은 계정으로 여러 곳에서 거의 동시에 로그인하면 서로의 세션이 무효화된다(설계상 정상). 병렬 에이전트 환경에서 공용 테스트 계정(admin@itsm.local 등) 동시 사용 시 세션 충돌이 발생해, 테스트 전용 계정(`tester-*@itsm.local`)을 별도 생성해 우회했다.
- **비매핑 도메인 응답 처리**: 정의되지 않은 경로(예: `/api/v1/changes/{id}/approvals`, 완전히 존재하지 않는 임의 경로)가 404가 아니라 500으로 응답한다. 재현해보니 승인 기능과 무관한 전역 동작(임의의 미매핑 경로 전부 동일)이라 이번 스테이지 범위 밖으로 판단해 별도 결함 보고하지 않았다. 참고차 기록.
- **TC-SRM-009 범위 조정**: 테스트 중 SERVICE_REQUEST 도메인 기본(tier=1) OR 규칙을 이미 생성해둔 상태라, 이후에는 도메인 내 모든 카탈로그 항목이 최소 이 규칙에 매칭돼 "규칙 자체가 없음" 케이스를 별도 카탈로그로 재현할 수 없었다. 대신 "0차 승인 규칙"(steps=[]) 케이스로 동일 코드 경로(0절 2번, "매칭 규칙이 없거나 차수 0개면 게이트 없이 통과")를 검증했다.
- **`approval` 필드 표시 방식**: API 응답에서 매칭 승인 프로세스가 없을 때 `{approvalRequestId: null, status: null}`이 아니라 필드 자체가 생략된 빈 객체(`{}`)로 온다. 이는 `application.yml`의 `jackson.default-property-inclusion: non_null` 전역 설정 때문으로, 승인 기능만의 문제가 아니라 앱 전체에 이미 적용된 기존 동작이다. FE에서 optional-chaining/falsy 체크로 처리 중이라 기능상 문제는 없어 결함으로 처리하지 않았다.
