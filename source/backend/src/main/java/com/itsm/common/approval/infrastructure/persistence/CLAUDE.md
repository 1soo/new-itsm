# CLAUDE.md

승인 엔진 리포지토리 인터페이스의 Spring Data JPA 구현체.

## 파일
- `ApprovalProcessJpaRepository.java` — ApprovalProcessRepository 구현(도메인별 조회·우선순위 중복 검증 JPQL 포함)
- `ApprovalProcessRequesterRoleJpaRepository.java` — ApprovalProcessRequesterRoleRepository 구현
- `ApprovalProcessStepJpaRepository.java` — ApprovalProcessStepRepository 구현
- `ApprovalProcessStepRoleJpaRepository.java` — ApprovalProcessStepRoleRepository 구현
- `ApprovalRequestJpaRepository.java` — ApprovalRequestRepository 구현(티켓 최신 인스턴스, 도메인 필터 대기함 후보 JPQL 포함)
- `ApprovalRequestStepJpaRepository.java` — ApprovalRequestStepRepository 구현
- `ApprovalRequestStepRoleJpaRepository.java` — ApprovalRequestStepRoleRepository 구현
- `ApprovalDecisionJpaRepository.java` — ApprovalDecisionRepository 구현
