# CLAUDE.md

인프라 모니터링·용량관리(IOM) 기능. 인프라 자산(HW/네트워크) 가동률·성능 지표 수동 등록·시계열 대시보드, 지표 항목별(전역) 임계치 설정·초과 알림, 자산별 SLA 대비 가동률 비교, 팀/서비스 용량 계획, 인프라 지표 리포팅 화면·API·타입, 지표/임계치/활용률 표시 매핑 제공. INFRA_OPERATOR 역할 기반. API 계약: infra-monitoring.md.

## 파일
- `api.ts` — IOM API 호출(`infraApi`: 지표 등록/시계열 조회, 임계치 목록/설정, 알림 목록/확인처리, 자산 가동률 목표 설정/현황 조회, 용량 계획 등록/목록, 리포팅).
- `types.ts` — IOM 도메인 타입(`MetricType`/`ThresholdType`, `MetricPoint`/`MetricThreshold`/`MetricAlert`/`UptimeStatus`/`CapacityPlan`/`InfraReport`, 입력·쿼리 타입 등).
- `status.ts` — 지표 항목 라벨(`metricTypeLabel(t, ...)`, `METRIC_TYPES`)/단위(`metricTypeUnit`, %·ms 기호라 번역 대상 아님), 임계치 초과 유형 라벨(`thresholdTypeLabel(t, ...)`), 용량 활용률/SLA 달성 여부 배지 tone 매핑.
- `format.ts` — 날짜·일시 표시 포맷터.
- `InfraMetricRegisterPage.tsx` — 인프라 지표 등록(SCR-IOM-001). 자산(숫자 ID)·지표 항목·값·측정 시각(선택) 입력. 임계치 초과로 알림 생성되면 토스트 안내.
- `InfraMetricDashboardPage.tsx` — 지표 대시보드(SCR-IOM-002). 자산·지표 항목·기간 선택 후 시계열 조회 + SLA 대비 가동률 카드(목표 가동률 설정 폼 포함, API-IOM-007 — 구성요소 표에 명시 안 됐으나 dev-lead 판단으로 SLA 카드 내 최소 UI 추가, ESM/COMP 선례와 동일 패턴). 시계열은 신규 차트 라이브러리 없이 기존 SVG 라인 차트(`components/common` TrendChart) 재사용(dev-ui 검토 결과).
- `InfraThresholdAlertPage.tsx` — 임계치 설정·알림 목록(SCR-IOM-003). 지표 항목 선택 시 현재 설정값 자동 반영되는 단일 폼(상한/하한, 항목 전역 단위)과 초과 알림 목록(자산·지표·초과값·발생시각·확인 처리, 확인된 알림은 흐리게 표시).
- `InfraCapacityPlanPage.tsx` — 용량 계획 관리(SCR-IOM-004). 팀/서비스·역량·예상 수요 등록 폼 + 목록(활용률 배지, 100% 초과 Danger/80%↑ Warning).
- `InfraReportPage.tsx` — 인프라 지표 리포팅(SCR-IOM-005). 기간·자산 필터 + KPI(평균 가동률·CPU·메모리·응답시간·용량 활용률).
