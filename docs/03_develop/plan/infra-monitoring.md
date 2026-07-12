# 개발 계획 — infra-monitoring (IT 인프라 모니터링 & 용량관리, IOM)

> 도메인: infra-monitoring · 개발 순서 4/4(확장 도메인, 마지막) · 작성: dev-lead · 2026-07-10

## 1. 목표

인프라 자산(HW/네트워크) 가동률·성능 지표 수동 등록·시계열 조회, 지표 항목별(전역) 임계치 설정·초과 알림, 자산별 SLA 대비 가동률 비교, 팀/서비스 용량 계획, 인프라 지표 리포팅을 구현한다. asset(자산 참조) 기반. 단일 역할(INFRA_OPERATOR) 도메인이며, 실시간 수집/에이전트 연동 없이 **수동 입력 기반**으로 축소된 범위다(`docs/01_analyze/tech.md` 5절 확인됨). 이 도메인이 이번 세션의 **마지막(4/4)** 도메인이다.

## 2. 설계 근거 (docs/02_plan)

- 화면: `screen/infra-monitoring.md`(SCR-IOM-001~005), 공통 SCR-COM-007/008(단, 이 도메인은 티켓형이 아니라 순수 데이터 등록/집계 화면이라 공통 목록/상세 패턴을 그대로 쓰기보다 폼+차트+표 조합이 많음)
- API: `api_spec/infra-monitoring.md`(API-IOM-001~011) — 0절 설계 배경(임계치는 지표 항목 전역 단위, 가동률/활용률은 조회 시점 계산값) 필독
- DB: `database/infra-monitoring.md`(infra_metric/infra_metric_threshold/infra_metric_alert/uptime_target/capacity_plan) + asset.md(asset 참조)
- 역할: `security/authorization/infra_operator.md`(INFRA_OPERATOR, 신규 단일 역할)

## 3. 담당별 범위

### DB (dev-database) — `source/db/`
- 신규 테이블 5개: `infra_metric`(asset_id FK NOT NULL, metric_type UPTIME/CPU/MEMORY/RESPONSE_TIME, value NUMERIC(10,2), measured_at TIMESTAMPTZ NOT NULL), `infra_metric_threshold`(metric_type UNIQUE, upper_limit/lower_limit NULL 허용), `infra_metric_alert`(metric_id FK, asset_id FK 비정규화, metric_type 비정규화, breached_value, threshold_type UPPER/LOWER, acknowledged DEFAULT false), `uptime_target`(asset_id FK UNIQUE, target_percentage), `capacity_plan`(team_or_service, capacity, demand).
- (asset_id, metric_type, measured_at) 조회 인덱스 권장(시계열·가동률 평균 계산용).
- 신규 역할 시드: `INFRA_OPERATOR`. screen/screen_role: SCR-IOM-001~005 등록.
- 테스트 유저: INFRA_OPERATOR 계정 1개 이상(예: io@itsm.local / Admin@1234).
- 시드 데이터: 기존 자산(asset) 중 2~3건을 대상으로 UPTIME/CPU/MEMORY/RESPONSE_TIME 지표를 여러 시점(measured_at 다르게)으로 몇 건씩 등록, 그중 일부는 임계치 초과 값(알림 생성 확인용) 포함. 임계치 1~2개 항목 사전 설정, 자산 1건 이상 가동률 목표(uptime_target) 설정, 용량 계획(capacity_plan) 2~3건(활용률 100% 초과 케이스 1건 포함) 시드 권장.

