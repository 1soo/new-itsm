# CLAUDE.md

esm 도메인의 애플리케이션 서비스 계층.

## 파일
- `EsmCatalogService.java` — 부서 카탈로그 항목 CRUD·동적 양식·체크리스트 템플릿 관리(PROCESS_OWNER 전용 생성/수정, IT 부서 거부)
- `EsmRequestService.java` — 부서 요청 제출(체크리스트 자동생성·오프보딩 자산회수 하위작업 자동추가)·조회·상태전이·코멘트 유스케이스(DEPT_COORDINATOR 소속 부서 검증)
- `EsmHrCaseService.java` — HR 케이스 접수·조회·상태전이(접수→기록→조사→해결 순차) 유스케이스(HR_CASE_MANAGER 전용)
- `EsmChecklistService.java` — 체크리스트 상세 조회·내 하위 작업 목록·하위 작업 완료 처리(전체 완료 시 체크리스트 자동완료) 유스케이스
- `EsmMetricsService.java` — ESM 지표(요청 건수·평균 처리시간·온보딩/오프보딩 완료율) 집계

## 하위 디렉토리
- `dto/` — 요청·응답 DTO(record)
