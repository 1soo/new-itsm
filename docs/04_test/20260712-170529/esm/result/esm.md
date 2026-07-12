# 통합 테스트 결과 — esm (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12)

- 시나리오: `docs/04_test/20260712-170529/esm/scenario.md`
- 테스트 계정: `user@itsm.local`(END_USER), `legal-coord@itsm.local`(DEPT_COORDINATOR/Legal), `it-coord@itsm.local`(DEPT_COORDINATOR/IT), `po@itsm.local`(PROCESS_OWNER), `hr@itsm.local`(HR_CASE_MANAGER)
- 브라우저: Playwright(새 context, storage 초기화)
- 11개 화면 규모로 화면별 TC 분리, 기능 회귀는 각 TC에 통합 포함

## 결과 요약

| TC | 결과 | 비고 |
|----|------|------|
| TC-BUILD-001 | PASS | backend `./gradlew build -x test` BUILD SUCCESSFUL, frontend `npm run build` 성공(사전 실행) |
| TC-ESM-I18N-001 | PASS | 부서 서비스 포털(SCR-ESM-001) 부서 탭 4종(HR/Legal/Facilities/Finance)·검색바·카탈로그 카드 영어 전환. 카탈로그 항목명/설명은 DB 데이터라 한국어 유지(정상) |
| TC-ESM-I18N-002 | PASS | 부서 요청 제출(SCR-ESM-002) "Request Form"/"Target User Name*"/"Cancel"/"Submit" 영어 전환. 카탈로그 스키마 필드 라벨(입사일/직책)은 DB 데이터라 한국어 유지(정상, 오류 메시지도 "{한국어 라벨} is required." 형태로 change phase와 동일 패턴). 실제 온보딩 요청 제출 시 "Request submitted (ESM-2026-0003). A checklist has been generated automatically." 토스트 정상(체크리스트 자동 생성 확인) |
| TC-ESM-I18N-003 | PASS | 내 부서 요청 목록(SCR-ESM-003) 필터(Status/From/To)·표 헤더(Ticket Key/Department/Type/Status/Updated)·부서 배지 2종(HR/Legal)·상태 배지 4종(Submitted/In Progress/Completed/Rejected) 전부 전환 확인 |
| TC-ESM-I18N-004 | PASS | 부서 요청 처리 큐(SCR-ESM-004) "Department Request Queue" 타이틀·필터·표 영어 전환. legal-coord 계정으로 Legal 부서 요청만 조회됨(타 부서 요청 비노출, 회귀 없음) |
| TC-ESM-I18N-005 | PASS | 부서 요청 상세(SCR-ESM-005) 상태 전이(Submitted→In Progress→Completed, 매 전이 "Status changed to '{status}'" 토스트)·코멘트 작성("Write Comment"/"Post", 등록 정상)·승인 패널("Approval Status"/"Step 1"/"Completed") 전부 영어 전환, 기능 회귀 없음 |
| TC-ESM-I18N-006 | PASS | 부서별 카탈로그 관리(SCR-ESM-006) 담당 부서 4종, 체크리스트 템플릿 유형 3종(None/Onboarding/Offboarding, NONE 포함), Onboarding 선택 시 "Checklist Template (Subtasks)" 섹션과 "No subtasks defined. Saving is allowed, but submitting an actual request of this type will be rejected." 경고 정상 전환. 하위 작업 담당 부서 5종(HR/Legal/Facilities/Finance/IT, IT 포함) 전부 전환 확인 |
| TC-ESM-I18N-007 | PASS | HR 케이스 목록(SCR-ESM-007) "HR Cases" 타이틀, 빈 목록 안내, "Intake Case" 모달(민감정보 안내 문구 포함) 전부 영어 전환. 케이스 접수 시 "Case submitted" 토스트 정상 |
| TC-ESM-I18N-008 | PASS | HR 케이스 상세(SCR-ESM-008) 4단계 순차 전이(Intake→Documentation→Investigation→Resolution) 전부 정상 토스트·상태 배지 전환, "Status History" 타임라인의 4개 항목 모두 정상 전환 확인(회귀 없음) |
| TC-ESM-I18N-009 | PASS | 온보딩 체크리스트 상세(SCR-ESM-009) "Onboarding Checklist" 타이틀(체크리스트 유형 라벨), "Overall progress 0/2"→"1/2"(하위 작업 완료 후 갱신 확인), 하위 작업 표(Department/Description/Status) 배지(HR/IT, Pending/Done) 전부 전환 |
| TC-ESM-I18N-010 | PASS | 내 하위 작업 목록(SCR-ESM-010) "My Subtasks" 타이틀, 표(Checklist Type/Target User/Task Description/Status), 체크리스트 유형 라벨 "Onboarding"이 SCR-ESM-009와 일관되게 표시(dev-lead 지시 사항 확인 완료). "Complete" 클릭 시 "Marked as complete" 토스트, 완료 후 SCR-ESM-009 진행률 자동 갱신(1/2) 회귀 없음 |
| TC-ESM-I18N-011 | PASS | ESM 지표 대시보드(SCR-ESM-011) "ESM Metrics" 타이틀, 부서 필터 4종, KPI 카드(Request Count/Avg. Processing Time/Onboarding·Offboarding Completion Rate) 전부 영어 전환. "Avg. Processing Time" 단위("min")가 `esmMetrics.minutesUnit` 키로 정상 번역됨을 소스로 확인(incident phase의 유사 결함 재발 없음) |
| TC-ESM-FORMAT-REG-001 | PASS | English 상태에서도 내 부서 요청 목록의 "Updated" 값이 `2026. 7. 12.`(ko-KR 로케일 포맷) 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음 확인 |
| TC-ESM-KO-ROUNDTRIP-001 | PASS | 지구본 아이콘으로 한국어 재전환 시 새로고침 없이 즉시 복귀. 카탈로그 관리 화면 필터·라벨·부서 배지(법무/재무/인사/시설) 전부 정상, 누락·깨짐 없음 |

