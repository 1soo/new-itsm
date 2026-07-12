# 통합 테스트 결과 — compliance (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12)

- 시나리오: `docs/04_test/20260712-174109/compliance/scenario.md`
- 테스트 계정: `co@itsm.local`(COMPLIANCE_OFFICER)
- 브라우저: Playwright(새 context, storage 초기화)

## 결과 요약

| TC | 결과 | 비고 |
|----|------|------|
| TC-BUILD-001 | PASS | backend `./gradlew build -x test` BUILD SUCCESSFUL, frontend `npm run build` 성공(사전 실행) |
| TC-COMP-I18N-001 | PASS | 요구사항 목록(SCR-COMP-001) 필터(Compliance Status/Owner Assigned)·표 헤더(Requirement Key/Name/Basis/Owner/Compliance Status/Updated) 영어 전환. 준수 상태 배지 2종(Compliant/Non-compliant), 책임자 지정 필터(Assigned/Unassigned) 정상 전환, "Owner not assigned" 배지 정상 |
| TC-COMP-I18N-002 | PASS | 요구사항 등록(SCR-COMP-002) 폼 라벨(Name/Basis/Scope)·등록 버튼 영어 전환. 필수 미입력 시 "Name and basis are required." 오류 정상. 등록 성공 토스트 "Requirement registered (COMP-2026-0005)" 영어, 상세 이동(회귀 없음) |
| TC-COMP-I18N-003 | PASS | 상세(SCR-COMP-003) 책임자 지정("Owner assigned")·시정조치 2건 등록/전이(Detected→In Progress→Resolved, 매 전이 "Corrective action status changed" 토스트)·변경 연계("Linked to change request", "CHG-2026-0001" 원문 표시)·"수정"(Edit) 인라인 편집("Requirement updated") 전부 영어 전환, 정상 동작(회귀 없음). **감사 로그 이벤트 유형 3종 전부 라벨로 정상 표시**("Requirement Created"/"Requirement Updated"/"Corrective Action Status Changed", 원시 코드 `COMPLIANCE_REQ_CREATE` 등 노출 없음). **승인 패널이 시정조치 항목별로 개별 표시되는 설계를 실제로 검증**: 2건의 시정조치 모두 RESOLVED 전이 시 매칭 승인 프로세스가 걸려("Cannot transition to resolved before approval is complete" 툴팁 정상 전환) 각 항목 옆에 독립적인 "Approval Status" 패널("Step 1"/"In Progress"→"Completed") 표시, 승인함(`/approvals`)에서 "Ticket Type"="Corrective Action"으로 개별 승인 처리 후 양쪽 항목 모두 정상 전이. 모든 시정조치 해결 시 요구사항 상태 Non-compliant→Compliant 자동 전환(회귀 없음) |
| TC-COMP-I18N-004 | PASS | 준수 현황 대시보드(SCR-COMP-004) 타이틀 "Compliance Status", 기간 필터(From/To), KPI 카드(Compliance Rate/Open Corrective Actions), "Status by Requirement" 표(Requirement/Owner/Compliance Status) 전부 영어 전환 |
| TC-COMP-FORMAT-REG-001 | PASS | English 상태에서도 목록의 "Updated" 값이 `2026. 7. 12.`(ko-KR 로케일 포맷) 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음 확인 |
| TC-COMP-KO-ROUNDTRIP-001 | PASS | 지구본 아이콘으로 한국어 재전환 시 새로고침 없이 즉시 복귀. 목록 화면 필터·표 헤더·배지(준수/미준수/책임자 미지정) 전부 정상, 누락·깨짐 없음 |

## 결함 분석

없음. dev-lead가 사전에 수정한 감사 로그 이벤트 유형 라벨(`auditEventTypeLabel`) 결함이 정상 동작함을 실제로 요구사항 생성·수정·시정조치 상태 변경 이벤트 3종 전부에 대해 라이브로 확인. compliance 도메인 고유의 "시정조치 항목별 개별 승인 패널" 구조도 실제 2건의 시정조치로 각각 독립적으로 게이트가 걸리고 개별 승인 처리되는 것을 end-to-end로 검증, 번역 상태와 기능 모두 이상 없음.

## 스크린샷/증적

- 별도 스크린샷 저장 없이 Playwright accessibility snapshot으로 각 화면의 텍스트 상태를 확인함(토스트 메시지는 `browser_run_code_unsafe`로 `.swal2-toast` 텍스트를 즉시 캡처).
- 테스트 중 실제 생성한 데이터: `COMP-2026-0005`("i18n test compliance requirement", 책임자 배정, 시정조치 2건(CA-9/CA-10) 각각 Detected→In Progress→Resolved 전이(승인 게이트 통과), CHG-2026-0001과 연계, 이름/근거/범위 수정)

## 종합 판정

compliance 도메인 i18n 전환 **전 항목 PASS**. 목록/등록/상세(책임자·시정조치(개별 승인 패널)·변경 연계·감사 로그·인라인 수정)/준수 현황 대시보드/날짜 포맷(ko-KR 유지)/한국어 재전환 모두 정상 확인. 결함 없음.
