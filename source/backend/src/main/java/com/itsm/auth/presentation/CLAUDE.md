# CLAUDE.md

auth 도메인 REST 컨트롤러(프레젠테이션 계층).

## 파일
- `AuthController.java` — `/api/v1/auth` 로그인/로그아웃/토큰 재발급/내 정보/비밀번호 변경. 유지보수(2026-07-12, 토큰 저장 Client Memory 전환): 로그인 성공 시 Refresh Token(httpOnly, `SameSite=Strict`) 쿠키와 함께 읽기 가능한 `XSRF-TOKEN` 쿠키(non-HttpOnly, `SameSite=Strict`)를 더블서밋 방식으로 발급. `/auth/refresh`는 `X-CSRF-Token` 헤더와 `XSRF-TOKEN` 쿠키 값을 비교해 불일치/누락 시 `BusinessException(CSRF_TOKEN_MISMATCH)`(403)를 Refresh Token 검증보다 먼저 던진다(우회 방지 위해 body-fallback 경로에도 동일 적용). 로그아웃 시 두 쿠키 모두 즉시 만료 처리
- `AdminUserController.java` — `/api/v1/admin/users` 관리자 계정 관리(CRUD·상태·역할)
- `AdminRoleController.java` — `/api/v1/admin/roles` 관리자 역할 관리
- `AdminAuditLogController.java` — `/api/v1/admin/audit-logs` 감사 로그 조회
- `AdminScreenController.java` — `/api/v1/admin/screens` 메뉴(화면) CRUD·역할 매핑 부여/회수(Role-Menu 동적 매핑)
- `MenuController.java` — `/api/v1/menus/mine` 내 메뉴 조회(인증만 필요, `/admin/**` 매처 밖이라 별도 컨트롤러)
- `RoleController.java` — `/api/v1/roles` 역할 목록 조회(API-AUTH-030, 인증만 필요, `/admin/**` 매처 밖이라 별도 컨트롤러, SRM 카탈로그 담당자 역할 select 등이 소비, 2026-07-15)
- `AdminApprovalProcessController.java` — `/api/v1/admin/approval-processes` 승인 프로세스 정의 CRUD(API-AUTH-023~029, 2026-07-11 승인 프로세스 커스텀 기능) + 도메인별 적용 상태 후보 조회(API-AUTH-031 `GET /domains/{domain}/states`, 2026-07-22 신규)
