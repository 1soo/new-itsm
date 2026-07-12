# 통합 테스트 결과 — asset (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12)

- 시나리오: `docs/04_test/20260712-164256/asset/scenario.md`
- 테스트 계정: `am@itsm.local`(ASSET_MANAGER)
- 브라우저: Playwright(새 context, storage 초기화)

## 결과 요약

| TC | 결과 | 비고 |
|----|------|------|
| TC-BUILD-001 | PASS | backend `./gradlew build -x test` BUILD SUCCESSFUL, frontend `npm run build` 성공(사전 실행) |
| TC-ITAM-I18N-001 | PASS | 자산 목록(SCR-ITAM-001) 필터(Type/Status/Owner/Expiring Within (days))·표 헤더(Asset Key/Name/Type/Status/Owner/Expiry Date) 영어 전환. 유형 배지 3종(Hardware/Software/Cloud), 상태 배지 5종(Planning/Procurement/Operation/Maintenance/Retirement) 필터 드롭다운에서 정상 전환 확인 |
| TC-ITAM-I18N-002 | PASS | 자산 등록(SCR-ITAM-002) 폼 라벨(Name/Type/Owner/Location/Purchase Date/Cost/License·Warranty·Contract Expiry) 및 "Type-specific Attributes" 섹션 영어 전환. 필수 미입력 시 "Name and type are required." 오류 정상. 만료일 과거(2020-01-01) 입력 후 저장 시 별도 경고 토스트는 없으나 상세 화면에서 "Expired" 배지로 즉시 강조 표시(설계상 "경고" 요구사항 충족, 회귀 없음). 저장 성공 토스트 "Asset registered (AST-0002)" 영어 |
| TC-ITAM-I18N-003 | PASS(단, 결함 1건 발견) | 상세(SCR-ITAM-003) 생애주기 전이 버튼(Procurement/Operation/Maintenance/Retire)·"Basic Information"/"Ticket Link"/"Expiry Information"/"Linked Tickets"/"Linked CIs" 섹션 전부 영어 전환. 상태 전이 시 "Status changed to 'Procurement'" 토스트 정상. 티켓 연계(Incident, Target ID=1) 정상 동작, "Linked Tickets" 패널에 "Incident · INC-2026-0001"로 정상 표시(dev-lead 사전 수정 확인). **"Lifecycle History"(생애주기 이력) 목록에서 단계명이 원시 enum("PROCUREMENT")으로 노출**(아래 결함 1) |
| TC-ITAM-I18N-004 | PASS | CI·CMDB 관계 뷰(SCR-ITAM-004) "CI List"/"Add Relation"/"Impact Scope" 섹션, CI 등록 폼("Register New CI"), 관계 유형 드롭다운("Depends On(DEPENDS_ON)" — 라벨 번역 정상, 괄호 안 원시 코드 병기는 이번 phase 이전부터 있던 기존 UI 설계로 확인되어 정상) 전부 영어 전환. 실제 CI 2개 등록 후 관계 추가(Depends On) → "Impact Scope" 패널에 "Depends On"으로 정상 표시(원시값 노출 없음, dev-lead 사전 수정 확인) |
| TC-ITAM-I18N-005 | PASS | 지표 대시보드(SCR-ITAM-005) 타이틀 "Asset Metrics", 기간 필터(From/To), KPI 카드(Utilization Rate/Expiring Soon), "Distribution by Type" 차트(Hardware/Software/Cloud) 전부 영어 전환 |
| TC-ITAM-FORMAT-REG-001 | PASS | English 상태에서도 자산 상세의 Warranty 만료일 값이 `2020. 1. 1.`(ko-KR 로케일 포맷) 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음 확인 |
| TC-ITAM-KO-ROUNDTRIP-001 | PASS | 지구본 아이콘으로 한국어 재전환 시 새로고침 없이 즉시 복귀. 목록 화면 필터(유형/상태/소유자/만료 임박(일))·표 헤더(식별키/이름/유형/상태/소유자/만료일)·배지(하드웨어/구매/폐기/미지정/경과) 전부 정상, 누락·깨짐 없음 |