### BE (dev-backend) — `source/backend/`
- API-IOM-001~011(api_spec 기준). `infra` 또는 `iommonitoring` 패키지 신설(패키지명은 `com.itsm.infra`로 통일 권장, vulnerability/compliance 패키지 컨벤션 재사용).
- 지표 등록(API-IOM-001): assetId 존재 검증(404), metricType/value 필수(400). 등록 시 해당 metricType의 전역 임계치(`infra_metric_threshold`) 조회 → upper/lower 초과 시 `infra_metric_alert` 생성(threshold_type UPPER/LOWER 판정), 응답의 `alertGenerated`에 반영. 임계치 미설정 항목은 비교 생략(알림 생성 안 함).
- 지표 시계열 조회(API-IOM-002): assetId/metricType/from/to 필터, 없으면 빈 배열.
- 임계치 목록/설정(API-IOM-003/004): 설정은 PUT(upsert, metric_type UNIQUE라 존재하면 갱신·없으면 생성), 정의되지 않은 metricType 400.
- 알림 목록/확인처리(API-IOM-005/006): acknowledged 필터, 확인 처리는 idempotent하게 처리(이미 확인된 것도 200 허용 또는 404만 존재하지 않는 id에 대해 — 설계에 409 등 명시 없으니 단순 처리).
- 자산 가동률 목표 설정/현황(API-IOM-007/008): 목표는 UPSERT(자산당 1:1), 현황 조회는 해당 자산의 `metricType='UPTIME'` 레코드 평균을 **조회 시점 계산**(저장 안 함), 목표 없으면 met=null.
- 용량 계획 등록/목록(API-IOM-009/010): capacity·demand 필수(400), utilizationRate=demand/capacity **조회 시점 계산**(0으로 나누기 방지 — capacity 0은 애초에 400으로 막힘, 이미 필수값 검증됨).
- 리포팅(API-IOM-011): avgUptime/avgCpu/avgMemory/avgResponseTime(각 metricType 평균, 기간·자산 필터 적용) + avgCapacityUtilization(전체 capacity_plan 평균 활용률). 데이터 없으면 빈 결과(0 또는 null, problem/asset 등 기존 패턴처럼 값 없으면 0으로 통일 권장).
- RBAC: infra_operator.md 기준 — INFRA_OPERATOR만 전 API 접근, 그 외 403.
- JUnit 통합테스트: 임계치 초과/미초과 알림 생성 여부, 임계치 미설정 시 알림 미생성, 가동률 계산(목표 있음/없음), 용량 활용률 계산, 리포팅 집계, RBAC 포함.
- 컨벤션대로 완료 보고 전 로컬 백엔드 재기동+curl 자체 검증 부탁드립니다.

### UI (dev-ui) — `source/frontend/` 공통 영역
- **이번 도메인의 핵심 검토 대상**: SCR-IOM-002(지표 대시보드)의 "시계열 차트" — 프론트엔드에 차트 라이브러리(recharts/chart.js 등)가 전혀 설치되어 있지 않습니다(확인 완료). asset 도메인의 CI 관계 뷰가 그래프 대신 리스트/트리로 최소 구현했던 선례와 동일하게, **신규 차트 라이브러리 도입보다 단순 표(시각·값 목록) 또는 최소한의 CSS/SVG 기반 스파크라인 정도로 축소 구현을 권장**합니다. 다만 최종 판단은 dev-ui가 검토 후 dev_fe와 협의해 결정해주세요(정말 필요하면 가벼운 라이브러리 도입도 가능하나, 신규 의존성 추가는 최소화하는 기존 방향과 일치시켜주세요).
- SCR-IOM-004의 활용률 배지는 기존 StatusBadge로 충분.

### FE (dev-frontend) — `source/frontend/` 기능 영역
- 신규 기능 모듈 `features/infra-monitoring/`(api.ts/types.ts/status.ts/format.ts + 5개 Page).
- SCR-IOM-001 지표 등록(자산 선택은 숫자ID 입력 — 기존 change/vulnerability의 LinkCard 패턴 재사용, 지표 항목 셀렉트, 값 입력, 등록 시 임계치 초과 알림 생성되면 토스트 안내) / SCR-IOM-002 지표 대시보드(자산·기간 선택, 시계열 표시는 dev-ui 검토 결과에 따름, SLA 대비 가동률 카드) / SCR-IOM-003 임계치 설정·알림 목록(설정 폼+알림 표+확인처리 버튼) / SCR-IOM-004 용량 계획 관리(등록 폼+목록+활용률 배지) / SCR-IOM-005 인프라 지표 리포팅(기간·자산 필터+KPI 카드).
- 역할 상수 `ROLE_INFRA_OPERATOR` 추가, `navConfig.tsx`에 인프라모니터링 메뉴 그룹(INFRA_OPERATOR 전용) 추가, `routes/index.tsx`에 라우팅 5개 추가.

