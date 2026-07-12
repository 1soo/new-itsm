# CLAUDE.md

영어 번역 리소스(네임스페이스별 JSON). 구조·파일명·키는 `ko/`와 1:1 대응한다. `common.json`은 common phase에서 채워졌고, 나머지는 각 도메인 phase 담당 개발자가 채운다.

## 파일
- `common.json` — 레이아웃(헤더)·언어 선택·확인 다이얼로그 기본 라벨·알림 조립 라벨(도메인 라벨·상대 시간) 등 common 네임스페이스 전체.
- `auth.json` / `service-request.json` / `incident.json` / `problem.json` / `change.json` / `knowledge.json` / `asset.json` / `esm.json` / `vulnerability.json` / `compliance.json` / `infra-monitoring.json` — 각 업무 도메인 네임스페이스. 담당 개발자가 채우기 전까지 `{}`.
