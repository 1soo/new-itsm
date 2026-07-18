---
date: 20260715-031710
domain: auth
change_type: [new, modified]
keywords: [감사 로그 actor 수정, 승인 프로세스 우선순위 재설계, 승인 프로세스 삭제, 3축 스코프]
---

# 유지보수 이력 — auth

> 유지보수 일시: 20260715-031710 · 도메인: auth

## 1. 요구사항

관리자가 qa.tester@itsm.local 계정 변경 및 역할 변경을 수행했는데, 감사 로그상 수행 주체(actor)가 관리자가 아니라 대상자 본인으로 기록되는 버그를 수정해야 한다.
승인 프로세스는 도메인·요청 유형·요청자 역할 3개 축으로 각각 독립적으로 스코프를 지정할 수 있어야 한다.
각 축은 비어있을 수 있으며, 비어있으면 해당 축의 모든 옵션에 해당하는 것으로 간주해야 한다.
더 구체적인 스코프로 지정된 규칙이 더 넓은 스코프의 규칙보다 우선 적용되어야 한다.
승인 프로세스 삭제 기능이 없어 신규로 추가해야 한다.
생성된 승인 프로세스 상세 화면으로 이동 시 요청 유형이 select 컴포넌트에 렌더링되지 않는 버그를 수정해야 한다.

## 2. 해결 방법

`UserAdminService`의 계정 생성·수정·상태변경·역할부여·역할회수 5개 메서드가 감사 로그 기록 시 대상 유저 자신의 id/email을 actor로 잘못 전달하던 것을, `SecurityUtils.currentPrincipal()`로 조회한 현재 로그인 관리자 정보로 정정했다.
같은 도메인의 `ScreenAdminService.recordChange()`가 이미 사용하던 올바른 패턴을 재사용했다.
`ApprovalProcessAdminService`의 우선순위(priorityTier) 산정 방식을 기존 3단계 상호배타 체계(1=도메인/2=요청유형/3=역할)에서, 도메인·요청유형·요청자역할 3축을 각각 독립적으로 지정 가능한 방식으로 재설계했다.
산정식은 "지정된 축 개수×10 + 역할지정×4 + 요청유형지정×2 + 도메인지정×1"이며, 실제로 나오는 값은 0/11/14/23/25/37이다.
도메인이 지정되지 않으면(null) 요청유형도 반드시 null이어야 하도록 강제했다(400 처리).
우선순위 충돌(409) 검증을 6개 tier 분기로 확장했다.
tier=0/11/23은 동일 스코프 규칙 존재 여부만 검증하고, tier=14/25/37은 동일 매칭 스코프 내에서 요청자 역할 조합의 교집합 여부까지 검증한다.
이를 위해 `ApprovalProcessRepository`에 신규 조회 메서드 3개를 추가했다.
공용 승인 엔진(`common.approval.application.ApprovalGateService`)의 `matchProcess`가 도메인이 지정된 규칙뿐 아니라 도메인 미지정(전체 도메인 적용) 규칙도 게이트 매칭 후보에 포함하도록 함께 확장되었다(상세 내용은 common 도메인 이력 참고).
프론트엔드는 승인 프로세스 목록(SCR-ADMIN-007)에 삭제 버튼과 확인 다이얼로그를 추가했다.
기존 메뉴 관리 화면(`MenuManagementPage.tsx`)의 삭제 확인 다이얼로그 패턴을 재사용했다.
목록의 우선순위 배지를 숫자 tier값 대신 축 조합을 나타내는 라벨로 변경했다.
승인 프로세스 생성/편집 화면(SCR-ADMIN-008)에는 "전체 도메인" 선택 옵션을 추가해 선택 시 `domain: null`을 전송하도록 했다.
편집 모드로 진입할 때 요청 유형 후보(subtypes) 조회를 스킵하던 결함을 수정해, 편집 모드에서도 도메인이 확정되면 요청 유형 후보를 조회하도록 변경했다.
통합 테스트 중 추가로, 승인 요청자 박스를 역할 0개(전체 요청자 대상)로 저장할 수 없던 결함을 발견해 수정했다.
공용 컴포넌트 `approval-process-flow.tsx`의 빈 박스 검증(`hasEmptyBox`)이 승인요청자 박스에도 잘못 적용되고 있었는데, 이를 승인자 박스에만 적용되도록 분리했다.

## 3. 변경 파일

- `source/backend/src/main/java/com/itsm/auth/application/UserAdminService.java`
- `source/backend/src/main/java/com/itsm/auth/application/ApprovalProcessAdminService.java`
- `source/backend/src/main/java/com/itsm/common/approval/domain/repository/ApprovalProcessRepository.java`
- `source/frontend/src/features/admin/ApprovalProcessListPage.tsx`
- `source/frontend/src/features/admin/ApprovalProcessFormPage.tsx`
- `source/frontend/src/features/admin/api.ts`
- `source/frontend/src/components/common/approval-process-flow.tsx`

## 4. 테스트 결과

통합 테스트 14건 전부 PASS했다(`docs/04_test/20260715-112437/auth/`).
승인 요청자 박스 역할 0개 저장 불가 결함 발견 후 수정하고, tier=0/11/23 조합의 UI 생성 시나리오로 재테스트해 정상 동작을 확인했다.
코드 리뷰 Standards축에서 FE race-guard 미복원 1건이 발견되어 반영했고, Spec축에서는 발견 사항이 없었다.
커밋 `35c2375`(본 기능), `82a173c`(타 도메인 테스트의 stale tier 하드코딩 정리)로 반영했다.
