# 유지보수 이력 — asset

> 유지보수 일시: 20260712-020000 · 도메인: asset

## 1. 요구사항

생애주기 전이(API-ITAM-005, targetStage=RETIREMENT)와 자산 폐기 전용 엔드포인트(API-ITAM-006 /retire) 두 엔드포인트 모두에 승인 게이트가 걸려야 한다.

## 2. 해결 방법

INCIDENT 도메인에서 발생했던 "동일 상태전이를 수행하는 별도 엔드포인트가 게이트를 우회하는" 사례를 교훈 삼아, 처음부터 두 엔드포인트(API-ITAM-005, API-ITAM-006) 모두를 확인해 게이트를 연동했다.
`AssetApprovalTicketSummaryProvider`를 신규 구현했다.

## 3. 변경 파일

- `source/backend/.../asset/application/{CLAUDE.md,AssetService.java}`
- `source/backend/.../asset/application/AssetApprovalTicketSummaryProvider.java`(신규)
- `source/backend/.../asset/application/dto/AssetDetailResponse.java`
- `source/backend/.../asset/presentation/AssetController.java`
- `source/frontend/src/features/asset/{AssetDetailPage.tsx,CLAUDE.md,types.ts}`

## 4. 테스트 결과

ASSET+VULNERABILITY 통합 테스트 18건 전부 PASS했다.
생애주기 전이·자산 폐기 두 엔드포인트 모두 게이트 우회가 재발하지 않음을 확인했다.
커밋 `b2acf23`으로 반영했다.
