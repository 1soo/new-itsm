# CLAUDE.md

인증/계정/권한(auth) 도메인. JWT 기반 인증, 계정·역할(RBAC) 관리, 감사 로그. DDD 4계층(application/domain/infrastructure/presentation) 구조.

## 하위 디렉토리
- `application/` — 유스케이스 서비스와 DTO
- `domain/` — 엔티티·enum·리포지토리 계약
- `infrastructure/` — 리포지토리 JPA 구현
- `presentation/` — REST 컨트롤러
