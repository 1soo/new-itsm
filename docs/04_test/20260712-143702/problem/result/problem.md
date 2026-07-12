# 통합 테스트 결과 — problem (20260712-143702)

## 요약
- 총 10건 · 성공 10 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 | PASS | `./gradlew build -x test` BUILD SUCCESSFUL, `npm run build` 성공(기존 청크 크기 경고만 존재) | - |
| TC-PRB-I18N-001 | PASS | 문제 목록: "Problems"/"Search KEDB"/"Report Problem", 필터(Status/Priority/Origin/Assignee/From Date/To Date)/"Search", 표 헤더(ID/Summary/Status/Priority/Origin/Assignee/Updated At), 상태 배지 6단계 전부("Detection"/"Closed" 등) 영어 전환. Origin 값("Reactive") 전환, Priority 코드값(P1~P4) 유지 확인 | - |
| TC-PRB-I18N-002 | PASS | 등록: "Report Problem"/"New Problem", 폼 라벨(Summary/Description/Origin/Component/Investigation Reason/Impact/Urgency)·"Report" 버튼 영어 전환. 필수 미입력("Summary is required.") 오류 확인. 영향도만 선택 시 "Priority Preview: Not calculated" 유지, 둘 다 선택 시 "P1" 정상 계산 확인. 정상 등록(PRB-2026-0003) 시 토스트 "Problem reported (PRB-2026-0003)" 확인 후 상세 이동(회귀 없음) | - |
| TC-PRB-I18N-003 | PASS | 상세 전 섹션 확인: 상태 전이(Detection→Classification→Investigating→Known Error→Workaround→Closed, 각 토스트 "Status changed to '...'"), RCA("Root Cause Analysis (RCA)"/"5 Whys"/"Why? 1"/"Add Step"/"Category"/"Save RCA"→토스트 "RCA saved successfully"), 워크어라운드("Temporary Workaround"/"Submit"→토스트 "Workaround registered successfully"), 알려진 오류 생성("Register Known Error (KEDB)"/"Create KE"→토스트 "Known error registered successfully"), 인시던트 연결("Link Incident"→토스트 "Incident linked successfully", Linked Incidents에 "INC-2026-0004" 표시), 후속 조치("Action Items"/"Add"→토스트 "Action item added successfully", 상태 "In Progress"→"Mark Done" 클릭 시 "Done"→토스트 "Action status changed successfully"), 승인 패널(공용, "Approval Status"/"Step 1"/"In Progress"→"Completed", 실제 승인 프로세스 매칭(PRB E2E 테스트 승인 규칙)으로 409 게이트→승인함에서 승인→재시도 성공까지 전 과정 실제 확인), 종료(토스트 "Status changed to 'Closed'"). 전 기능 회귀 없음 | - |
| TC-PRB-ORIGIN-REG-001 | PASS | 레거시 데이터(PRB-2026-0001, origin 값 없음) 목록에서 Origin 컬럼이 빈 셀로 정상 표시(날 것의 "origin.undefined" 키 문자열 노출 없음) 확인 — dev_fe 자체 수정 회귀 정상 반영. Origin 값이 있는 문제(PRB-2026-0002 "Reactive", 신규 등록 PRB-2026-0003 "Reactive")는 정상적으로 영어 라벨 표시 | - |
| TC-PRB-I18N-004 | PASS | KEDB 검색: "Search Known Errors (KEDB)" 타이틀, 검색바 placeholder("Search by title or symptom keyword")/"Search" 버튼, 결과 chrome("Root Cause"/"Workaround" 라벨) 영어 전환. 등록한 알려진 오류로 검색 시 정상 매칭, 결과 없는 키워드 검색 시 빈 목록 안내("No known errors"/"No known errors match the criteria.") 영어 전환 확인 | - |
| TC-SEARCH-PRB-001 | PASS | 통합 검색 결과: PROBLEM 도메인 배지("Problem")와 상태 배지("Closed") 영어 전환(`features/search/status.ts`가 `problem/status.ts`의 `statusLabel(t, ...)` 재사용) | - |
| TC-PRB-FORMAT-REG-001 | PASS | English 상태에서도 검색 결과·목록의 날짜/시각이 ko-KR 포맷(`2026. 7. 12. 오후 ...`) 그대로 유지, 영어 로케일로 전환되지 않음 확인 | - |
| TC-PRB-KO-ROUNDTRIP-001 | PASS | English 상태에서 한국어로 재전환 시 새로고침 없이 즉시 복귀("통합 검색 결과"/"문제"/"종료" 등), 누락·깨짐 없음 | - |

## 참고 사항

- RESOLVED_CLOSED 전이 409 게이트 응답 메시지("승인 대기 중에는 이행할 수 없습니다.")와 미해결 후속조치 종료 경고 다이얼로그의 상세 문구(`res.warning`)는 BE 응답 원문이라 English 전환 후에도 한국어로 남는다 — incident/service-request phase와 동일한 "BE/DB 변경 없음" 원칙에 따른 것으로 결함이 아니다(다이얼로그 타이틀 "There are unresolved action items"는 FE 소유 텍스트라 정상 전환됨).

## 테스트 데이터 안내
- `pm@itsm.local`로 PRB-2026-0003(i18n test problem, P1)을 등록하고, 상태 전이 전 과정(Detection→Classification→Investigating→Known Error→Workaround→Closed), RCA 작성, 워크어라운드 등록, 알려진 오류 생성(KEDB 검색으로 재확인), 인시던트 연계(INC-2026-0004), 후속 조치 등록·완료 처리, 승인 게이트(실제 매칭 규칙으로 409 발생→승인함에서 자가 승인→재시도 성공)까지 전부 진행했다. 실제 운영 데이터가 아닌 테스트 산출물이므로 별도 정리는 하지 않았다(기존 tester 테스트 데이터 컨벤션과 동일하게 유지).
