# CLAUDE.md

problem 도메인 엔티티·enum·리포지토리 계약.

## 파일
- `Problem.java` — 문제 엔티티(요약·출처·상태·우선순위·영향도·긴급도·RCA·워크어라운드)
- `ProblemAction.java` — 후속 조치 엔티티
- `ProblemFiveWhy.java` — RCA 5Why 항목 엔티티
- `KnownError.java` — 기지 오류 엔티티(title·rootCause·workaround, 검색 대상)
- `ProblemStatus.java` — 상태 enum(DETECTION, CLASSIFICATION, INVESTIGATION, KNOWN_ERROR, WORKAROUND, RESOLVED_CLOSED)
- `ProblemOrigin.java` — 출처 enum(REACTIVE, PROACTIVE)
- `ProblemPriority.java` — 우선순위 enum(P1~P4)
- `Level.java` — 영향도/긴급도 수준 enum(HIGH, MEDIUM, LOW)
- `ActionStatus.java` — 후속 조치 상태 enum(IN_PROGRESS, DONE)
- `LinkTargetType.java` — 연계 대상 유형 enum(INCIDENT, CHANGE)

## 하위 디렉토리
- `repository/` — 리포지토리 인터페이스
