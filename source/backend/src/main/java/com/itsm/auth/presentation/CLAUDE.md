# CLAUDE.md

auth 도메인의 REST 컨트롤러(프레젠테이션 계층).

## 파일
- `AuthController.java` — `/api/v1/auth` 로그인/로그아웃/토큰 재발급/내 정보/비밀번호 변경
- `AdminUserController.java` — `/api/v1/admin/users` 관리자 계정 관리(CRUD·상태·역할)
- `AdminRoleController.java` — `/api/v1/admin/roles` 관리자 역할 관리
- `AdminAuditLogController.java` — `/api/v1/admin/audit-logs` 감사 로그 조회
