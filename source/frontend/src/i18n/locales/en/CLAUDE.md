# CLAUDE.md

영어 번역 리소스(네임스페이스별 JSON). 구조·파일명·키는 `ko/`와 1:1 대응. `common.json`은 common phase에서 채워짐, 나머지는 각 도메인 phase 담당 개발자가 채움.

## 파일
- `common.json` — 레이아웃(헤더)·언어 선택·확인 다이얼로그 기본 라벨·알림 조립 라벨(도메인 라벨·상대 시간) 등 common 네임스페이스 전체.
- `auth.json` — auth phase에서 채워짐. `ko/auth.json`과 키 구조 1:1 대응(로그인·프로필·비밀번호 변경·관리자 8개 화면).
- `service-request.json` — service-request phase에서 채워짐. `ko/service-request.json`과 키 구조 1:1 대응.
- `incident.json` — incident phase에서 채워짐. `ko/incident.json`과 키 구조 1:1 대응.
- `problem.json` — problem phase에서 채워짐. `ko/problem.json`과 키 구조 1:1 대응.
- `change.json` — change phase에서 채워짐. `ko/change.json`과 키 구조 1:1 대응.
- `knowledge.json` — knowledge phase에서 채워짐. `ko/knowledge.json`과 키 구조 1:1 대응.
- `asset.json` — asset phase에서 채워짐. `ko/asset.json`과 키 구조 1:1 대응.
- `esm.json` — esm phase에서 채워짐. `ko/esm.json`과 키 구조 1:1 대응.
- `vulnerability.json` — vulnerability phase에서 채워짐. `ko/vulnerability.json`과 키 구조 1:1 대응.
- `compliance.json` — compliance phase에서 채워짐. `ko/compliance.json`과 키 구조 1:1 대응.
- `infra-monitoring.json` — infra-monitoring phase에서 채워짐(마지막 도메인). `ko/infra-monitoring.json`과 키 구조 1:1 대응.
