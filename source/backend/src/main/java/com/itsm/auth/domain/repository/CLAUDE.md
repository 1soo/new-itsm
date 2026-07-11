# CLAUDE.md

auth 도메인의 리포지토리 인터페이스(영속성 기술 비의존, 구현은 infrastructure에 위임).

## 파일
- `AppUserRepository.java` — 계정(AppUser) 저장·조회
- `RoleRepository.java` — 역할(Role) 저장·조회
- `UserRoleRepository.java` — 사용자-역할 매핑(UserRole) 저장·조회
- `ScreenRepository.java` — 화면(Screen) 저장·조회(screenCode/path 중복 검사, groupCode/domain 필터 페이지네이션, nav_visible 목록)
- `ScreenRoleRepository.java` — 역할-화면 매핑(ScreenRole) 저장·조회(screenId/roleId 기준)
- `RefreshTokenRepository.java` — Refresh Token 저장·조회·무효화
- `AuditLogRepository.java` — 감사 로그 저장·조회(단일 EventType search() 외 다중 이벤트타입 조회 findByEventTypeInAndOccurredAtBetweenOrderByOccurredAtDesc 포함, 컴플라이언스 전용 조회용)
