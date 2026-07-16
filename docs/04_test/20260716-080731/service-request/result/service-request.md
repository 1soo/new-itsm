---
date: 20260716-080731
domain: service-request
result: pass
keywords: [상태전이 버튼 라벨 동사형, 타임라인 actor 표시]
---

# 통합 테스트 결과 — service-request (20260716-080731)

## 요약
- 총 4건 · 성공 4 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-SRM-001 | PASS | 백엔드 `./gradlew build` BUILD SUCCESSFUL(2m 2s), 프론트엔드 `npm run build`(tsc+vite) 성공(2136 modules) | - |
| TC-SRM-002 | PASS | SRM-2026-0014(노트북 신청) 생성 후 agent@itsm.local로 전체 흐름 수행. 각 단계 버튼 텍스트: SUBMITTED→VALIDATED "검증 완료", VALIDATED→ROUTED "라우팅 처리"(담당자 배정 전 비활성화 확인 후 배정→활성화), ROUTED→IN_FULFILLMENT "이행 시작"(승인 게이트 있어 admin@itsm.local 승인 처리 후 재시도), IN_FULFILLMENT→FULFILLED "이행 완료 처리", FULFILLED→CLOSED "종료 처리" — 표와 정확히 일치 | playwright snapshot |
| TC-SRM-003 | PASS | 검증 완료 전이 시 토스트 "상태가 '검증됨'로 변경되었습니다", 이행 시작 전이 시 토스트 "상태가 '이행 중'로 변경되었습니다", 이행 완료 처리 시 토스트 "상태가 '이행 완료'로 변경되었습니다" — 버튼 라벨(동작 동사형)과 다르게 기존 도착 상태명 유지 확인 | playwright snapshot(alert) |
| TC-SRM-004 | PASS | 타임라인 전체 항목에 actor 표시: "서비스데스크 담당자"(agent 로그인 계정) 각 상태변경 항목에 표시. 메시지도 코드 대신 한글 라벨 사용("상태가 검증됨로 변경되었습니다", "상태가 라우팅됨로 변경되었습니다", "상태가 이행 중로 변경되었습니다", "상태가 이행 완료로 변경되었습니다", "상태가 종료로 변경되었습니다") — VALIDATED/ROUTED 등 enum 코드 노출 없음 | playwright snapshot |

## 실패 항목 분석
- 없음
