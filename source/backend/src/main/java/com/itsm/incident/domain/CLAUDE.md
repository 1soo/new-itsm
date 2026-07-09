# CLAUDE.md

incident 도메인의 엔티티·enum·리포지토리 계약.

## 파일
- `Incident.java` — 인시던트 엔티티(요약·심각도·우선순위·상태·영향 대상·시각 지표)
- `IncidentResponder.java` — 인시던트 대응자 배정 엔티티
- `IncidentSeverityHistory.java` — 심각도 변경 이력 엔티티
- `Postmortem.java` — 포스트모템 엔티티(요약·타임라인·근본원인)
- `PostmortemActionItem.java` — 포스트모템 후속 액션 아이템 엔티티
- `PostmortemFiveWhy.java` — 포스트모템 5Why 항목 엔티티
- `IncidentStatus.java` — 상태 enum(NEW, IN_PROGRESS, RESOLVED, CLOSED)
- `Severity.java` — 심각도 enum(SEV1, SEV2, SEV3)
- `Priority.java` — 우선순위 enum(P1~P4)
- `ResponseRole.java` — 대응 역할 enum(TECH_LEAD, COMMS, SCRIBE)
- `EscalationType.java` — 에스컬레이션 유형 enum(HIERARCHICAL, FUNCTIONAL)
- `ActionItemStatus.java` — 액션 아이템 상태 enum(OPEN, DONE)

## 하위 디렉토리
- `repository/` — 리포지토리 인터페이스
