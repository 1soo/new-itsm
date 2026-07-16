---
date: 20260712-160842
domain: change
result: pass
keywords: [다국어(i18n) 전환, 연계 항목 라벨, 날짜 포맷 로케일 유지]
---

# 통합 테스트 결과 — change (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12)

- 시나리오: `docs/04_test/20260712-160842/change/scenario.md`
- 테스트 계정: `cm@itsm.local`(CHANGE_MANAGER)
- 브라우저: Playwright(새 context, storage 초기화)

## 결과 요약

| TC | 결과 | 비고 |
|----|------|------|
| TC-BUILD-001 | PASS | backend `./gradlew build -x test` BUILD SUCCESSFUL, frontend `npm run build` 성공(사전 실행, 소스 리뷰 단계에서 확인) |
| TC-CHG-I18N-001 | PASS | 목록(SCR-CHG-001) 필터(Type/Status/Risk/From/To)·표 헤더(Ticket Key/Summary/Type/Status/Risk/Scheduled) 영어 전환. 상태 배지 6종(Requested/Review/Planning/Approval/Implementation/Closed), 유형 배지 3종(Standard/Normal/Emergency), 위험도 배지 3종(High/Medium/Low) 전부 필터 드롭다운에서 정상 전환 확인 |
| TC-CHG-I18N-002 | PASS | RFC 생성(SCR-CHG-002) 폼 라벨 전부 영어 전환. 필수 미입력 제출 시 "Summary is required." 오류 정상 표시. "표준" 선택 시 "Standard Change Template" 섹션과 "Standard changes automatically skip the approval step (pre-approved)." 안내 정상 전환. Normal/Medium으로 실제 생성 시 SweetAlert2 토스트 "Change request created (CHG-2026-0002)" 후 상세 화면 정상 이동 |
| TC-CHG-I18N-003 | PASS(단, 결함 1건 발견) | 상세(SCR-CHG-003) 상태 전이(Requested→Review→Planning→Approval→Implementation, 매 전이마다 "Status changed to '{status}'" 토스트) 정상. 승인 패널(공용) "Approval Status"/"This change has no approval process" 정상(해당 티켓은 매칭 승인 프로세스 없음). 구현 결과 기록(Outcome=Success, Note 입력 후 Save Result) → "Implementation result recorded" 토스트 정상. **인시던트/문제 연계 결함 발견**(아래 결함 1) |
| TC-CHG-I18N-004 | PASS | 일정 캘린더(SCR-CHG-005) 타이틀 "Change Schedule", 유형 필터, 요일 헤더(Sun~Sat), 월 라벨("7/2026" 형식) 전부 정상 전환. "Previous month" 클릭 시 "6/2026"으로 정상 이동(기능 회귀 없음) |
| TC-CHG-I18N-005 | PASS | 지표 대시보드(SCR-CHG-006) 타이틀 "Change Metrics", 기간 필터(From/To), KPI 카드(Success Rate/Failure Rate/Emergency Change Rate) 전부 영어 전환 |
| TC-SEARCH-CHG-001 | PASS | 통합 검색 결과에서 CHANGE 도메인 배지 "Change", 상태 배지 "Implementation" 정상 전환(`features/search/status.ts`가 `change/status.ts` 재사용 확인) |
| TC-CHG-FORMAT-REG-001 | PASS | English 상태에서도 통합검색 "Updated At" 값이 `2026. 7. 12. 오후 4:17:22`(ko-KR 로케일 포맷) 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음 확인 |
| TC-CHG-KO-ROUNDTRIP-001 | PASS | 지구본 아이콘으로 한국어 재전환 시 새로고침 없이 즉시 복귀. 목록 화면 필터(유형/상태/위험도/시작일/종료일)·표 헤더(식별키/요약/유형/상태/위험도/예정일)·배지(일반/구현/보통/낮음) 전부 정상, 누락·깨짐 없음. 상세 화면도 "승인 현황"/"이 변경에는 승인 절차가 없습니다"/"연계 항목" 정상(결함 1의 "INCIDENT" 노출 제외) |

## 결함 분석

### [결함 1] 상세 화면 "연계 항목(Linked Items)" 목록의 링크 대상 유형이 원시 enum 값으로 노출 (언어 무관)

