# CLAUDE.md

compliance 도메인의 엔티티·enum·리포지토리 계약.

## 파일
- `ComplianceRequirement.java` — 컴플라이언스 요구사항 엔티티(requirementKey COMP-YYYY-####, name/basis 필수, scope 선택, ownerId nullable)
- `CorrectiveAction.java` — 시정조치 엔티티(requirementId 소속, DETECTED 기본 상태)
- `CorrectiveActionStatus.java` — 시정조치 상태 enum(DETECTED, IN_PROGRESS, RESOLVED)
- `ComplianceStatus.java` — 준수 상태 enum(COMPLIANT, NON_COMPLIANT). 저장 컬럼이 아닌 계산값(시정조치 미해결 존재 여부로 조회 시점 산정)

## 하위 디렉토리
- `repository/` — 리포지토리 인터페이스
