# CLAUDE.md

메인 리소스 디렉토리. 애플리케이션 설정 파일.

## 파일
- `application.yml` — Spring Boot 설정. DataSource(PostgreSQL)·JPA(ddl-auto validate)·JWT(만료시간)·CORS·springdoc(Swagger-UI). 환경변수(`${VAR}`)는 `.env`에서 주입
