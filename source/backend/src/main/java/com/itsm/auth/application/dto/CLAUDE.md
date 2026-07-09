# CLAUDE.md

인증/계정/권한(auth) 도메인 애플리케이션 계층의 요청·응답 DTO(record).

## 파일
- `LoginRequest.java` — 로그인 요청(email, password)
- `LoginResponse.java` — 로그인 응답(access/refresh 토큰, 만료시간, 사용자 정보 UserInfo 포함)
- `TokenResponse.java` — 토큰 재발급 응답(accessToken, tokenType, expiresIn)
- `RefreshRequest.java` — 토큰 재발급 요청(refreshToken)
- `LogoutRequest.java` — 로그아웃 요청(무효화 대상 refreshToken, 선택)
- `MessageResponse.java` — 단순 메시지 응답(message)
- `MeResponse.java` — 내 정보 조회 응답(id, email, name, status, roles)
- `PasswordChangeRequest.java` — 비밀번호 변경 요청(currentPassword, newPassword)
- `PageResponse.java` — 제네릭 페이지 응답(content, page, size, totalElements)
- `CreateUserRequest.java` — 계정 생성 요청(email, name, initialPassword, roleIds)
- `UpdateUserRequest.java` — 계정 수정 요청(name)
- `UserSummaryResponse.java` — 계정 목록 요약 응답
- `UserDetailResponse.java` — 계정 상세 응답(생성/수정 시각 포함)
- `StatusChangeRequest.java` — 계정 상태 변경 요청(UserStatus)
- `StatusChangeResponse.java` — 계정 상태 변경 응답(id, status)
- `AssignRoleRequest.java` — 사용자 역할 부여 요청(roleId)
- `UserRolesResponse.java` — 사용자 역할 목록 응답(userId, roles)
- `CreateRoleRequest.java` — 역할 생성 요청(roleCode, name, description)
- `RoleCreatedResponse.java` — 역할 생성 응답
- `RoleResponse.java` — 역할 조회 응답(userCount 포함)
- `AuditLogResponse.java` — 감사 로그 응답(eventType, actor, target, result, occurredAt)
