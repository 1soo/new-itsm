# CLAUDE.md

auth 도메인의 REST 컨트롤러(프레젠테이션 계층).

## 파일
- `AuthController.java` — `/api/v1/auth` 로그인/로그아웃/토큰 재발급/내 정보/비밀번호 변경
- `AdminUserController.java` — `/api/v1/admin/users` 관리자 계정 관리(CRUD·상태·역할)
- `AdminRoleController.java` — `/api/v1/admin/roles` 관리자 역할 관리
- `AdminAuditLogController.java` — `/api/v1/admin/audit-logs` 감사 로그 조회
- `AdminScreenController.java` — `/api/v1/admin/screens` 메뉴(화면) CRUD·역할 매핑 부여/회수(Role-Menu 동적 매핑)
- `MenuController.java` — `/api/v1/menus/mine` 내 메뉴 조회(인증만 필요, `/admin/**` 매처 밖이라 별도 컨트롤러)
- `AdminApprovalProcessController.java` — `/api/v1/admin/approval-processes` 승인 프로세스 정의 CRUD(API-AUTH-023~029, 2026-07-11 승인 프로세스 커스텀 기능)
