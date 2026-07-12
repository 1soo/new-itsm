# 통합 테스트 시나리오 — Infra Monitoring (IT 인프라 모니터링 & 용량관리, IOM) 재테스트

> 이전 실행(`20260710-234456`)에서 발견된 FE 이슈 수정분 재검증. 전체 12건 중 아래 2건만 재수행하고, 나머지는 이전 실행 결과를 유지한다.

## 사전 조건
- frontend http://localhost:5173, backend http://localhost:8080, docker `itsm-postgres` 기동 중
- io@itsm.local(INFRA_OPERATOR) 로그인, 새 브라우저 컨텍스트(storage 초기화)
- 시드 데이터: 용량 계획 5건(활용률 65%/96%/120%/150%/150%), AST-0002(assetId=6) 가동률 목표 95% 설정됨(직전 실행에서 설정)

## 시나리오

### TC-IOM-010(재검증) · SCR-IOM-004/005 활용률 표시 스케일 수정 확인
- 근거: @docs/02_plan/screen/infra-monitoring.md (SCR-IOM-004 "활용률 100% 초과 시 Danger 강조", SCR-IOM-005 KPI 카드)
- 전제: io@itsm.local 로그인
- 절차:
  1. 용량 계획 관리 화면에서 활용률 배지 확인(65%/96%/120%/150%/150%)
  2. 인프라 리포팅 화면에서 "평균 용량 활용률" KPI 확인
- 기대 결과: (1) 65%→success, 96%→warning, 120%·150%→danger 톤으로 정확한 값 표시 (2) avgCapacityUtilization*100 값(116%) 정상 표시

### TC-IOM-007(재검증) · SCR-IOM-002 SLA 카드 라벨 개선 확인
- 근거: @docs/01_analyze/feature/infra-monitoring.md (FEAT-IOM-005 Unwanted — 목표 미설정 시 실제 가동률만 표시), dev_lead-2 라벨 개선 요청
- 전제: io@itsm.local 로그인, AST-0001(assetId=5, 목표 99%, 실제 데이터 있음) / AST-0002(assetId=6, 목표 95% 설정됨, UPTIME 실제 데이터 없음)
- 절차:
  1. 지표 대시보드에서 assetId=5(가동률) 조회 → SLA 카드 라벨 확인
  2. assetId=6(가동률) 조회 → SLA 카드 라벨 확인(목표는 있으나 실제 데이터 없음)
- 기대 결과: (1) "달성"(met=true) 정상 표시 (2) 목표 미설정과 구분되는 라벨(예: "가동률 데이터 없음" 등)로 표시, "목표 미설정"으로 오인되지 않음
