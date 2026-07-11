# CLAUDE.md

auth 도메인 리포지토리 인터페이스의 Spring Data JPA 구현체. 각 인터페이스는 `JpaRepository`와 도메인 리포지토리 계약을 함께 확장한다.

## 파일
- `AppUserJpaRepository.java` — AppUserRepository 구현
- `RoleJpaRepository.java` — RoleRepository 구현
- `UserRoleJpaRepository.java` — UserRoleRepository 구현
- `ScreenJpaRepository.java` — ScreenRepository 구현(groupCode/domain 필터 검색, nav_visible 목록 조회 JPQL 포함)
- `ScreenRoleJpaRepository.java` — ScreenRoleRepository 구현
- `RefreshTokenJpaRepository.java` — RefreshTokenRepository 구현
- `AuditLogJpaRepository.java` — AuditLogRepository 구현(다중 이벤트타입 조회 포함)
