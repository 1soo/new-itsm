---
date: 20260712-175603
domain: infra-monitoring
result: pass
keywords: [다국어(i18n) 전환, 지표 등록/대시보드, 임계치 알림, 용량 계획/리포팅, 한국어 재전환]
---

# 통합 테스트 결과 — infra-monitoring (다국어(i18n) 텍스트 전환, 유지보수 요청 2026-07-12, 마지막 도메인)

- 시나리오: `docs/04_test/20260712-175603/infra-monitoring/scenario.md`
- 테스트 계정: `io@itsm.local`(INFRA_OPERATOR)
- 브라우저: Playwright(새 context, storage 초기화)

## 결과 요약

| TC | 결과 | 비고 |
|----|------|------|
| TC-BUILD-001 | PASS | backend `./gradlew build -x test` BUILD SUCCESSFUL, frontend `npm run build` 성공(사전 실행) |
| TC-IOM-I18N-001 | PASS | 지표 등록(SCR-IOM-001) 폼 라벨(Asset ID/Metric/Value/Measured At)·등록 버튼 영어 전환. 지표 항목 4종(Uptime/CPU/Memory/Response Time) 전부 전환, "Value (%)"처럼 단위 기호가 라벨에 자동 부착되되 기호 자체는 번역 대상 아님을 확인. 필수 미입력 오류 영어 전환. CPU 임계치(90) 초과 값(95) 등록 시 "A threshold exceeded alert was generated" 토스트 영어 전환(회귀 없음) |
| TC-IOM-I18N-002 | PASS | 지표 대시보드(SCR-IOM-002) 자산/지표/기간 필터, "{Metric} Time Series" 차트(축 라벨 %, 날짜 ko-KR 유지), "Uptime vs. SLA" 카드(Target/Actual/Met, 미설정 시 "Target not set"/"No uptime data" falsy 가드 정상, 목표 설정("Uptime target set" 토스트) 후 "99.5%"/"No uptime data" 정상 갱신) 전부 영어 전환, 회귀 없음 |
| TC-IOM-I18N-003 | PASS | 임계치 설정·알림 목록(SCR-IOM-003) 설정 폼(지표 항목 선택 시 기존 값 자동 반영, 상한/하한/저장)·안내 문구 영어 전환. 알림 목록 표 헤더·초과 유형 라벨("Above Upper Limit", 원시값 노출 없음)·확인 여부("Unacknowledged"/"Acknowledged", 원시 불리언 노출 없음) 필터(Acknowledged 2종) 전부 영어 전환. "Acknowledge" 클릭 시 "Alert acknowledged" 토스트, 확인 후 상태·행 갱신 정상(회귀 없음) |
| TC-IOM-I18N-004 | PASS | 용량 계획 관리(SCR-IOM-004) 등록 폼(Team/Service Name/Capacity/Expected Demand)·목록 표 헤더(Utilization 포함) 영어 전환. 필수 미입력 시 "Team/service name, capacity, and expected demand are required." 오류 정상. 활용률 150%(100% 초과) 등록 시 "Capacity plan registered" 토스트, 수치 정상 표시(회귀 없음) |
| TC-IOM-I18N-005 | PASS | 인프라 지표 리포팅(SCR-IOM-005) 필터(From/To/Asset ID)·KPI 카드 5종(Avg. Uptime/CPU/Memory/Response Time/Capacity Utilization) 전부 영어 전환, 단위(%, ms) 정상 유지 |
| TC-IOM-FORMAT-REG-001 | PASS | English 상태에서도 알림 목록의 "Occurred At" 값이 `2026. 7. 12. 오후 6:00:10`(ko-KR 로케일 포맷) 그대로 유지, 영어 로케일 포맷으로 바뀌지 않음 확인 |
| TC-IOM-KO-ROUNDTRIP-001 | PASS | 지구본 아이콘으로 한국어 재전환 시 새로고침 없이 즉시 복귀. 임계치·알림 화면 필터·표 헤더·데이터(상한 초과/확인됨) 전부 정상, 누락·깨짐 없음 |

## 결함 분석

없음. 단위 기호(%, ms)는 설계대로 번역 대상에서 제외되어 있음을 확인(`metricTypeUnit`). 임계치 초과 알림의 확인/미확인 상태·초과 유형이 원시값 노출 없이 정상 라벨로 표시됨을 확인(dev-lead 우려 사항 해소).

## 스크린샷/증적

- 별도 스크린샷 저장 없이 Playwright accessibility snapshot으로 각 화면의 텍스트 상태를 확인함(토스트 메시지는 `browser_run_code_unsafe`로 `.swal2-toast` 텍스트를 즉시 캡처).
- 테스트 중 실제 생성한 데이터: AST-0001의 CPU 지표 95%(임계치 90 초과, 알림 발생 후 확인 처리), 자산 1의 Uptime SLA 목표 99.5% 설정, 신규 용량 계획("i18n test team capacity", Capacity 100/Demand 150, 활용률 150%)

## 종합 판정

infra-monitoring 도메인 i18n 전환 **전 항목 PASS**. 지표 등록/대시보드(시계열·SLA)/임계치 설정·알림(확인 처리)/용량 계획/리포팅/날짜 포맷(ko-KR 유지)/한국어 재전환 모두 정상 확인. 결함 없음.

**이번 유지보수 요청(i18n/SweetAlert2 전 도메인 다국어 전환)의 마지막 도메인이며, 전 12개 도메인(common/auth/service-request/incident/problem/change/knowledge/asset/esm/vulnerability/compliance/infra-monitoring) 통합 테스트가 모두 완료되었다.**
