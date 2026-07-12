# CLAUDE.md

한국어 번역 리소스(네임스페이스별 JSON). `common.json`은 common phase에서 채워졌고, 나머지는 각 도메인 phase 담당 개발자가 채운다.

## 파일
- `common.json` — 레이아웃(헤더)·언어 선택·확인 다이얼로그 기본 라벨·알림 조립 라벨(도메인 라벨·상대 시간) 등 common 네임스페이스 전체.
- `auth.json` — auth phase에서 채워짐. 로그인(SCR-AUTH-001)·프로필(SCR-AUTH-002)·비밀번호 변경(SCR-AUTH-003)과 관리자 8개 화면(SCR-ADMIN-001~008) 전체 텍스트.
- `service-request.json` — service-request phase에서 채워짐. 포털·요청 제출·목록·큐·상세·카탈로그 관리·지표(SCR-SRM-001~005,007~008) 전체 텍스트 + 상태/SLA 라벨(`status.*`/`sla.*`).
- `incident.json` — incident phase에서 채워짐. 목록·등록·상세·포스트모템·지표(SCR-INC-001~005) 전체 텍스트 + 상태 라벨(`status.*`).
- `problem.json` — problem phase에서 채워짐. 목록·등록·상세·KEDB 검색(SCR-PRB-001~004) 전체 텍스트 + 상태/출처/영향도-긴급도/조치 상태 라벨(`status.*`/`origin.*`/`level.*`/`actionStatus.*`).
- `change.json` — change phase에서 채워짐. 목록·RFC생성·상세·일정 캘린더·지표(SCR-CHG-001~003,005~006) 전체 텍스트 + 상태/유형/위험도 라벨(`status.*`/`type.*`/`risk.*`).
- `knowledge.json` — knowledge phase에서 채워짐. 검색/목록·열람·작성/편집·지표(SCR-KM-001~003,005) 전체 텍스트 + 상태 라벨(`status.*`).
- `asset.json` — asset phase에서 채워짐. 목록·등록/수정·상세·CI/CMDB 관계·지표(SCR-ITAM-001~005) 전체 텍스트 + 유형/상태/만료/연계 티켓 유형/CI 관계 유형 라벨(`type.*`/`status.*`/`expiry.*`/`ticketType.*`/`relationType.*`).
- `esm.json` — esm phase에서 채워짐. 부서 포털/요청 제출·목록·큐·상세/카탈로그 관리/HR 케이스 목록·상세/체크리스트 상세·내 하위 작업/지표(SCR-ESM-001~011) 전체 텍스트 + 부서/요청 상태/HR 케이스 상태/체크리스트 상태·유형/하위 작업 상태 라벨(`department.*`/`requestStatus.*`/`hrCaseStatus.*`/`checklistStatus.*`/`checklistType.*`/`checklistTemplateType.*`/`checklistTaskStatus.*`).
- `vulnerability.json` — vulnerability phase에서 채워짐. 목록·등록·상세(라이프사이클·리스크·담당자·개선·검증·자산/CI 연계)·지표(SCR-VULN-001~004) 전체 텍스트 + 상태/심각도/악용가능성/조치유형/검증결과 라벨(`status.*`/`severity.*`/`exploitability.*`/`actionType.*`/`verificationResult.*`).
- `compliance.json` — compliance phase에서 채워짐. 목록·등록·상세(책임자·시정조치·변경 연계·감사 로그)·준수 현황(SCR-COMP-001~004) 전체 텍스트 + 준수 상태/시정조치 상태/감사 로그 이벤트 유형 라벨(`complianceStatus.*`/`actionStatus.*`/`auditEventType.*`).
- `infra-monitoring.json` — infra-monitoring phase에서 채워짐(마지막 도메인). 지표 등록/대시보드/임계치 설정·알림/용량 계획/리포팅(SCR-IOM-001~005) 전체 텍스트 + 지표 항목/임계치 초과 유형 라벨(`metricType.*`/`thresholdType.*`).
