# CLAUDE.md

관리자(계정/역할/감사 로그/메뉴) 기능. SYSTEM_ADMIN 전용 화면과 API·타입을 제공한다(미보유 시 403). API 계약은 auth.md(API-AUTH-006~022) 기준.

## 파일
- `api.ts` — admin API 호출(`adminApi`: 계정 목록/생성/상세/수정/상태변경, 역할 부여·회수·목록·생성, 감사 로그 조회, 메뉴(화면) 목록/생성/수정/삭제·역할 매핑 부여/회수). 빈 쿼리 파라미터 제거 헬퍼 포함.
- `types.ts` — admin 도메인 타입(`UserSummary`/`UserDetail`/`Role`/`AuditLog`/`Screen`(메뉴), 쿼리·생성 요청, 공통 `PageResponse<T>` 등).
- `UserListPage.tsx` — 계정 목록(SCR-ADMIN-001).
- `UserCreatePage.tsx` — 계정 생성(SCR-ADMIN-002).
- `UserDetailPage.tsx` — 계정 상세·수정·역할 관리(SCR-ADMIN-003).
- `RoleManagementPage.tsx` — 역할 관리(SCR-ADMIN-004).
- `AuditLogPage.tsx` — 감사 로그 조회(SCR-ADMIN-005).
- `MenuManagementPage.tsx` — 메뉴 관리(SCR-ADMIN-006, Role-Menu 동적 매핑). 메뉴(화면) CRUD 모달 + 역할 매핑 우측 슬라이드 패널(체크박스 토글마다 즉시 반영). 아이콘 미리보기는 `lib/icon.ts`의 `resolveIcon` 재사용.
