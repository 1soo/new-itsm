# 통합 테스트 시나리오 — infra-monitoring (런타임 업그레이드 후 기능 회귀)

## 사전 조건
- auth 도메인 로그인 성공 전제. 신규 기능 검증 아님(기존 동작 회귀 확인).
- asset 도메인 TC-ITAM-101에서 생성한 자산(id=1) 존재.

## 시나리오

### TC-IOM-101 · 지표 대시보드 화면 접근
- 근거: @docs/01_analyze/feature/infra-monitoring.md (SCR-IOM-002)
- 전제: 로그인 상태
- 절차:
  1. `/infra/metrics` 접근
- 기대 결과: 조회 조건(자산 ID/지표 항목/기간) 폼 정상 렌더링, 콘솔 에러 없음

### TC-IOM-102 · 인프라 지표 등록
- 근거: @docs/01_analyze/feature/infra-monitoring.md (SCR-IOM-001, metric_type CPU/MEMORY/UPTIME/RESPONSE_TIME)
- 전제: 자산 id=1 존재
- 절차:
  1. "인프라 지표 등록" → 자산 ID(1)/지표 항목(CPU)/값(55.5) 입력 → 등록
- 기대 결과: "지표가 등록되었습니다" 알림 표시, 콘솔 에러 없음
