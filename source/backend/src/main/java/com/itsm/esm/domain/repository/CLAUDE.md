# CLAUDE.md

esm 도메인 리포지토리 인터페이스.

## 파일
- `EsmCatalogItemRepository.java` — 카탈로그 항목(EsmCatalogItem) 저장·조회·검색(department/keyword)
- `EsmChecklistTemplateTaskRepository.java` — 체크리스트 하위 작업 템플릿 저장·조회·삭제
- `EsmRequestRepository.java` — 부서 요청(EsmRequest) 저장·조회·검색(requesterId/department/status/기간)
- `EsmHrCaseRepository.java` — HR 케이스(EsmHrCase) 저장·조회·검색(status)
- `EsmChecklistRepository.java` — 체크리스트(EsmChecklist) 저장·조회
- `EsmChecklistTaskRepository.java` — 체크리스트 하위 작업 저장·조회·검색(department/status), 완료 개수 집계

> `EsmCatalogFormFieldRepository`/`EsmRequestFormValueRepository`(레거시 EAV)는 2026-07-19 유지보수 요청으로 폐기됨 — `EsmCatalogItem.formSchema`/`EsmRequest.formValues`(JSONB)로 대체.
