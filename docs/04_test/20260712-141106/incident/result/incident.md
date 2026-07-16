---
date: 20260712-141106
domain: incident
result: pass
keywords: [다국어전환i18n, formatMinutes단위표기, 통합검색상태배지, 날짜포맷유지]
---

# 통합 테스트 결과 — incident (20260712-141106)

## 요약
- 1차: 총 12건 · 성공 11 · 실패 1
- 재검증(결함 수정 후): `formatMinutes` 단위 통일 1건 PASS 확인 — 최종 전 항목 PASS

## 상세

| TC ID | 결과 | 실제 동작 | 비고 |
|-------|------|-----------|------|
| TC-BUILD-001 | PASS | `./gradlew build -x test` BUILD SUCCESSFUL, `npm run build` 성공(기존 청크 크기 경고만 존재) | - |
| TC-INC-I18N-001 | PASS | 인시던트 목록: "Incidents"/"Report Incident", 필터(Status/Severity/Assignee/Keyword)/"Search", 표 헤더(ID/Summary/Severity/Status/Assignee/PM/Updated At), 상태 배지(New/Resolved 등) 영어 전환. 심각도 값(SEV1~3)은 코드값 자체가 표시값이라 전환 대상 아님 | - |
| TC-INC-I18N-002 | PASS | 등록: 폼 라벨(Summary/Description/Severity/Affected Service/Affected Product)·"Report" 버튼 영어 전환. 필수 미입력("Summary is required.") 인라인 오류 확인. 정상 등록(INC-2026-0004, SEV2) 시 토스트 "Incident reported (INC-2026-0004)" 확인 후 상세 이동(회귀 없음) | - |
| TC-INC-I18N-003 | PASS | 상세 전 섹션 확인: 상태 전이(New→In Progress→Resolved→Closed, 각 토스트 "Status changed to '...'"), 역할 배정("Assign Responder"/"Role Assign"→토스트 "Role assigned successfully", Responders 패널에 "Tech Lead" 라벨 반영), 에스컬레이션(토스트 "Escalated successfully"), 상태 업데이트("Update Message"/"Visibility"·"Internal"/"Post"), 해결 처리("Resolve"→토스트 "Resolved successfully"), 문제 연계("Create & Link New Problem"→토스트 "Problem linked successfully"), 승인 패널(공용, "Approval Status"/"Step 1"/"In Progress"→"Completed", 실제 승인 프로세스 매칭 인시던트(INC E2E 테스트 승인 규칙)로 409 게이트→승인 후 재시도 전 과정 실제 확인). 전 기능 회귀 없음 | shots 없음(텍스트 스냅샷으로 확인) |
| TC-INC-I18N-004 | **FAIL(부분)** | 값 없을 때는 "Not calculated" 정상 전환. 그러나 실제 값이 계산된 후 시간지표가 "10분"/"15분"/"25분"로 한국어 단위 그대로 표시됨(영어 전환 안 됨) — 하단 "실패 항목 분석" 참조. 참고로 지표 대시보드(SCR-INC-005)의 평균 MTTR은 동일 값이 "25min"으로 정상 전환되어 대조됨 | - |
| TC-INC-I18N-005 | PASS | 포스트모템: "Postmortem"/"Back to Incident", "Overview"(Summary/Timeline Summary), "5 Whys"(Why? 1/Delete/Add Why), "Root Cause"(Root cause (required)), "Action Items"(No action items./Add Action Item), Cancel/Save 영어 전환. 근본원인 미입력 시 "Root cause is required." 오류, 정상 제출 시 토스트 "Postmortem saved successfully" 확인, 인시던트에 연결 저장(회귀 없음) | - |
| TC-INC-I18N-006 | PASS | 지표 대시보드: "Incident Metrics", 필터(From Date/To Date)/"Search", KPI 카드("Incident Count"/"Avg MTTR", "25min" 정상 단위 전환), "Severity Distribution" 차트(SEV1/2/3 라벨) 영어 전환 | - |
| TC-INC-TIMELINE-REG-001 | PASS(참고) | 타임라인 섹션 타이틀 "Status Update & Timeline"은 영어 전환, BE 생성 이벤트 메시지("인시던트가 등록되었습니다.", "상태가 IN_PROGRESS로 변경되었습니다." 등)는 한국어 유지 — dev-lead 사전 안내대로 결함 아님 | - |
| TC-SEARCH-INC-001 | PASS | 통합 검색 결과: INCIDENT 도메인 배지("Incident")와 상태 배지("Closed") 영어 전환(`features/search/status.ts`가 `incident/status.ts`의 `statusLabel(t, ...)` 재사용) | - |
| TC-INC-FORMAT-REG-001 | PASS | English 상태에서도 타임라인·검색 결과의 날짜/시각이 ko-KR 포맷(`2026. 7. 12. 오후 ...`) 그대로 유지, 영어 로케일로 전환되지 않음 확인 | - |
| TC-INC-KO-ROUNDTRIP-001 | PASS | English 상태에서 한국어로 재전환 시 새로고침 없이 즉시 복귀("통합 검색 결과"/"인시던트"/"종료" 등), 누락·깨짐 없음 | - |
| TC-INC-CROSSREG-001 | PASS | SERVICE_DESK_AGENT(agent@itsm.local)로 인시던트 상세 진입 시 "Assign Responder"(역할 배정) 섹션 자체가 렌더링되지 않음(숨김) 확인. INCIDENT_MANAGER(im@itsm.local)로 동일 인시던트 진입 시에는 정상 노출(회귀 없음, 텍스트 전환과 무관) | - |

