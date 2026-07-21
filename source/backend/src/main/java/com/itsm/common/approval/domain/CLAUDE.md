# CLAUDE.md

승인 엔진 엔티티·enum·리포지토리 계약(`docs/02_plan/database/common.md` 4절).

## 파일
- `ApprovalProcess.java` — 승인 프로세스 정의 헤더(domain(nullable, null=전체 도메인)/targetState(nullable, domain 종속, 2026-07-22 4번째 매칭 축 신규)/requestSubtypeKey/priorityTier/name/description, 2026-07-15 domain nullable 재설계)
- `ApprovalProcessRequesterRole.java` — 규칙의 승인요청자 역할 스코프(ANY 매칭, 0개=요청자 무관. targetState 지정 시 최소 1개 필수 — auth.ApprovalProcessAdminService 검증)
- `ApprovalProcessStep.java` — 규칙의 승인자 차수(n차, decisionMode)
- `ApprovalProcessStepRole.java` — 차수별 승인 역할(1개 이상)
- `ApprovalRequest.java` — 승인 인스턴스 헤더(전 도메인 다형 참조 ticketType/ticketId, targetState(NOT NULL, 생성 시점 스냅샷, 2026-07-22 신규), status, currentStepNo)
- `ApprovalRequestStep.java` — 인스턴스 차수 스냅샷(규칙 변경 무관, 생성 시점 값 고정)
- `ApprovalRequestStepRole.java` — 인스턴스 차수별 필요 역할 스냅샷(AND 집계의 분모)
- `ApprovalDecision.java` — 역할별 승인/반려 결정 기록(append-only, BaseEntity 미상속, UNIQUE(step_id, role_id))
- `ApprovalRequestStatus.java` — 인스턴스 상태 enum(IN_PROGRESS, APPROVED, REJECTED)
- `ApprovalStepStatus.java` — 차수 상태 enum(PENDING, APPROVED, REJECTED, SKIPPED)
- `DecisionMode.java` — 차수 결정 방식 enum(AND, OR)
- `DecisionType.java` — 결정 값 enum(APPROVE, REJECT)

## 하위 디렉토리
- `repository/` — 리포지토리 인터페이스
