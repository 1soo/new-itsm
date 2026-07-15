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
- `RoleOptionResponse.java` — 역할 옵션 응답(id/roleCode/name만, API-AUTH-030 비관리자 공개용, 2026-07-15)
- `AuditLogResponse.java` — 감사 로그 응답(eventType, actor, target, result, occurredAt)
- `ScreenResponse.java` — 메뉴(화면) 응답(사이드바 표시 컬럼 + screenNameEn/groupLabelEn + 매핑된 roles 포함, 2026-07-13 i18n 유지보수)
- `CreateScreenRequest.java` — 메뉴 생성 요청(screenCode/screenName/screenNameEn/path/domain 필수, groupCode 지정 시 groupLabelEn 필수(서비스에서 검증), 나머지 선택+기본값)
- `UpdateScreenRequest.java` — 메뉴 수정 요청(모두 선택, screenCode·domain 제외, screenNameEn/groupLabelEn 포함)
- `AssignScreenRoleRequest.java` — 메뉴 역할 매핑 부여 요청(roleId)
- `ScreenRolesResponse.java` — 메뉴 역할 매핑 목록 응답(screenId, roles)
- `ScreenDeletedResponse.java` — 메뉴 삭제 응답(id, deleted)
- `MyMenuResponse.java` — 내 메뉴 조회 응답(groups)
- `MenuGroupResponse.java` — 메뉴 그룹(groupCode/groupLabel/groupLabelEn, items)
- `MenuItemResponse.java` — 메뉴 항목(screenCode/screenName/screenNameEn/path/iconName)
- `ApprovalDomainResponse.java` — 승인 프로세스 대상 도메인 응답(domain, label, hasRequestSubtype)
- `ApprovalProcessStepInput.java` — 승인 프로세스 차수 입력(decisionMode, roleIds) — 생성/수정 요청이 공유
- `CreateApprovalProcessRequest.java` — 승인 프로세스 생성 요청(domain 선택(null=전체 도메인)·requestSubtypeKey(domain null이면 반드시 null)·requesterRoleIds·steps, 2026-07-15 domain 필수→선택 변경)
- `UpdateApprovalProcessRequest.java` — 승인 프로세스 수정 요청(전달된 필드만 갱신, requesterRoleIds·steps는 전달 시 전체 교체. domain·requestSubtypeKey는 식별 스코프라 수정 대상 제외)
- `ApprovalProcessSummaryResponse.java` — 승인 프로세스 목록 항목(domain nullable, 우선순위·요청자 역할·차수 수 포함)
- `ApprovalProcessDetailResponse.java` — 승인 프로세스 상세(domain nullable, 요청자 역할 id·차수별 역할 id)
- `ApprovalProcessDeletedResponse.java` — 승인 프로세스 삭제 응답(id, deleted)
