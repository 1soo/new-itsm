# CLAUDE.md

ITSM(IT 서비스 관리) 프론트엔드. React 19(CSR) + Vite + TypeScript + Redux Toolkit + React Router + shadcn/ui(Tailwind v4) 기반. 인증(RBAC), 서비스 요청(SRM), 인시던트(INC), 문제(PRB) 도메인 화면 제공. 백엔드(`source/backend`)와 apiClient(`/api/v1`)로 통신. dev 서버는 `/api`를 백엔드(8080)로 프록시.

## 파일
- `package.json` — 의존성·스크립트(`dev`/`build`/`preview`/`lint`).
- `package-lock.json` — npm 의존성 잠금 파일.
- `vite.config.ts` — Vite 설정. React·Tailwind 플러그인, `@` 별칭, dev 프록시(`/api` → localhost:8080).
- `components.json` — shadcn/ui 설정(new-york 스타일, 별칭, lucide 아이콘).
- `index.html` — HTML 진입 문서(`#root`, main.tsx 로드).
- `.env.example` — 환경변수 예시(`VITE_API_BASE_URL`).
- `.gitignore` — Git 제외 목록(node_modules·빌드 산출물 등).
- `tsconfig.json` — 루트 TS 설정. app/node 프로젝트 참조 + `@/*` 경로 별칭.
- `tsconfig.app.json` — 앱 소스(src) 컴파일러 설정.
- `tsconfig.node.json` — Vite 등 노드 툴링용 컴파일러 설정.
- `tsconfig.app.tsbuildinfo` / `tsconfig.node.tsbuildinfo` — TS 증분 빌드 캐시(빌드 산출물).

## 하위 디렉토리
- `src/` — 애플리케이션 소스(진입점·컴포넌트·기능·라우팅·상태·유틸).