## 결함 분석

### [결함 1] 자산 상세 화면 "생애주기 이력(Lifecycle History)" 목록의 단계명이 원시 enum 값으로 노출 (언어 무관)

- **위치**: `source/frontend/src/features/asset/AssetDetailPage.tsx:232`
- **현상**: 자산 상단의 현재 상태 배지는 `statusLabel(t, detail.status)`로 정상 번역되어 있으나, "Lifecycle History"/"생애주기 이력" 섹션에서 이력 항목을 나열하는 부분은 `{h.stage}`(원시 값, 예: `"PROCUREMENT"`)를 그대로 렌더링. 그 결과:
  - English 모드: "Lifecycle History"에 "PROCUREMENT" 노출 (기대: "Procurement")
  - 한국어 모드: "생애주기 이력"에도 동일하게 "PROCUREMENT" 노출 (기대: "구매") — **한국어 모드에서도 미번역 상태**로, change 도메인 "연계 항목" 결함·dev-lead가 이번 asset phase에서 선제 수정한 티켓유형/CI관계유형 결함과 동일한 유형이지만, 이 지점(`h.stage`)은 발견·수정되지 않은 채 남아 있었음
- **재현**: am@itsm.local 로그인 → 자산 상세 진입 → 생애주기 전이 버튼(예: "Procurement") 클릭 → "Lifecycle History" 섹션에 "PROCUREMENT" 노출 확인(영어/한국어 모드 모두 동일 현상)
- **제안 수정**: 기존 `features/asset/status.ts`의 `statusLabel(t, s)`를 재사용해 `h.stage`를 라벨로 매핑(예: `statusLabel(t, h.stage as AssetStatus)`)하여 232번 라인에 적용. `types.ts`의 `LifecycleEntry.stage: string` 타입이 느슨하게 정의되어 있어(39행) 실제로는 `AssetStatus` 값이 들어오므로 캐스팅 또는 타입 강화 필요

## 스크린샷/증적

- 별도 스크린샷 저장 없이 Playwright accessibility snapshot으로 각 화면의 텍스트 상태를 확인함(토스트 메시지는 `browser_run_code_unsafe`로 `.swal2-toast` 텍스트를 즉시 캡처).
- 테스트 중 실제 생성한 데이터: `AST-0002`("i18n test asset - laptop", Hardware, Warranty Expiry 2020-01-01 → Expired, Planning→Procurement 전이, INC-2026-0001과 티켓 연계), CI 2건("i18n test CI - server A"→"i18n test CI - db B", DEPENDS_ON 관계)

## 재테스트 (결함 1 수정 확인)

- 수정 내용: `AssetDetailPage.tsx:232`에서 `{h.stage}` 대신 `statusLabel(t, h.stage as AssetStatus)`로 변경(기존 `features/asset/status.ts`의 `statusLabel` 재사용).
- 재검증 절차: 새 브라우저 context, storage 초기화 → am@itsm.local 로그인 → English 전환 → 기존 AST-0002 상세(TC-ITAM-I18N-003에서 Procurement로 전이한 자산) 진입 → "Lifecycle History" 확인 → 한국어 재전환 후 "생애주기 이력" 재확인
- **결과: PASS**
  - English: "Lifecycle History"에 "**Procurement**" 정상 표시(기존 "PROCUREMENT"에서 수정됨)
  - 한국어: "생애주기 이력"에 "**구매**" 정상 표시
- `npm run build` 통과(dev-lead 확인).
- 나머지 TC는 지난 결과 그대로 유효하다고 판단해 전체 재실행하지 않음.

## 종합 판정

asset 도메인 i18n 전환 **전 항목 PASS**(결함 1건 수정 확인 완료). 목록/등록(만료 경고)/상세(생애주기 전이·티켓 연계·CI·생애주기 이력 라벨)/CI·CMDB 관계 뷰(관계 추가·영향 범위)/지표 대시보드/날짜 포맷(ko-KR 유지)/한국어 재전환 모두 정상 확인.
