---
date: 20260715-064012
domain: infra-monitoring
result: pass
keywords: [지표 조회 폼, 지표 등록, 토큰 재발급]
---

# 통합 테스트 결과 — infra-monitoring (20260715-064012)

## 요약
- 총 2건 · 성공 2 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-IOM-101 | PASS | `/infra/metrics` 접근 시 자산 ID/지표 항목(가동률 기본값)/시작일/종료일 조회 폼 정상 렌더링 | snapshot 확인 |
| TC-IOM-102 | PASS | 자산 ID 1, 지표 항목 "CPU", 값 55.5 입력 후 등록 → "지표가 등록되었습니다" 알림 정상 표시, 폼 초기화 확인 | snapshot 확인 |

## 실패 항목 분석
- 없음 (실패 0건)

## 참고 사항 (실패는 아니나 발견한 사항)
- 이번 라운드 접속 중 `/infra/metrics` 진입 시점에 `/api/v1/notifications/dismissals` 401이 1건 관찰되었으나, 토큰 재발급 주기(감사 로그 근거, `docs/04_test/20260715-064012/auth/result/auth.md` TC-AUTH-102)에 따른 정상적인 자동 재시도 해소로 확인되어 실패로 집계하지 않음.
