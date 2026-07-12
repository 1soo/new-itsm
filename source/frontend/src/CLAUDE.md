# CLAUDE.md

프론트엔드 소스 루트. 진입점(main → App → 라우터), 전역 상태(store), 라우팅(routes), 재사용 컴포넌트(components), 도메인 기능(features), 공통 유틸/HTTP(lib)로 구성된 React(CSR) + TypeScript 앱이다. import 경로는 `@/` 별칭(= `src/`)을 사용한다.

## 파일
- `main.tsx` — 진입점. Redux Provider로 스토어 주입, 라우터 App을 렌더. `@/i18n`을 사이드이펙트로 import해 i18next를 초기화한다(토스트/확인 다이얼로그는 SweetAlert2가 자체 DOM으로 렌더링해 별도 컨테이너 마운트 불필요).
- `App.tsx` — 앱 루트. `RouterProvider`로 라우터 마운트.
- `index.css` — 디자인 토큰(원자·시맨틱 색상)과 Tailwind/애니메이션 임포트, SweetAlert2 커스텀 클래스(`itsm-swal-*`, 2026-07-12 SweetAlert2 도입). 컴포넌트는 시맨틱 토큰만 사용.
- `vite-env.d.ts` — Vite 클라이언트 타입 참조.

## 하위 디렉토리
- `components/` — 재사용 컴포넌트(ui 프리미티브·common 공통·layout 셸).
- `features/` — 도메인별 기능(auth·admin·service-request·incident·problem).
- `routes/` — 라우팅·인증 가드·앱 레이아웃.
- `store/` — Redux Toolkit 전역 상태(인증).
- `lib/` — 공통 유틸(`cn`)과 공통 apiClient.
- `i18n/` — 다국어(i18next) 코어 초기화·언어 저장·네임스페이스별 번역 리소스(2026-07-12 다국어 지원, common.md 6절).
