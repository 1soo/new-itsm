# CLAUDE.md

문제(problem) 도메인의 애플리케이션 서비스 계층.

## 파일
- `ProblemService.java` — 문제 생성·상태전이·RCA·워크어라운드·후속조치·연계·종료 및 기지 오류(Known Error) 유스케이스
- `ProblemStateMachine.java` — 상태 전이 규칙(DETECTION→…→RESOLVED_CLOSED 6단계 순차, package-private)

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
