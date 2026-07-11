# 유지보수 이력 — esm

> 유지보수 일시: 20260712-020000 · 도메인: esm

## 1. 요구사항

부서요청 상태 전이(API-ESM-008, targetStatus=COMPLETED)에 승인 게이트가 걸려야 한다.
HR 케이스·체크리스트 하위 작업(API-ESM-016)은 게이트 대상에서 제외해야 한다(설계 확정).

## 2. 해결 방법

`EsmRequestService`의 부서요청 상태 전이(API-ESM-008)에 게이트 체크를 연동했다.
`EsmRequestApprovalTicketSummaryProvider`를 신규 구현했다.
HR 케이스·체크리스트 하위 작업(API-ESM-016)은 설계 확정에 따라 게이트 대상에서 명시적으로 제외했다.
부수적으로 공용 승인 대기함(SCR-COM-014)에서 CORRECTIVE_ACTION/ESM_REQUEST 티켓 유형의 라벨 매핑이 없어 raw enum이 노출되던 문제를 발견해, 공용 티켓 유형 라벨(status.ts/types.ts)에 매핑을 보완했다(도메인 필터 자체에는 영향 없었음).

## 3. 변경 파일

- `source/backend/.../esm/application/{CLAUDE.md,EsmRequestService.java}`
- `source/backend/.../esm/application/EsmRequestApprovalTicketSummaryProvider.java`(신규)
- `source/backend/.../esm/application/dto/RequestDetailResponse.java`
- `source/backend/.../esm/presentation/EsmRequestController.java`
- `test/.../esm/application/EsmRequestServiceTest.java`
- `source/frontend/src/features/esm/{CLAUDE.md,EsmRequestDetailPage.tsx,types.ts}`
- `source/frontend/src/features/common/{status.ts,types.ts}`(라벨 매핑 보완)

## 4. 테스트 결과

COMPLIANCE+ESM 통합 테스트 19건 전부 PASS했다(HR 케이스·체크리스트 게이트 제외 회귀 확인, 승인 대기함 필터·라벨 확인 포함).
커밋 `f421aa9`로 반영했다.
