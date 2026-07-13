# CLAUDE.md

change 도메인 엔티티·enum·리포지토리 계약.

## 파일
- `ChangeRequest.java` — 변경 요청(RFC) 엔티티(유형·위험도·상태·구현결과). 승인경로(approvalRoute) 필드는 2026-07-11 승인 프로세스 커스텀 기능으로 제거됨
- `ChangeTemplate.java` — 표준 변경 사전승인 템플릿 엔티티
- `ChangeAffectedSystem.java` — 변경 영향 시스템 엔티티
- `ChangeType.java` — 유형 enum(STANDARD, NORMAL, EMERGENCY)
- `ChangeRisk.java` — 위험도 enum(HIGH, MEDIUM, LOW, 미평가 시 null)
- `ChangeStatus.java` — 상태 enum(REQUESTED, REVIEW, PLANNING, APPROVAL, IMPLEMENTATION, CLOSED)
- `Outcome.java` — 구현 결과 enum(SUCCESS, FAILURE)
- `LinkTargetType.java` — 연계 대상 유형 enum(INCIDENT, PROBLEM)

## 하위 디렉토리
- `repository/` — 리포지토리 인터페이스
