# CLAUDE.md

infra 도메인 통합 테스트(Testcontainers PostgreSQL).

## 파일
- `InfraMonitoringIntegrationTest.java` — 지표 등록 시 전역 임계치 초과/미초과/미설정 알림 생성 여부(신규 임계치 설정 반영 포함)→알림 확인처리(idempotent, 존재하지 않는 알림 404)→존재하지 않는 자산에 지표 등록 404→자산 가동률 현황(목표 있음/없음 met 계산, 조회 시점 평균)→존재하지 않는 자산 가동률 목표설정/조회 404→용량 계획 활용률·리포팅 집계(자산 필터, DB 직접 조회로 기대값 검증)→INFRA_OPERATOR 아닌 역할 403 테스트