## 결함 분석

없음. dev-lead가 사전에 통합·신설한 `checklistTypeLabel`(ChecklistDetailPage·MyChecklistTasksPage 공용)·`checklistTemplateTypeLabel`(EsmCatalogManagePage 전용, NONE 포함)·`validateForm`의 `t` 인자 추가 모두 라이브 테스트로 정상 동작 확인. incident/change/asset phase에서 발견됐던 유형의 결함(단위 미번역, 원시 enum 노출, falsy 가드 누락)이 이번 esm 도메인에는 재발하지 않음(사전 소스 리뷰 및 11개 화면 전체 라이브 테스트로 확인).

## 스크린샷/증적

- 별도 스크린샷 저장 없이 Playwright accessibility snapshot으로 각 화면의 텍스트 상태를 확인함(토스트 메시지는 `browser_run_code_unsafe`로 `.swal2-toast` 텍스트를 즉시 캡처).
- 테스트 중 실제 생성한 데이터: `ESM-2026-0003`(온보딩 요청, "i18n Test User", 체크리스트 자동 생성·IT 하위 작업 완료 처리), `ESM-2026-0002`(Legal 계약서 검토, In Progress→Completed 전이·코멘트 등록), `HR-2`(HR 케이스, Intake→Documentation→Investigation→Resolution 전체 전이)

## 종합 판정

esm 도메인(11개 화면) i18n 전환 **전 항목 PASS**. 부서 포털/요청 제출·목록·처리 큐·상세(상태 전이·코멘트·승인 패널)/카탈로그 관리(템플릿 유형 NONE 포함)/HR 케이스 목록·상세(4단계 전이)/체크리스트 상세·내 하위 작업(체크리스트 유형 라벨 일관성)/지표 대시보드/날짜 포맷(ko-KR 유지)/한국어 재전환 모두 정상 확인. 결함 없음.
