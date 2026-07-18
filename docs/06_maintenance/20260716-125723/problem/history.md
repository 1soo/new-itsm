---
date: 20260716-125723
domain: problem
change_type: [modified]
keywords: [상태전이 버튼 라벨, 동사형 문구]
---

# 유지보수 이력 — problem

> 유지보수 일시: 20260716-125723 · 도메인: problem

## 1. 요구사항

상태 변경 버튼의 문구가 도착 상태명으로만 표시되어 있어, 이를 실제 수행하는 행위에 맞는 동사형 문구로 변경해야 한다.

## 2. 해결 방법

`features/problem/status.ts`에 `transitionLabel()`을 신규 추가해 상태 전이 버튼 문구를 도착 상태명 대신 동작 동사형으로 전환했다.
`ProblemDetailPage.tsx`의 전이 버튼 라벨을 `transitionLabel()`로 교체했다.
상태 변경 완료 토스트 문구는 기존 `statusLabel()`을 그대로 유지했다.
이 도메인은 타임라인(Timeline) 컴포넌트를 사용하지 않아 코드→이름·행위주체자 표시 요구사항은 대상이 아니다.

## 3. 변경 파일

- `source/frontend/src/features/problem/status.ts`
- `source/frontend/src/features/problem/ProblemDetailPage.tsx`

## 4. 테스트 결과

통합 테스트 결과는 `docs/04_test/20260716-080731`에 기록되어 있으며 PASS했다.
커밋 `1759a25`로 반영했다.
