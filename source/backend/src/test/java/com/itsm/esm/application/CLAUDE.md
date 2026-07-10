# CLAUDE.md

esm 애플리케이션 서비스의 단위 테스트.

## 파일
- `EsmCatalogServiceTest.java` — 카탈로그 CRUD 로직 및 PROCESS_OWNER 권한·IT 부서 거부 예외 테스트
- `EsmRequestServiceTest.java` — 요청 제출(체크리스트 자동생성·오프보딩 자산회수)·조회·상태전이·코멘트 로직 및 department 기반 접근제어 예외 테스트
- `EsmHrCaseServiceTest.java` — HR 케이스 접수·조회·상태전이(순차 검증) 로직 및 HR_CASE_MANAGER 전용 접근제어(SYSTEM_ADMIN 포함 차단) 예외 테스트
- `EsmChecklistServiceTest.java` — 체크리스트 상세 접근제어·내 하위 작업 목록·완료 처리(전체완료 자동갱신) 로직 및 예외 테스트
- `EsmMetricsServiceTest.java` — ESM 지표 집계(요청건수 department 필터·온보딩 완료율) 로직 테스트
