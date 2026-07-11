# CLAUDE.md

승인 엔진 애플리케이션 서비스 계층.

## 파일
- `ApprovalGateService.java` — 게이트 체크(common.md 0절). 우선순위 매칭 → 0차/무매칭 통과 → 인스턴스 없음 시 스냅샷 생성+409 → 진행중/반려 409 → 승인완료 통과
- `ApprovalInstanceService.java` — 대기함 목록(API-COM-003)·상세(API-COM-004)·결정(API-COM-005) 유스케이스. OR(최초 1건 확정)/AND(역할별 전원 승인 또는 즉시 반려) 집계, 차수 진행·SKIPPED 처리
- `ApprovalRoleResolver.java` — role_id ↔ role_code(role claim) 상호 변환 공용 헬퍼(auth.domain.repository.RoleRepository 위임)
- `ApprovalTicketSummaryProvider.java` — 대기함·상세가 노출할 티켓 요약(ticketKey·제목·요청자) 조회 확장 포인트. 도메인별 구현 빈을 등록(현재 SRM, 이후 단계에서 도메인 추가)
- `TicketSummary.java` — 위 확장 포인트의 반환 타입(ticketKey, title, requesterName)
- `ApprovalRequestSubtypeProvider.java` — 관리자 CRUD(API-AUTH-024)가 도메인별 요청유형 후보를 조회하기 위한 확장 포인트(하위유형 있는 도메인만 구현 빈 등록, 현재 SRM). CHANGE는 고정 코드라 auth 서비스가 직접 하드코딩하고 이 확장 포인트를 사용하지 않는다
- `RequestSubtypeOption.java` — 위 확장 포인트의 반환 타입(key, label). auth의 API-AUTH-024 응답으로 그대로 재사용된다

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
