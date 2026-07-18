---
date: 20260712-014911
domain: compliance
change_type: [new, modified]
keywords: [승인 게이트, 시정조치 상태전이, TicketType.CORRECTIVE_ACTION, 항목별 개별 승인]
---

# 유지보수 이력 — compliance

> 유지보수 일시: 20260712-014911 · 도메인: compliance

## 1. 요구사항

시정조치 상태 전이(API-COMP-008, targetStatus=RESOLVED)에 승인 게이트가 걸려야 한다.
시정조치가 요구사항 하위에 여러 건(1:N) 존재할 수 있으므로, 요구사항 전체 단위가 아닌 시정조치 항목별로 독립된 승인이 이뤄져야 한다.

## 2. 해결 방법

`ComplianceService`의 시정조치 상태 전이(API-COMP-008)에 게이트 체크를 연동했다.
시정조치가 요구사항 하위에 여러 건 존재할 수 있다는 화면 설계(screen/compliance.md) 근거에 따라, 신규 `TicketType.CORRECTIVE_ACTION`을 도입해 시정조치 단위로 개별 승인 인스턴스를 부여했다(요구사항 전체 단위가 아님).
`CorrectiveActionApprovalTicketSummaryProvider`를 신규 구현했다.

## 3. 변경 파일

- `source/backend/.../compliance/application/{CLAUDE.md,ComplianceService.java}`
- `source/backend/.../compliance/application/CorrectiveActionApprovalTicketSummaryProvider.java`(신규)
- `source/backend/.../compliance/application/dto/RequirementDetailResponse.java`
- `source/backend/.../compliance/presentation/ComplianceController.java`
- `test/.../compliance/application/ComplianceServiceTest.java`
- `source/frontend/src/features/compliance/{CLAUDE.md,ComplianceDetailPage.tsx,types.ts}`
- `source/backend/.../common/ticket/{CLAUDE.md,TicketType.java}`(공통, 신규 CORRECTIVE_ACTION 추가)

## 4. 테스트 결과

COMPLIANCE+ESM 통합 테스트 19건 전부 PASS했다(시정조치 항목별 독립 승인 확인 포함).
커밋 `f421aa9`로 반영했다.
