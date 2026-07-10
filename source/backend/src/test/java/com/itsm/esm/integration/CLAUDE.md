# CLAUDE.md

esm 도메인 통합 테스트(Testcontainers PostgreSQL).

## 파일
- `EsmIntegrationTest.java` — 카탈로그(체크리스트 템플릿)→온보딩/오프보딩 요청 제출(체크리스트·자산회수 자동생성)→부서 상태전이(department 불일치 403)→하위 작업 완료(전체완료 자동갱신)→HR 케이스(HR_CASE_MANAGER 전용, 순차 전이)→지표 집계 end-to-end 통합 테스트
