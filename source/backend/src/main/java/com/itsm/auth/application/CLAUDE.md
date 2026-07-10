# CLAUDE.md

인증/계정/권한(auth) 도메인의 애플리케이션 서비스 계층. 유스케이스 조율·트랜잭션 처리.

## 파일
- `AuthService.java` — 로그인/로그아웃/토큰 재발급/내 정보/비밀번호 변경 유스케이스
- `UserAdminService.java` — 관리자용 계정 CRUD·상태 변경·역할 부여 유스케이스
- `RoleService.java` — 역할 생성·조회 유스케이스
- `AuditLogService.java` — 감사 로그 기록·조회. findByEventTypes()로 다중 이벤트타입 조회 지원(컴플라이언스 전용 조회 등, 기존 단일 EventType search() 시그니처는 유지)
- `PasswordPolicy.java` — 비밀번호 정책 검증 유틸(final)
- `AccessTokenSessionCheckerImpl.java` — Access Token 세션(JTI) 유효성 검증 구현체

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
