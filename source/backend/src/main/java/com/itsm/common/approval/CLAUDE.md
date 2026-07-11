# CLAUDE.md

승인 프로세스 커스텀 기능(유지보수 요청, 2026-07-11)의 전 도메인 공용 승인 엔진. 관리자가 정의한 규칙(도메인·요청유형·승인요청자 역할 스코프 + n차 승인자)에 따라 각 도메인의 상태 전이를 게이트하고, 진행 중 승인 인스턴스를 조회·결정한다. 기존 `common.ticket.Approval`(단일 승인) 전체를 대체한다. `common.notification`과 동일한 DDD 4계층 구조.

## 하위 디렉토리
- `domain/` — 엔티티(정의 8종 중 6종 + 인스턴스)·enum·리포지토리 계약
- `infrastructure/` — 리포지토리 JPA 구현
- `application/` — 게이트 체크·대기함/상세/결정 유스케이스, 도메인 확장 포인트(ApprovalTicketSummaryProvider), DTO
- `presentation/` — REST 컨트롤러(`/api/v1/approvals`, API-COM-003~005)

## 설계 메모
- 규칙 CRUD(API-AUTH-023~029, 관리자 전용)는 `com.itsm.auth`에 위치한다(이 패키지는 인스턴스 조회·결정과 게이트 체크만 담당).
- 각 도메인은 상태 전이 서비스에서 `ApprovalGateService.checkGate(domain, requestSubtypeKey, requesterId, ticketType, ticketId)`를 호출해 게이트를 통과시킨다(막히면 `BusinessException(APPROVAL_PENDING, ..., approvalRequestId)` 409).
- 대기함·상세가 노출하는 티켓 요약(ticketKey·제목·요청자)은 `ApprovalTicketSummaryProvider` 구현 빈(도메인별 1개)을 통해 조회한다(common 모듈이 개별 도메인 저장소에 의존하지 않도록 하는 확장 포인트).
