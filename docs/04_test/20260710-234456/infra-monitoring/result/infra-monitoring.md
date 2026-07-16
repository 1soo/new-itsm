---
date: 20260710-234456
domain: infra-monitoring
result: pass
keywords: [지표 등록, 임계치 알림, 용량 계획, SLA 가동률, 리포팅 집계]
---

# 통합 테스트 결과 — Infra Monitoring (IT 인프라 모니터링 & 용량관리, IOM) (20260710-234456)

## 요약
- 총 12건 · 성공 12 · 실패 0
- (재테스트 2026-07-10) TC-IOM-010 수정 확인 후 PASS로 갱신. 나머지 11건은 최초 실행에서 이미 PASS.

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-IOM-BUILD-001 | PASS | frontend `npm run build` 성공(타입/컴파일 오류 없음), backend `gradlew.bat build -x test` 성공, backend `gradlew.bat test --tests "com.itsm.infra.*"`(JUnit 통합테스트) 성공 | - |
| TC-IOM-001 | PASS | (1) assetId 누락 400 `VALIDATION_ERROR`("assetId: 널이어서는 안됩니다") (2) value 누락 400(동일 코드) (3) 존재하지 않는 자산(999999) 404 `ASSET_NOT_FOUND` (4) assetId=5·MEMORY·value=50 등록 시 201 | - |
| TC-IOM-002 | PASS | assetId=5·CPU·value=95(임계치 upperLimit=90 초과) 등록 시 201 `alertGenerated:true`, `GET /metric-alerts?assetId=5` 목록에 신규 알림(threshold_type=UPPER) 즉시 반영 | - |
| TC-IOM-003 | PASS | assetId=5·MEMORY·value=99999(MEMORY 임계치 미설정) 등록 시 201 `alertGenerated:false`(비교 자체 생략) | - |
| TC-IOM-004 | PASS | assetId=5 조회 시 측정 시각 오름차순 정렬된 지표 목록(CPU/UPTIME/MEMORY 혼합) 반환, 데이터 없는 미래 기간(2030-01)은 빈 배열 `[]` | - |
| TC-IOM-005 | PASS | 임계치 목록에 기존 CPU(90)/RESPONSE_TIME(500) 확인, MEMORY upperLimit=85 신규 설정(PUT) 200, 재조회 시 3건으로 반영. 정의되지 않은 지표 항목(DISK)으로 설정 시도 시 400 `VALIDATION_ERROR`("입력 형식이 올바르지 않습니다", enum 역직렬화 실패로 처리) | - |
| TC-IOM-006 | PASS | assetId/acknowledged 필터 정상 축소. 알림 id=4 확인 처리(PATCH) 200, acknowledged=true 전환. 존재하지 않는 id(999999) 확인 처리 시 404 `INFRA_METRIC_ALERT_NOT_FOUND`. FE 알림 목록에서 확인된 알림 행이 회색으로 흐리게 표시되고 "확인 처리" 버튼이 사라짐(스크린샷 확인) | infra-threshold-alerts.png |
| TC-IOM-007 | PASS | AST-0001(assetId=5, 목표 99%): targetPercentage=99/actualPercentage=99.03/met=true 정상 산출. AST-0002(assetId=6, 목표 미설정): `{"assetKey":"AST-0002"}`만 반환(target/actual/met 모두 null → 프로젝트 공통 Jackson `non_null` 설정으로 필드 생략, 기존 도메인과 동일 컨벤션). 목표 95% 신규 설정(PUT) 후 재조회 시 targetPercentage=95 반영(실제 가동률 데이터 없어 actualPercentage/met은 여전히 null). 존재하지 않는 자산(999999) 목표 설정 시 404 `ASSET_NOT_FOUND` | FE SLA 카드에서 목표 95% 설정 후에도 "달성 여부" 배지가 "목표 미설정"으로 표시됨(실제로는 목표가 설정되어 있고 실제 가동률 데이터가 없어 met=null인 상태). API 응답은 올바르나 FE 라벨이 "목표 미설정"과 "실제값 없음"을 구분하지 않아 다소 오해 소지 있음(경미, `InfraMetricDashboardPage.tsx:161`) |
| TC-IOM-008 | PASS | (1) capacity 누락 400 (2) demand 누락 400 (3) teamOrService="QA Test Env"(ASCII, 한글 문자열은 로컬 curl/bash 셸 인코딩 문제로 최초 시도 400 오탐 발생 → UTF-8 파일 기반 재요청 시 정상 201 확인, 백엔드 결함 아님) capacity=100·demand=150 등록 201 (4) 목록 조회 시 utilizationRate=1.5 정상 계산, 기존 120%(0.2/1.2) 케이스도 정확한 값 | - |
| TC-IOM-009 | PASS | (1) 전체 리포팅 조회 시 avgUptime/avgCpu/avgMemory/avgResponseTime/avgCapacityUtilization 모두 시드+등록 데이터 기반 정상 집계(avgCapacityUtilization은 설계대로 기간 필터와 무관하게 전체 capacity_plan 평균) (2) assetId=5 필터링 시 해당 자산 지표만 반영된 평균 산출 (3) 데이터 없는 미래 기간은 avgUptime/avgCpu/avgMemory/avgResponseTime 모두 0, avgCapacityUtilization은 설계상 전체 평균 유지(오류 없음) | - |
| TC-IOM-010 | PASS(재테스트) | SCR-IOM-001(등록+임계치 초과 토스트 정상), SCR-IOM-002(시계열 차트·SLA 카드 정상), SCR-IOM-003(임계치 설정+확인 처리+흐리게 표시 정상)는 최초 실행부터 PASS. 최초 실행에서 SCR-IOM-004/005 활용률 표시 오류(FAIL, 아래 실패 항목 분석 참고) 발견 후 dev_fe 수정. 재테스트: `InfraCapacityPlanPage.tsx`/`InfraReportPage.tsx` `*100` 반영 확인 — 용량 계획 목록 활용률 배지 65%(success)/96%(warning)/120%·150%(danger) 정상 표시, 리포팅 "평균 용량 활용률" 116%(=avgCapacityUtilization 1.16*100) 정상 표시. 100% 초과 Danger 강조 정상 동작 확인(오류 재현 안 됨) | infra-capacity-plans-retest.png |
| TC-IOM-011 | PASS | am@itsm.local(ASSET_MANAGER) 로그인(새 컨텍스트, storage 초기화) 사이드바에 인프라모니터링 메뉴 미노출(자산/CI·CMDB/자산 지표만 노출), `GET /api/v1/infra/metrics` 등 IOM 전 API 403 확인(metrics/metric-thresholds/metric-alerts/capacity-plans/metrics/report), `/infra/metrics/new` 화면 직접 진입 시 `/403` 리다이렉트. 인증 헤더 없는 요청은 401 | - |

