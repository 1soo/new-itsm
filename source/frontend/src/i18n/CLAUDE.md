# CLAUDE.md

다국어(i18next) 코어 인프라(2026-07-12 유지보수 요청, common.md 6절 "i18n 아키텍처"). Provider 없이 default 인스턴스를 사용하며, `main.tsx`에서 `import "@/i18n"` 사이드이펙트로 초기화한다.

## 파일
- `index.ts` — `i18next.use(initReactI18next).init(...)` 호출. `resources`는 12개 네임스페이스(common + 11개 업무 도메인)의 ko/en JSON을 정적 import해 구성. `lng`는 `language.ts`의 저장값(기본 ko), `fallbackLng: "ko"`, `defaultNS: "common"`. `NAMESPACES` 상수를 export해 다른 곳에서 네임스페이스 목록이 필요할 때 재사용 가능.
- `language.ts` — 언어 선호도 `localStorage` 읽기/쓰기(`itsm-language` 키, 기본 "ko"). `theme-toggle.tsx`와 동일한 패턴. `readStoredLanguage`/`storeLanguage`/`Language` 타입 제공.

## 하위 디렉토리
- `locales/` — 언어(ko/en)별 네임스페이스 JSON 번역 리소스.
