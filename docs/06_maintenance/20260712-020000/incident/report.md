# 유지보수 이력 — incident

> 유지보수 일시: 20260712-020000 · 도메인: incident

## 1. 요구사항

IN_PROGRESS→RESOLVED 상태전이(API-INC-005)와 별도의 해결 처리 엔드포인트(API-INC-009 resolve) 두 진입점 모두에 승인 게이트가 걸려야 한다.
어느 진입점을 사용하더라도 게이트를 우회할 수 없어야 한다.

## 2. 해결 방법

`IncidentService`의 범용 상태전이(API-INC-005)와 별도 resolve 엔드포인트(API-INC-009) 양쪽 모두에 게이트 체크를 연동했다.
`IncidentApprovalTicketSummaryProvider`를 신규 구현했다.
최초 구현 시 resolve 엔드포인트가 범용 상태전이 API와 별개로 게이트를 완전히 우회하는 결함이 있었음을 발견했다.
designer 확인 후 두 진입점 모두 게이트를 걸도록 보완하고, 재테스트로 우회가 차단됨을 확인했다.

## 3. 변경 파일

- `docs/02_plan/api_spec/incident.md`
- `source/backend/.../incident/application/{CLAUDE.md,IncidentService.java}`
- `source/backend/.../incident/application/IncidentApprovalTicketSummaryProvider.java`(신규)
- `source/backend/.../incident/application/dto/IncidentDetailResponse.java`
- `source/backend/.../incident/presentation/IncidentController.java`
- `test/.../incident/application/IncidentServiceTest.java`
- `source/frontend/src/features/incident/{CLAUDE.md,IncidentDetailPage.tsx,types.ts}`

## 4. 테스트 결과

INCIDENT+PROBLEM 통합 테스트 19건과 재테스트 6건 전부 PASS했다.
resolve(API-INC-009) 엔드포인트가 게이트를 우회하던 결함을 수정 후 재테스트로 우회가 차단됨을 확인했다.
커밋 `a03225f`로 반영했다.
