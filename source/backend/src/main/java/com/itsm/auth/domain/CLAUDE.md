# CLAUDE.md

auth 도메인의 엔티티·값(enum)·리포지토리 계약. 계정/역할/토큰/감사의 핵심 모델.

## 파일
- `AppUser.java` — 계정 엔티티(BaseEntity 상속). email/비밀번호 해시/이름/상태/소속 부서(department, esm 도메인 담당 부서 판정용)
- `Role.java` — 역할 엔티티(roleCode, name). RBAC의 역할
- `UserRole.java` — 사용자-역할 매핑 엔티티(N:M)
- `RefreshToken.java` — Refresh Token 엔티티(append-only, BaseEntity 미상속)
- `AuditLog.java` — 감사 로그 엔티티(append-only)
- `UserStatus.java` — 계정 상태 enum(ACTIVE, INACTIVE)
- `AuditResult.java` — 감사 결과 enum(SUCCESS, FAILURE)
- `EventType.java` — 감사 이벤트 유형 enum(LOGIN, LOGOUT, REFRESH, USER_CHANGE, ROLE_CHANGE, COMPLIANCE_REQ_CREATE, COMPLIANCE_REQ_UPDATE, COMPLIANCE_ACTION_STATUS_CHANGE)
- `Department.java` — 부서 코드 enum(HR, LEGAL, FACILITIES, FINANCE, IT). esm 도메인에서 재사용

## 하위 디렉토리
- `repository/` — 리포지토리 인터페이스
