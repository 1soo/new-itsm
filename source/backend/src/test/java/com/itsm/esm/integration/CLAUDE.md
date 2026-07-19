# CLAUDE.md

esm 도메인 통합 테스트(Testcontainers PostgreSQL).

## 파일
- `EsmIntegrationTest.java` — 카탈로그(체크리스트 템플릿)→온보딩/오프보딩 요청 제출(체크리스트·자산회수 자동생성)→부서 상태전이(department 불일치 403)→하위 작업 완료(전체완료 자동갱신)→HR 케이스(HR_CASE_MANAGER 전용, 순차 전이)→지표 집계 end-to-end 통합 테스트. `updateCatalogItemReplacesFormSchema`: 카탈로그 `formSchema`(자체 8×n 그리드 스키마) 생성·수정 및 필수값 미충족 요청 제출 시 `FormSubmissionValidator` 400 재검증(2026-07-19 유지보수 요청, 레거시 EAV 회귀 테스트 대체). Testcontainers 스키마 마운트에 `40_esm_form_schema_jsonb.sql` 포함 필요
