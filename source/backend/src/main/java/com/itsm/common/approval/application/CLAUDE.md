# CLAUDE.md

승인 엔진 애플리케이션 서비스 계층.

## 파일
- `ApprovalGateService.java` — 게이트 체크(common.md 0절). `matchProcess`는 티켓 도메인 규칙 + 전체 도메인(domain null) 규칙을 함께 후보로 조회해 priorityTier가 가장 큰 규칙을 선택(2026-07-15 3축 재설계). `checkGate`(SRM/CHANGE류, 던지는 방식): 우선순위 매칭 → 0차/무매칭 통과 → 인스턴스 없음 시 스냅샷 생성+409 → 진행중/반려 409 → 승인완료 통과. `evaluateAndCreateIfNeeded`(KNOWLEDGE류, 논-스로잉): 항상 200 성공해야 하는 도메인용, `GateDecision`(passed, approvalRequestId) 반환, 매칭 시 이전 인스턴스 상태 무관하게 항상 새 인스턴스 생성. `canApproverView(domain, requestSubtypeKey, requesterId)` — 승인 대상자 역할 기반 동적 상세조회 권한(common.md 0-1절, 2026-07-15). `matchProcess`로 매칭된 규칙의 **전체 차수**(현재 차수 아님) 승인자 역할을 모아 로그인 사용자(조회자) role과 교집합 판정. SRM/CHANGE/INCIDENT/PROBLEM/ASSET/VULNERABILITY/COMPLIANCE/ESM 8개 도메인 상세조회 가드가 기존 조건과 OR로 호출
- `ApprovalInstanceService.java` — 대기함 목록(API-COM-003)·상세(API-COM-004)·결정(API-COM-005) 유스케이스. OR(최초 1건 확정)/AND(역할별 전원 승인 또는 즉시 반려) 집계, 차수 진행·SKIPPED 처리. 인스턴스 최종 확정(APPROVED/REJECTED) 시 `ApprovalDecisionCallback` 구현 빈(ticketType별) 호출
- `ApprovalDecisionCallback.java` — 승인 인스턴스 최종 확정 시 도메인 반응용 확장 포인트(onApproved/onRejected). 전이 재시도하는 SRM/CHANGE는 불필요, 결정 즉시 자동 전환 필요한 KNOWLEDGE가 구현
- `ApprovalRoleResolver.java` — role_id ↔ role_code(role claim) 상호 변환 공용 헬퍼(auth.domain.repository.RoleRepository 위임)
- `ApprovalTicketSummaryProvider.java` — 대기함·상세가 노출할 티켓 요약(ticketKey·제목·요청자) 조회 확장 포인트. 도메인별 구현 빈 등록(현재 SRM/CHANGE/KNOWLEDGE)
- `TicketSummary.java` — 위 확장 포인트 반환 타입(ticketKey, title, requesterName)
- `ApprovalRequestSubtypeProvider.java` — 관리자 CRUD(API-AUTH-024)가 도메인별 요청유형 후보 조회용 확장 포인트(하위유형 있는 도메인만 구현 빈 등록, 현재 SRM). CHANGE는 고정 코드라 auth 서비스가 직접 하드코딩, 이 확장 포인트 미사용
- `RequestSubtypeOption.java` — 위 확장 포인트 반환 타입(key, label). auth의 API-AUTH-024 응답으로 그대로 재사용

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
