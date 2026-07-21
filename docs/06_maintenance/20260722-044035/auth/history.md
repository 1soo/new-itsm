---
date: 20260722-044035
domain: auth
change_type: [new, modified]
keywords: [targetState, 승인프로세스관리화면, API-AUTH-031]
---

# 유지보수 이력 — auth

> 유지보수 일시: 20260722-044035 · 도메인: auth

## 1. 요구사항

승인 프로세스 관리자 화면·API가 신규 매칭 축인 `targetState`(적용 상태)를 등록·조회할 수 있어야 한다.
특정 상태를 지정하는 규칙은 승인요청자 역할을 반드시 함께 지정해야 하며, 위반 시 저장할 수 없어야 한다.

## 2. 해결 방법

`ApprovalProcessAdminService`/DTO/`AdminApprovalProcessController`에 `targetState` 필드를 반영했다.
`target_state`가 값을 가지면 승인요청자 역할이 최소 1개 이상 있어야 한다는 `validateTargetStateRequiresRole` 검증을 신규 추가했다(`domain` NULL이면 `target_state`도 NULL이어야 하는 기존 종속 규칙과 동일한 형태).
도메인별 유효 상태값·라벨 목록을 조회하는 `GET /api/v1/admin/approval-processes/domains/{domain}/states`(API-AUTH-031)를 신규 구현했다.
`ApprovalProcessFormPage.tsx`(SCR-ADMIN-008)에 도메인 선택 다음 순서로 "적용 상태" select를 추가하고, 구체 상태를 선택하면 요청자 역할 1개 이상을 선택해야만 저장 버튼이 활성화되도록 프론트 검증을 추가했다.
전체 도메인 선택 시 적용 상태 select 자체를 숨기도록 했다(요청유형과 동일한 종속 규칙).
`ApprovalProcessListPage.tsx`(SCR-ADMIN-007)에 "적용 상태" 컬럼을 추가했다.

## 3. 변경 파일

- `source/backend/src/main/java/com/itsm/auth/application/ApprovalProcessAdminService.java`
- `source/backend/src/main/java/com/itsm/auth/application/dto/{CreateApprovalProcessRequest,ApprovalProcessSummaryResponse,ApprovalProcessDetailResponse,TargetStateOption}.java`
- `source/backend/src/main/java/com/itsm/auth/presentation/AdminApprovalProcessController.java`
- `source/frontend/src/features/admin/{types.ts,api.ts,ApprovalProcessFormPage.tsx,ApprovalProcessListPage.tsx}`
- `docs/02_plan/api_spec/auth.md`(API-AUTH-031 신규 절, API-AUTH-025~028 스키마 반영)
- `docs/02_plan/screen/admin.md`(SCR-ADMIN-007/008 절 갱신)

## 4. 테스트 결과

공용 승인엔진 통합 테스트 라운드에 포함되어 검증됐다(`docs/04_test/20260722-040618`, `docs/04_test/20260722-042424`).
관리자 화면 저장 검증(적용 상태 지정 시 요청자 역할 필수) 관련 별도 결함은 발견되지 않았다.
코드 리뷰(Standards축/Spec축) 발견 사항 없었다.
커밋 `c8f9386`으로 반영했다.
