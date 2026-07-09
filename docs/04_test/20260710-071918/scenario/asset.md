# 통합 테스트 시나리오 — asset (ITAM) 재테스트

> 실행 타임스탬프: 20260710-071918 · 도메인: asset
> 목적: 직전 실행(20260710-064414) 실패 1건(TC-REG-002 자산-티켓 단방향 연계) 수정분 재검증 + 관련 회귀(4개 도메인 상세 조회 전반)
> 원본 시나리오: docs/04_test/20260710-064414/scenario/asset.md

## 사전 조건
- BE(:8080, 재기동 완료)·FE dev(:5173) 기동, PostgreSQL(`itsm-postgres`) healthy
- 계정: am@itsm.local(ASSET_MANAGER), im/pm/cm(각 도메인 매니저)

## 시나리오

### A. 빌드 (재검증)
- **TC-BUILD-001** · BE `gradlew test --rerun-tasks` 통과(수정된 AssetService + problem/change/srm DTO 변경분 포함) — 전 도메인 FEAT
- **TC-BUILD-002** · FE `npm run build` 통과(problem/change/srm 상세 화면 "연결 자산" 표시 추가분 포함)

### B. TC-REG-002 수정 재검증 — 자산-티켓 양방향 연계
- **TC-REG-002-1** · 신규 자산→INCIDENT 연계 후 **인시던트 상세**에서 `links`에 `{"type":"ASSET","targetKey":...}` 노출 확인
- **TC-REG-002-2** · 신규 자산→PROBLEM 연계 후 **문제 상세**에서 신설된 `linkedAssets:[{id,ticketKey}]` 노출 확인
- **TC-REG-002-3** · 신규 자산→CHANGE 연계 후 **변경 상세**에서 확장된 `links[].type=ASSET` 노출 확인
- **TC-REG-002-4** · 신규 자산→SERVICE_REQUEST 연계 후 **서비스 요청 상세**에서 신설된 `linkedAssets:[{id,assetKey}]` 노출 확인

### C. 관련 회귀 — 4개 도메인 상세 조회 전반
- **TC-REG-002-5** · 자산이 연계되지 않은 기존 문제/서비스요청 상세 조회 시 `linkedAssets=[]`(빈 배열, 정상 렌더링, 오류 없음) — 회귀 없음 확인
- **TC-REG-002-6** · 자산 쪽 조회(linkedTickets)도 기존과 동일하게 정상 동작(양방향 저장 후 자산 측 회귀 없는지) — API-ITAM-003

### D. FE E2E 회귀
- **TC-E2E-REG-001** · 문제 상세(SCR-PRB-003)에 "연결 자산" 섹션이 추가되어 연계된 자산 표시, 연계 없는 경우 "연결 없음" 정상 렌더링 — problem 도메인 회귀
- **TC-E2E-REG-002** · 변경 상세(SCR-CHG-003)에 연결 자산 표시(양방향: 자산 상세에서도 해당 변경 확인) — change 도메인 회귀
- **TC-E2E-REG-003** · 서비스 요청 상세(SCR-SRM-005)에 "연결 자산" 섹션 추가·정상 렌더링 — SRM 도메인 회귀
