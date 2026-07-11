# CLAUDE.md

auth 애플리케이션 서비스의 단위 테스트(JUnit). 예외 케이스 포함.

## 파일
- `AuthServiceTest.java` — 로그인/토큰/비밀번호 변경 로직 테스트
- `UserAdminServiceTest.java` — 계정 관리 로직 테스트
- `RoleServiceTest.java` — 역할 관리 로직 테스트
- `ScreenAdminServiceTest.java` — 메뉴(화면) CRUD·역할 매핑 부여/회수 로직 및 예외 테스트(SCREEN_NOT_FOUND/SCREEN_CODE_DUPLICATE/PATH_DUPLICATE/ROLE_NOT_FOUND/SCREEN_ROLE_MAPPING_DUPLICATE)
- `MyMenuServiceTest.java` — 내 메뉴 조회 로직 테스트(무매핑 화면 전체 공개, 역할 매핑 필터링, SYSTEM_ADMIN 우회, 그룹 순서)
- `PasswordPolicyTest.java` — 비밀번호 정책 검증 테스트
- `AccessTokenSessionCheckerImplTest.java` — Access Token 세션 검증 테스트
