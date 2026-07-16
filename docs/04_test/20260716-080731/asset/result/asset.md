---
date: 20260716-080731
domain: asset
result: pass
keywords: [상태전이 버튼 라벨, 생애주기 이력]
---

# 통합 테스트 결과 — asset (20260716-080731)

## 요약
- 총 5건 · 성공 5 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-ITAM-001 | PASS | service-request TC-SRM-001과 동일 빌드 결과 공유(BUILD SUCCESSFUL) | - |
| TC-ITAM-002 | PASS | AST-0002(하드웨어) 등록 후 am@itsm.local로 전체 흐름 수행. PROCUREMENT "구매 진행", OPERATION "운영 전환", MAINTENANCE "유지보수 전환" — 표와 정확히 일치. 초기 상태(PLANNING)의 "계획 수립" 라벨도 화면 전체에 일관되게 노출됨 | playwright snapshot |
| TC-ITAM-003 | PASS | "폐기" 버튼 텍스트가 전이 전후 모두 "폐기"로 동일(확인 다이얼로그 제목 "자산을 폐기하시겠습니까?"도 기존과 동일) — 변경 없음 확인 | playwright snapshot |
| TC-ITAM-004 | PASS | 구매 전이 시 토스트 "상태가 '구매'로 변경되었습니다" — 버튼 라벨("구매 진행")과 다르게 기존 도착 상태명 유지 확인 | playwright snapshot(alert) |
| TC-ITAM-005 | PASS | 자산 상세 좌측 "생애주기 이력" 항목에 상태 라벨(구매/운영/유지보수/폐기)과 시각만 표시(기존과 동일한 형태, actor 필드 추가 없음) — 이번 변경으로 인한 이력 표시 변경 없음 확인 | playwright snapshot |

## 실패 항목 분석
- 없음
