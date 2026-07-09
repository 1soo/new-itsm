# CLAUDE.md

도메인별 기능 모듈. 각 도메인은 화면(`*Page.tsx`)과 API 호출(`api.ts`)·타입(`types.ts`), 필요 시 상태/포맷 헬퍼(`status.ts`/`format.ts`)를 자체 포함한다. 공통 컴포넌트(`components/common`)와 apiClient(`lib`)를 조합해 구현하며, RBAC 라우팅은 `routes`가 담당한다.

## 하위 디렉토리
- `auth/` — 인증/계정/역할(RBAC). 로그인·프로필·비밀번호 변경, 역할 상수·RBAC 헬퍼.
- `admin/` — 관리자(계정/역할/감사 로그). SYSTEM_ADMIN 전용.
- `service-request/` — 서비스 요청(SRM). 포털·요청·큐·승인·카탈로그·지표.
- `incident/` — 인시던트(INC). 목록·등록·상세·포스트모템·지표.
- `problem/` — 문제(PRB). 목록·등록·상세·KEDB 검색.
- `change/` — 변경(CHG). 목록·RFC 생성·상세·CAB 승인 대기함·일정 캘린더·지표.