## 실패 항목 분석
- 없음(TC-IOM-010 수정 확인 완료). 최초 실행에서 발견된 원인: `utilizationRate`(0~1 소수, API 계약대로 조회 시점 계산값)를 FE가 백분율로 변환하지 않고 그대로 반올림·톤 판정에 사용해, 실제 65%/96%/120%/150%가 각각 "1%/1%/1%/2%"로 오표시되고 100% 초과 Danger 강조(SCR-IOM-004 인수 기준)가 동작하지 않았음. `InfraCapacityPlanPage.tsx:71`/`InfraReportPage.tsx:74`에 `*100` 반영으로 수정 확인.

## 참고(FAIL 아님)
- TC-IOM-007 FE SLA 카드 라벨: 목표가 설정되어 있으나 실제 가동률 데이터가 없어 met=null인 경우에도 "목표 미설정"으로 표시되어 다소 오해 소지 있음(`InfraMetricDashboardPage.tsx:161`). API 계약(met=null) 자체는 정상이라 경미한 UX 이슈로만 기록.
- 리포팅 화면 "평균 메모리" 이상치(33372%) 관련: dev_fe가 발견해 dev_be에 확인 요청한 건과 동일 현상으로 보이며, tester가 원인 추적한 결과 **백엔드/프론트엔드 결함이 아니라 tester가 TC-IOM-003(임계치 미설정 항목 알림 미생성 검증) 수행 중 의도적으로 등록한 비정상적으로 큰 테스트값(assetId=5, MEMORY, value=99999, id=12)이 원인**. `GET /api/v1/infra/metrics?metricType=MEMORY` 조회 결과 실제 데이터는 68.40/50.00/99999.00 3건뿐이며 평균 계산(33372.47)이 수식대로 정확히 일치함. 집계 로직 자체는 정상이므로 별도 수정 불필요.
