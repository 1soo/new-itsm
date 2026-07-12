# CLAUDE.md

한국어 번역 리소스(네임스페이스별 JSON). `common.json`은 common phase에서 채워졌고, 나머지는 각 도메인 phase 담당 개발자가 채운다.

## 파일
- `common.json` — 레이아웃(헤더)·언어 선택·확인 다이얼로그 기본 라벨·알림 조립 라벨(도메인 라벨·상대 시간) 등 common 네임스페이스 전체.
- `auth.json` — auth phase에서 채워짐. 로그인(SCR-AUTH-001)·프로필(SCR-AUTH-002)·비밀번호 변경(SCR-AUTH-003)과 관리자 8개 화면(SCR-ADMIN-001~008) 전체 텍스트.
- `service-request.json` — service-request phase에서 채워짐. 포털·요청 제출·목록·큐·상세·카탈로그 관리·지표(SCR-SRM-001~005,007~008) 전체 텍스트 + 상태/SLA 라벨(`status.*`/`sla.*`).
- `incident.json` — incident phase에서 채워짐. 목록·등록·상세·포스트모템·지표(SCR-INC-001~005) 전체 텍스트 + 상태 라벨(`status.*`).
- `problem.json` — problem phase에서 채워짐. 목록·등록·상세·KEDB 검색(SCR-PRB-001~004) 전체 텍스트 + 상태/출처/영향도-긴급도/조치 상태 라벨(`status.*`/`origin.*`/`level.*`/`actionStatus.*`).
- `change.json` / `knowledge.json` / `asset.json` / `esm.json` / `vulnerability.json` / `compliance.json` / `infra-monitoring.json` — 각 업무 도메인 네임스페이스. 담당 개발자가 채우기 전까지 `{}`.
