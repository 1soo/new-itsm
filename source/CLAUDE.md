# CLAUDE.md

ITSM(IT 서비스 관리) 시스템의 전체 소스 루트. 프론트엔드·백엔드·DB 산출물을 각각의 하위 디렉토리에 분리해서 관리한다.

## 파일
- `.gitkeep` — 빈 디렉토리 버전관리용 placeholder.

## 하위 디렉토리
- `backend/` — Spring Boot 기반 REST API 서버(Java, DDD 4계층).
- `frontend/` — React(CSR) + Vite 기반 웹 클라이언트.
- `db/` — 로컬 DB 컨테이너 구성과 스키마 DDL·시드 데이터.
