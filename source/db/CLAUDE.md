# CLAUDE.md

ITSM 시스템 DB 산출물 루트. 로컬 DB 컨테이너 구성과 도메인별 스키마 DDL·시드 데이터 관리.

## 하위 디렉토리
- `docker/` — 로컬 개발용 PostgreSQL 컨테이너 구성(docker-compose, 접속 정보). 최초 기동 시 `sql/`을 초기화 스크립트로 마운트해 자동 실행.
- `sql/` — 도메인별(auth·common·service-request·incident·problem) 스키마 DDL과 시드 데이터. 파일명 번호 순 실행.
