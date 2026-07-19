# CLAUDE.md

esm 도메인 엔티티·enum·리포지토리 계약.

## 파일
- `EsmCatalogItem.java` — 부서 카탈로그 항목 엔티티(담당 부서·체크리스트 템플릿 유형). `formSchema`(JSONB, SRM과 완전히 동일한 자체 8×n 그리드 스키마 `{components,labels}`)에 동적 양식을 저장(2026-07-19 유지보수 요청, 레거시 EAV `EsmCatalogFormField` 폐기)
- `EsmChecklistTemplateTask.java` — 카탈로그 항목의 체크리스트 하위 작업 템플릿 엔티티
- `EsmRequest.java` — 부서 요청 티켓 엔티티(부서·대상자명·연계 체크리스트). `formValues`(JSONB, 컴포넌트 key 기준 key-value 맵)에 양식 제출 데이터를 저장(2026-07-19 유지보수 요청, 레거시 EAV `EsmRequestFormValue` 폐기)
- `EsmHrCase.java` — HR 케이스 엔티티(민감정보, 애플리케이션 레벨 HR_CASE_MANAGER 전용 강제)
- `EsmChecklist.java` — 온보딩/오프보딩 체크리스트 엔티티
- `EsmChecklistTask.java` — 체크리스트 하위 작업(실행 인스턴스) 엔티티(자산 회수 시 relatedAssetId)
- `ChecklistTemplateType.java` — 체크리스트 템플릿 유형 enum(NONE, ONBOARDING, OFFBOARDING)
- `EsmRequestStatus.java` — 부서 요청 상태 enum(SUBMITTED, IN_PROGRESS, COMPLETED, REJECTED)
- `HrCaseStatus.java` — HR 케이스 상태 enum(INTAKE, DOCUMENTATION, INVESTIGATION, RESOLUTION, 순차 전이만 허용)
- `ChecklistStatus.java` — 체크리스트 상태 enum(IN_PROGRESS, COMPLETED)
- `ChecklistTaskStatus.java` — 하위 작업 상태 enum(PENDING, DONE)

## 하위 디렉토리
- `repository/` — 리포지토리 인터페이스
