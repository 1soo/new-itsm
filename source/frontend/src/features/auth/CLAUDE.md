# CLAUDE.md

인증/계정/역할(RBAC) 기능. 로그인·내 프로필·비밀번호 변경 화면·API·타입, 앱 전역 역할 상수·RBAC 헬퍼 제공. 전역 인증 상태는 `store/authSlice.ts`가 관리.

## 파일
- `api.ts` — auth API 호출(`authApi`: login/logout/me/changePassword/getMyMenu). 공통 apiClient 경유.
- `types.ts` — auth 도메인 타입(`AuthUser`/`LoginRequest`/`LoginResponse`/`MeResponse`/`ChangePasswordRequest`/`MyMenuResponse`(사이드바 동적 메뉴, API-AUTH-022) 등).
- `roles.ts` — 역할 상수(`ROLE_*`)와 RBAC 헬퍼(`isSystemAdmin`/`hasAnyRole`/`roleHome`). 라우트 가드·메뉴 노출·로그인 후 홈 결정에 사용.
- `password.ts` — 비밀번호 정책 사전검증(`validatePasswordPolicy`, 최소 8자+영문·숫자). 계정 생성·변경 공통.
- `LoginPage.tsx` — 로그인 화면(SCR-AUTH-001). 인증 후 역할 기본 홈으로 이동.
- `ProfilePage.tsx` — 내 프로필 화면(SCR-AUTH-002).
- `ChangePasswordPage.tsx` — 비밀번호 변경 화면(SCR-AUTH-003).
