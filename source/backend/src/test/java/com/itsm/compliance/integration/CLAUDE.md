# CLAUDE.md

compliance 도메인 통합 테스트(Testcontainers PostgreSQL).

## 파일
- `ComplianceIntegrationTest.java` — 요구사항 등록→준수상태 계산(시정조치 미해결/전부 RESOLVED)→시정조치 순차전이(순서 위반 400)→책임자지정→수정→변경 요청 연계(존재하지 않는 변경 400, change 상세 응답에도 COMPLIANCE_REQUIREMENT 링크 노출)→컴플라이언스 전용 감사로그 조회(이벤트타입 필터)→목록 필터→준수현황 집계 end-to-end 통합 테스트, 존재하지 않는 요구사항에 시정조치 등록 404 테스트, COMPLIANCE_OFFICER 아닌 역할 403 테스트
