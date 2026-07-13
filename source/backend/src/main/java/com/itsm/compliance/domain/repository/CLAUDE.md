# CLAUDE.md

compliance 도메인 리포지토리 인터페이스.

## 파일
- `ComplianceRequirementRepository.java` — 요구사항 저장·조회·검색(complianceStatus 계산값 EXISTS 서브쿼리 필터·ownerAssigned·keyword)
- `CorrectiveActionRepository.java` — 시정조치 저장·조회(요구사항별 목록, 배치 조회로 N+1 방지)