## 실패 항목 분석

- **[결함] `features/incident/format.ts`의 `formatMinutes` — 값이 있을 때 단위 미번역**: 17번째 줄에서 값이 `null`일 때만 `t("metrics.notCalculated", ...)`로 번역하고, 실제 숫자가 있을 때는 `` `${Math.round(m)}분` ``으로 한국어 단위 "분"을 그대로 하드코딩한다. 그 결과 인시던트 상세(SCR-INC-003) 화면에서 English 전환 후에도 MTTD/MTTA/MTTR 값이 "10분"/"15분"/"25분"로 표시된다. 같은 인시던트의 같은 가 값이 지표 대시보드(SCR-INC-005, `IncidentMetricsPage.tsx`)에서는 `t("metrics.minutesUnit", { defaultValue: "분" })`을 통해 "25min"으로 정상 전환되어, 같은 도메인 내에서 화면별로 단위 표기가 불일치한다. 재현: `im@itsm.local` 로그인 → English 전환 → 시간지표가 실제로 계산된 인시던트 상세 진입 → 우측 "Time Metrics" 패널에서 "10분" 등 한국어 단위 확인. 해결: `formatMinutes`의 `` `${Math.round(m)}분` `` 부분도 `t("metrics.minutesUnit", { ns: "incident", defaultValue: "분" })` 키를 사용하도록 통일.

## 테스트 데이터 안내
- `agent@itsm.local`로 INC-2026-0004(i18n test incident, SEV2)를 등록하고, `im@itsm.local`로 상태 전이(New→In Progress→Resolved→Closed) 전 과정, 역할 배정(Tech Lead), 에스컬레이션, 상태 업데이트, 해결 처리, 승인 처리(승인 프로세스 규칙에 매칭되어 409 게이트 실제 확인 후 승인), 포스트모템 작성, 문제 신규 연계(PRB-2026-0002)까지 진행했다. 실제 운영 데이터가 아닌 테스트 산출물이므로 별도 정리는 하지 않았다(기존 tester 테스트 데이터 컨벤션과 동일하게 유지).

## 재테스트 (결함 수정 후)

dev_fe가 `features/incident/format.ts`의 `formatMinutes`를 수정 — 값이 있을 때도 `t("metrics.minutesUnit", { ns: "incident", defaultValue: "분" })` 키로 통일했다. `npm run build` 통과 확인(재빌드로 재확인).

| 항목 | 결과 | 실제 동작 |
|------|------|-----------|
| 결함 재검증 — `formatMinutes` 단위 통일 | **PASS** | `im@itsm.local` 로그인 → English 전환 → INC-2026-0004(id=13) 상세 재진입 → "Time Metrics" 패널에서 MTTD/MTTA/MTTR이 "10 min"/"15 min"/"25 min"으로 정상 전환 확인(지표 대시보드의 "25min"과 동일 단위 체계로 일치) |

**재테스트 결론**: 결함 수정 확인. incident 도메인 통합 테스트 전 항목 PASS.