- **위치**: `source/frontend/src/features/change/ChangeDetailPage.tsx:194`
- **현상**: 인시던트/문제 연계(SCR-CHG-003) 섹션에서 링크 생성 시 대상 유형 드롭다운(`SelectItem`)은 `t("changeDetail.linkTargetIncident", {defaultValue: "인시던트"})`/`linkTargetProblem`으로 정상 번역되어 있으나(라인 342-343), 정작 생성된 연계 항목을 나열하는 "Linked Items"/"연계 항목" 패널에서는 `l.type`(원시 값 `"INCIDENT"`)을 그대로 렌더링(`{l.type} · {l.targetKey}`). 그 결과:
  - English 모드: "Linked Items" 패널에 "INCIDENT · INC-2026-0001"로 표시 (기대: "Incident · INC-2026-0001")
  - 한국어 모드: "연계 항목" 패널에도 동일하게 "INCIDENT · INC-2026-0001"로 표시 (기대: "인시던트 · INC-2026-0001") — **한국어 모드에서도 미번역 상태**로, 언어 전환과 무관하게 발생하는 결함
- **재현**: cm@itsm.local 로그인 → 변경 상세 진입 → "Incident/Problem Link"(Link Target=Incident, Target ID=1) 입력 후 "Link" 클릭 → "Linked Items" 패널에 "INCIDENT · INC-2026-0001" 노출 확인(영어/한국어 모드 모두 동일 현상)
- **제안 수정**: 동일 파일 내 이미 정의된 `t("changeDetail.linkTargetIncident", ...)`/`t("changeDetail.linkTargetProblem", ...)` 키를 재사용해 `l.type`을 라벨로 매핑하는 헬퍼(예: `linkTargetLabel(t, l.type)`)를 만들어 194번 라인에 적용

## 스크린샷/증적

- 별도 스크린샷 저장 없이 Playwright accessibility snapshot으로 각 화면의 텍스트 상태를 확인함(토스트 메시지는 `browser_run_code_unsafe`로 `.swal2-toast` 텍스트를 즉시 캡처).
- 테스트 중 실제 생성한 데이터: `CHG-2026-0002`("i18n test change - standard template check", Normal/Medium, Implementation 단계까지 전이, 구현 결과 Success 기록, INC-2026-0001과 연계)

## 재테스트 (결함 1 수정 확인)

- 수정 내용: `linkTargetLabel(t, type)` 헬퍼(`features/change/status.ts`) 추가, `LinkedItemType`(INCIDENT/PROBLEM/ASSET/COMPLIANCE_REQUIREMENT) 4종 전부 커버(지적한 2종보다 넓게 수정). `ChangeDetailPage.tsx:195`에서 `l.type` 대신 `linkTargetLabel(t, l.type)` 사용하도록 수정.
- 재검증 절차: 새 브라우저 context, storage 초기화 → cm@itsm.local 로그인 → English 전환 → 기존 CHG-2026-0002 상세 진입(TC-CHG-I18N-003에서 INC-2026-0001과 연계 생성한 티켓) → "Linked Items" 패널 확인 → 한국어 재전환 후 "연계 항목" 패널 재확인
- **결과: PASS**
  - English: "Linked Items" 패널에 "**Incident** · INC-2026-0001" 정상 표시(기존 "INCIDENT · INC-2026-0001"에서 수정됨)
  - 한국어: "연계 항목" 패널에 "**인시던트** · INC-2026-0001" 정상 표시
- ASSET/COMPLIANCE_REQUIREMENT 연계는 이번 change 도메인 테스트 데이터로는 생성 경로가 없어(연계 생성 드롭다운이 Incident/Problem 2종만 제공) 라이브 검증 불가. 소스 확인 결과 `LINK_TARGET_LABEL`/`LINK_TARGET_KEY`/`en·ko/change.json`(`linkTargetAsset`/`linkTargetComplianceRequirement`) 모두 정상 정의되어 있어 로직상 문제 없음으로 판단.
- `npm run build` 통과(dev-lead 확인).

## 종합 판정

change 도메인 i18n 전환 **전 항목 PASS**(결함 1건 수정 확인 완료). 목록/RFC 생성/상세(상태 전이·승인 패널·구현결과·연계 항목 라벨)/일정 캘린더(요일·월 라벨)/지표 대시보드/통합검색 배지/날짜 포맷(ko-KR 유지)/한국어 재전환 모두 정상 확인.
