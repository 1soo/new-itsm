# CLAUDE.md

auth 도메인의 리포지토리 인터페이스(영속성 기술 비의존, 구현은 infrastructure에 위임).

## 파일
- `AppUserRepository.java` — 계정(AppUser) 저장·조회
- `RoleRepository.java` — 역할(Role) 저장·조회
- `UserRoleRepository.java` — 사용자-역할 매핑(UserRole) 저장·조회
- `RefreshTokenRepository.java` — Refresh Token 저장·조회·무효화
- `AuditLogRepository.java` — 감사 로그 저장·조회
