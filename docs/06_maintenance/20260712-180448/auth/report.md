# 유지보수 이력 — auth

> 유지보수 일시: 20260712-180448 · 도메인: auth

## 1. 요구사항

다국어 지원 기능이 추가되어야 하며, 한국어/영어 전환이 모든 화면에 적용되어야 한다.
(확정) 날짜/숫자 포맷은 현지화하지 않고 텍스트(라벨·메시지)만 번역한다.
auth/admin 도메인의 로그인·프로필·비밀번호 변경 및 관리자 화면 전체가 번역 대상이다.

## 2. 해결 방법

로그인/프로필/비밀번호 변경(SCR-AUTH-001~003) 화면과 관리자 8개 화면(SCR-ADMIN-001~008)의 하드코딩 텍스트를 번역 키로 전환했다.
`password.ts`의 비밀번호 정책 오류 메시지도 번역 키로 전환했다.
공용 i18n 인프라(`source/frontend/src/i18n/`, common 도메인에서 구축)를 재사용했다.

## 3. 변경 파일

- `source/frontend/src/features/auth/ProfilePage.tsx`
- `source/frontend/src/features/auth/ChangePasswordPage.tsx`
- `source/frontend/src/features/auth/password.ts`
- `source/frontend/src/features/admin/UserListPage.tsx`
- `source/frontend/src/features/admin/UserDetailPage.tsx`
- `source/frontend/src/features/admin/UserCreatePage.tsx`
- `source/frontend/src/features/admin/RoleManagementPage.tsx`
- `source/frontend/src/features/admin/MenuManagementPage.tsx`
- `source/frontend/src/features/admin/AuditLogPage.tsx`
- `source/frontend/src/features/admin/ApprovalProcessListPage.tsx`
- `source/frontend/src/features/admin/ApprovalProcessFormPage.tsx`

## 4. 테스트 결과

통합 테스트 17건 전부 PASS했다.
커밋 `78eeead`로 반영했다.
