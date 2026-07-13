# CLAUDE.md

ITSM 로컬 개발용 PostgreSQL 컨테이너 구성 디렉토리.

## 파일
- `docker-compose.yml` — PostgreSQL 16(alpine) 컨테이너(`itsm-postgres`) 정의. TZ=Asia/Seoul, `itsm_pgdata` 볼륨으로 데이터 영속화, `../sql`을 `/docker-entrypoint-initdb.d`에 마운트해 최초 기동 시 파일명 알파벳 순으로 스키마·시드 자동 실행, `pg_isready` 헬스체크 포함.
- `.env.example` — 접속 정보 템플릿(DB/USER/PASSWORD/PORT). `.env`로 복사해 사용하며 backend `.env`와 값 일치시킴.
- `.env` — 실제 접속 정보(git 제외).
