---
date: 20260712-014911
domain: common
change_type: [new, removed]
keywords: [공용승인엔진, ApprovalGateService, 하드코딩제거]
---

# 유지보수 이력 — common

> 유지보수 일시: 20260712-014911 · 도메인: common

## 1. 요구사항

관리자가 도메인·요청유형·승인요청자 역할 기준으로 커스텀 다단계 승인 프로세스를 정의할 수 있어야 한다.
기존 도메인별로 하드코딩되어 있던 승인 로직(CHANGE의 CAB, SRM의 approverRole, KNOWLEDGE의 게이트키퍼)을 대체할 공용 승인 엔진이 필요하다.
승인 프로세스가 없던 6개 도메인(INCIDENT/PROBLEM/ASSET/VULNERABILITY/COMPLIANCE/ESM)에도 게이트를 신규로 추가할 수 있는 확장 가능한 구조가 필요하다.
각 도메인은 승인 요청 발생 시 공용 승인 대기함(SCR-COM-014)에 도메인별 티켓 요약 정보를 제공할 수 있어야 한다.
각 도메인은 승인 완료·반려 결정 이후 도메인별 후속 처리(예: 자동 게시 전환)를 수행할 수 있어야 한다.

## 2. 해결 방법

공용 승인 엔진 DB 스키마(approval_process/step/step_role/request/request_step/request_step_role/decision)를 신규 설계·구축했다.
임의 도메인의 상태전이 시점에 게이트 체크를 수행하는 `ApprovalGateService.checkGate()`를 구현했다.
도메인이 승인 완료/반려 후 후속 처리를 구현할 수 있도록 `ApprovalDecisionCallback` SPI를 확장점으로 신규 추가했다(KNOWLEDGE 연동 시점인 9c51fa0에서 도입).
공용 승인 대기함에서 도메인별 티켓 요약을 노출할 수 있도록 `ApprovalTicketSummaryProvider` 확장점을 제공했다.
공용 승인 API(API-COM-003~005)를 신설하고, 공용 승인 대기함 화면(ApprovalInboxPage)과 공용 컴포넌트(approval-panel, approval-process-flow, approval-schema, approval-step-progress)를 신규 구현했다.
기존 change/ticket 패키지에 있던 하드코딩 Approval/ApprovalStatus 엔티티와 리포지토리를 제거했다.
COMPLIANCE/ESM 연동 시점(f421aa9)에 `TicketType`에 CORRECTIVE_ACTION/ESM_REQUEST를 추가하고, 공용 승인 대기함의 티켓 유형 라벨 매핑을 보강했다.

## 3. 변경 파일

- `source/backend/.../common/approval/**`(domain/application/infrastructure/presentation 전체 신규 — ApprovalProcess*, ApprovalRequest*, ApprovalDecision, ApprovalGateService, ApprovalInstanceService, ApprovalController 등)
- `source/backend/.../common/exception/{BusinessException,ErrorCode,ErrorResponse,GlobalExceptionHandler}.java`
- `source/backend/.../common/ticket/{Approval,ApprovalStatus}.java`(제거)
- `source/backend/.../common/ticket/persistence/ApprovalJpaRepository.java`(제거)
- `source/backend/.../common/ticket/repository/ApprovalRepository.java`(제거)
- `source/db/sql/26_approval_engine_schema.sql`
- `source/db/sql/27_approval_engine_seed.sql`
- `source/db/sql/28_approval_engine_index_fix.sql`
- `source/frontend/src/components/common/{approval-panel.tsx,approval-process-flow.tsx,approval-schema.ts,approval-step-progress.tsx,index.ts}`
- `source/frontend/src/components/ui/sheet.tsx`(신규)
- `source/frontend/src/features/common/{ApprovalInboxPage.tsx,api.ts,format.ts,status.ts,types.ts}`
- `source/frontend/src/routes/{AppLayout.tsx,index.tsx}`
- `docs/02_plan/{api_spec/common.md,database/common.md,screen/common.md}`
- `source/backend/.../common/approval/application/ApprovalDecisionCallback.java`(신규, 9c51fa0)
- `source/backend/.../common/ticket/TicketType.java`(f421aa9)
- `source/frontend/src/features/common/{status.ts,types.ts}`(f421aa9, 라벨 매핑 보강)

## 4. 테스트 결과

Stage 1 통합 테스트 총 25건 중 1차 성공 23·실패 1(TC-ADM-006)·범위조정 1(TC-SRM-009), 2차 재테스트에서 25건 전부 PASS했다.
승인 프로세스 steps 수정 시 500 발생 결함(Hibernate flush 순서로 delete 전에 insert가 나가 유니크 제약 위반)과, soft-delete 후 동일 스코프 재생성 시 500 발생 결함(부분 유니크 인덱스가 is_deleted를 고려하지 않음)을 발견해 수정했다.
이후 CHANGE(13건)/KNOWLEDGE(9건)/INCIDENT+PROBLEM(19건+재테스트 6건)/ASSET+VULNERABILITY(18건)/COMPLIANCE+ESM(19건) 전 스테이지에서 공용 엔진 관련 회귀는 발견되지 않았다.
커밋 `0b1caa3`(엔진 최초 구축), `9c51fa0`(ApprovalDecisionCallback SPI 확장), `f421aa9`(TicketType 라벨 보강)로 반영했다.
