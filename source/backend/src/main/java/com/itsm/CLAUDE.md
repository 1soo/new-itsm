# CLAUDE.md

ITSM 백엔드의 루트 패키지(`com.itsm`). Spring Boot 진입점과 도메인 패키지들을 담는다. 각 도메인은 DDD 4계층(application/domain/infrastructure/presentation)으로 구성된다.

## 파일
- `ItsmApplication.java` — Spring Boot 애플리케이션 진입점(@SpringBootApplication)

## 하위 디렉토리
- `common/` — 전 도메인 공통 모듈(설정·예외·보안·티켓 공통)
- `auth/` — 인증/계정/권한(RBAC) 도메인
- `incident/` — 인시던트 관리 도메인
- `problem/` — 문제 관리 도메인
- `srm/` — 서비스 요청 관리 도메인
- `change/` — 변경 관리 도메인
- `knowledge/` — 지식 관리 도메인
- `asset/` — IT 자산 관리(ITAM/CMDB) 도메인
- `search/` — 통합 검색(지식+티켓 교차 도메인) 도메인
- `esm/` — 엔터프라이즈 서비스 관리(부서 요청·HR 케이스·온보딩/오프보딩 체크리스트) 도메인
- `vulnerability/` — 취약점 관리 도메인
- `compliance/` — 컴플라이언스 관리 도메인
