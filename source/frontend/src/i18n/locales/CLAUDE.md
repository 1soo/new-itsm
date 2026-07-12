# CLAUDE.md

언어별(ko/en) 네임스페이스 번역 리소스(common.md 6.3절). 네임스페이스 = `common` + 11개 업무 도메인 slug(`tech.md` 5절과 동일). 각 도메인 화면은 `useTranslation(["{domain-ns}", "common"])`로 필요한 네임스페이스만 로드한다.

키 컨벤션: `{section}.{itemKey}` 계층 구조(예: `header.dismissAll`, `notification.domainLabel.CHANGE`).

## 하위 디렉토리
- `ko/` — 한국어 리소스. `common.json`은 채워짐(common phase 완료), 나머지 11개 도메인 JSON은 각 도메인 phase 담당 개발자가 채울 때까지 `{}` 스캐폴딩.
- `en/` — 영어 리소스. 구조는 `ko/`와 동일(파일명·키 1:1 대응).