## 4. 진행 순서 · 의존성
1. DB(테이블 5개+역할/유저/시드) → BE(infra 패키지) → FE 연동. UI는 차트 방식 검토가 선행되어야 FE가 SCR-IOM-002를 확정 구현 가능(먼저 결론 내주시면 좋겠습니다).
2. 계약 단일 기준 `api_spec/infra-monitoring.md`. 모호점은 저에게 질문 → 제가 판단 못 하면 designer에게 확인.
3. **이번이 마지막 도메인입니다.** 개발+테스트+커밋·푸시 완료 후 전체 완료를 Main에게 보고합니다.

## 5. 완료(테스트 통과) 기준
- BE: API-IOM-001~011 정상+오류(400/401/404), 임계치 초과/미초과 알림 생성 로직, 가동률·용량활용률 조회시점 계산, 리포팅 집계.
- FE: 지표 등록(임계치 초과 토스트 확인)→대시보드(시계열/SLA)→임계치 설정+알림 확인처리→용량계획 등록/조회→리포팅 E2E. INFRA_OPERATOR RBAC 확인(타 역할 403+사이드바 미노출).
- `tester` 통합테스트 실패 0 → `feat(infra-monitoring): ...` 커밋/푸시.
- 테스트 산출물: `docs/04_test/infra-monitoring/{timestamp}/{scenario,result}/`.

## 6. 파일 소유
- `source/db/` DB / `source/backend/`(infra 패키지) BE / `source/frontend/` 공통 UI·기능 FE.

## 7. 특이사항
- 실시간 모니터링 도구 연동은 범위 밖(analyzer/designer 단계에서 이미 수동입력·더미데이터 기반으로 축소 확정됨, `docs/01_analyze/tech.md` 5절 참고). 자동 수집 기능을 임의로 추가 구현하지 않는다.
- 시계열 차트 구현 방식(신규 라이브러리 vs 표/스파크라인)은 dev-ui 검토 결과를 최우선으로 따른다.

## i18n 다국어 전환 (유지보수 요청, 2026-07-12) — 마지막 도메인

> i18n 인프라·SweetAlert2·언어 선택은 common phase에서 완료됨(`docs/03_develop/plan/common.md` v3절). 레이아웃/컴포넌트 변경 없이 텍스트만 번역 키로 치환(`docs/02_plan/screen/common.md` 6절). BE/DB 변경 없음. 이 도메인이 유지보수 요청의 **마지막 도메인**이다.

### 담당 범위 — dev-fe 단독(UI 미소집)

- 대상 화면(`docs/02_plan/screen/infra-monitoring.md` 3절): `InfraMetricRegisterPage.tsx`(SCR-IOM-001), `InfraMetricDashboardPage.tsx`(002), `InfraThresholdAlertPage.tsx`(003), `InfraCapacityPlanPage.tsx`(004), `InfraReportPage.tsx`(005).
- `features/infra-monitoring/status.ts` — `t` 인자를 받도록 전환, 호출부 갱신. 통합검색 대상 아님(`features/search/status.ts` 변경 불필요).
- `format.ts` 확인 필수 — 라벨 섞여 있으면 전환.
- `useTranslation(["infra-monitoring", "common"])` 사용. `locales/{ko,en}/infra-monitoring.json`(현재 `{}` 스캐폴딩) 단독 소유, 직접 채운다.
- 임계치 알림 상태, 지표 항목 유형, 용량 계획 상태 등에서 지금까지 반복된 원시값 노출 패턴 점검.

### 완료 기준
- English 전환 시 지표 등록/대시보드/임계치 설정·알림/용량 계획/리포팅 전체 텍스트(지표 항목·임계치 상태 라벨 포함) 영어 전환.
- 지표 등록·임계치 초과 알림·용량 계획 등 기존 기능 회귀 없음(텍스트만 치환).
- **모든 도메인(common + 11개) 완료** — 완료되면 dev-lead에게 알려라, 전체 완료 보고를 준비하겠다.
