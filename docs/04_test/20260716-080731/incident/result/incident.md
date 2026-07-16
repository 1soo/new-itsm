---
date: 20260716-080731
domain: incident
result: pass
keywords: [상태전이버튼라벨, 타임라인actor표시, 상태라벨]
---

# 통합 테스트 결과 — incident (20260716-080731)

## 요약
- 총 4건 · 성공 4 · 실패 0

## 상세

| TC ID | 결과 | 실제 동작 | 비고(스크린샷/로그) |
|-------|------|-----------|----------------------|
| TC-INC-001 | PASS | service-request TC-SRM-001과 동일 빌드 결과 공유(BUILD SUCCESSFUL) | - |
| TC-INC-002 | PASS | INC-2026-0002(SEV3) 등록 후 agent@itsm.local→im@itsm.local로 전체 흐름 수행. NEW→IN_PROGRESS "대응 시작", IN_PROGRESS→RESOLVED "해결 처리"(agent는 403으로 차단, im@itsm.local로 시간지표 입력 후 처리), RESOLVED→CLOSED "종료 처리" — 표와 정확히 일치 | playwright snapshot |
| TC-INC-003 | PASS | 대응 시작 전이 토스트 "상태가 '대응중'로 변경되었습니다" — 버튼 라벨과 다르게 기존 도착 상태명 유지 확인 | playwright snapshot(alert) |
| TC-INC-004 | PASS | 상태 변경 타임라인(대응중/종료)에 actor "서비스데스크 담당자"/"인시던트 매니저" 표시 + 메시지가 한글 라벨("상태가 대응중로 변경되었습니다", "상태가 종료로 변경되었습니다", 코드 노출 없음). 별도 상태 업데이트(자유 텍스트) 항목 "고객사 재확인 중입니다."와 RESOLVE 전용 이벤트 "인시던트가 해결되었습니다."는 기존과 동일하게 그대로(문구 변경 없음), 다만 actor 필드는 두 항목 모두 신규로 표시됨(BE가 모든 타임라인 항목에 공통으로 actor를 채우도록 구현되어 있어 회귀 아님 — 메시지 문구만 STATUS_ 이벤트에서 라벨로 교체되고 다른 이벤트 유형 문구는 불변이라는 설계 의도에 부합) | playwright snapshot |

## 실패 항목 분석
- 없음
