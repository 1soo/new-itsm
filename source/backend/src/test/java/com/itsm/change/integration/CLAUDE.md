# CLAUDE.md

change 도메인 통합 테스트(Testcontainers PostgreSQL).

## 파일
- `ChangeIntegrationTest.java` — 변경 요청(RFC) API end-to-end 통합 테스트(6단계 전이·구현결과·연계(인시던트/문제/자산, REQ-ITAM-006)·일정·템플릿·지표). Stage 2(2026-07-11)에서 IMPLEMENTATION 전이의 공용 승인 게이트 차단(409+approvalRequestId)·승인 후 재시도 통과·상세 조회 approval 필드 노출을 실 트랜잭션으로 검증하는 테스트 추가
