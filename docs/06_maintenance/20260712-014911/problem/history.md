---
date: 20260712-014911
domain: problem
change_type: [new]
keywords: [승인 게이트, 상태전이, 강제종료 제외]
---

# 유지보수 이력 — problem

> 유지보수 일시: 20260712-014911 · 도메인: problem

## 1. 요구사항

WORKAROUND→RESOLVED_CLOSED 상태전이에 승인 게이트가 걸려야 한다.
강제종료(API-PRB-012)는 게이트 대상에서 제외해야 한다(설계 확정).

## 2. 해결 방법

`ProblemService`의 WORKAROUND→RESOLVED_CLOSED 전이에 게이트 체크를 연동했다.
`ProblemApprovalTicketSummaryProvider`를 신규 구현했다.
강제종료(API-PRB-012)는 설계 확정에 따라 게이트 대상에서 명시적으로 제외했다.

## 3. 변경 파일

- `source/backend/.../problem/application/ProblemService.java`
- `source/backend/.../problem/application/ProblemApprovalTicketSummaryProvider.java`(신규)
- `source/backend/.../problem/application/dto/ProblemDetailResponse.java`
- `source/backend/.../problem/presentation/ProblemController.java`
- `test/.../problem/application/ProblemServiceTest.java`
- `source/frontend/src/features/problem/{CLAUDE.md,ProblemDetailPage.tsx,types.ts}`

## 4. 테스트 결과

INCIDENT+PROBLEM 통합 테스트 19건과 재테스트 6건 전부 PASS했다(강제종료 게이트 제외 확인 포함).
커밋 `a03225f`로 반영했다.
