# CLAUDE.md

인증/계정/권한(auth) 도메인 애플리케이션 서비스 계층. 유스케이스 조율·트랜잭션 처리.

## 파일
- `AuthService.java` — 로그인/로그아웃/토큰 재발급/내 정보/비밀번호 변경 유스케이스
- `UserAdminService.java` — 관리자용 계정 CRUD·상태 변경·역할 부여 유스케이스
- `RoleService.java` — 역할 생성·조회 유스케이스. `listOptions()`는 비관리자 공개용 경량 조회(API-AUTH-030, userCount 등 관리자 전용 필드 제외, 2026-07-15)
- `ScreenAdminService.java` — 메뉴(화면) CRUD·역할 매핑 부여/회수 유스케이스(Role-Menu 동적 매핑, SYSTEM_ADMIN 전용). 모든 변경은 ROLE_CHANGE 감사 로그 기록. 생성 시 groupCode 지정하고 groupLabelEn 누락하면 VALIDATION_ERROR(400)(2026-07-13 i18n 유지보수)
- `MyMenuService.java` — 로그인 사용자 역할에 매핑된 사이드바 메뉴 조회 유스케이스(API-AUTH-022). 매핑 없는 화면은 전체 공개. 응답에 screenNameEn/groupLabelEn 포함(FE가 i18n.language로 선택, 2026-07-13)
- `AuditLogService.java` — 감사 로그 기록·조회. findByEventTypes()로 다중 이벤트타입 조회 지원(컴플라이언스 전용 조회 등, 기존 단일 EventType search() 시그니처 유지)
- `ApprovalProcessAdminService.java` — 승인 프로세스 정의 CRUD 유스케이스(API-AUTH-023~029, SYSTEM_ADMIN 전용, 2026-07-11 승인 프로세스 커스텀 기능, 2026-07-15 범위 우선순위 3축 재설계). 대상 도메인 9개 하드코딩(domain은 선택, null=전체 도메인), 요청유형 후보는 CHANGE 고정 코드+`common.approval.application.ApprovalRequestSubtypeProvider` 구현 빈(SERVICE_REQUEST 등) 위임. priorityTier는 domain/requestSubtypeKey/requesterRoleIds 3축 독립 스코프 기반 산정식(축개수×10+역할4+요청유형2+도메인1, 실제값 0/11/14/23/25/37)이며 domain이 null이면 requestSubtypeKey도 null 강제. tier=0/11/23은 존재 검증, tier=14/25/37은 동일 매칭 스코프 내 역할 조합 교집합 검증(409). 실제 게이트 체크·인스턴스 조회/결정은 `common.approval` 담당(이 서비스는 규칙 정의만)
- `PasswordPolicy.java` — 비밀번호 정책 검증 유틸(final)
- `AccessTokenSessionCheckerImpl.java` — Access Token 세션(JTI) 유효성 검증 구현체

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
