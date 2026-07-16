---
date: 20260712-014911
domain: auth
change_type: [new]
keywords: [승인 프로세스, 우선순위 tier, drag&drop 순서 재정렬, CRUD API]
---

# 유지보수 이력 — auth

> 유지보수 일시: 20260712-014911 · 도메인: auth

## 1. 요구사항

System Admin이 승인 프로세스 규칙을 도메인별로 조회(SCR-ADMIN-007)하고 생성·편집(SCR-ADMIN-008)할 수 있어야 한다.
도메인 기본(tier1) / 요청유형 전용(tier2) / 승인요청자 역할 전용(tier3) 우선순위를 자동 산정해야 한다.
승인 요청자 박스(고정 1개, 드래그 불가) + n차 승인자 박스(최대 10차, drag&drop으로 순서 재정렬)로 구성된 플로우를 편집할 수 있어야 한다.
각 박스는 역할이 1개 이상 선택되어야 저장 가능하되, 승인자 박스 자체는 0개(요청자만 있는 프로세스)까지 허용해야 한다.
승인자 박스가 0개인 채로 생성 시도 시 확인 다이얼로그를 거쳐 진행해야 한다.
System Admin 외의 접근은 403 처리되어야 한다.

## 2. 해결 방법

`ApprovalProcessAdminService`와 관련 DTO(ApprovalDomainResponse, ApprovalProcessDeletedResponse, ApprovalProcessDetailResponse, ApprovalProcessStepInput, ApprovalProcessSummaryResponse, CreateApprovalProcessRequest, UpdateApprovalProcessRequest)를 신규 구현했다.
`AdminApprovalProcessController`에 도메인 목록 조회(API-AUTH-023), 요청유형 목록 조회(API-AUTH-024), 승인 프로세스 CRUD(API-AUTH-025~029)를 구현했다.
프론트엔드에 목록 페이지(ApprovalProcessListPage)와 생성/편집 전용 페이지(ApprovalProcessFormPage)를 모달이 아닌 별도 페이지로 신규 구현하고, drag&drop 기반 차수 재정렬을 구현했다.
`ApprovalProcessAdminIntegrationTest`로 통합 테스트를 구현했다.

## 3. 변경 파일

- `source/backend/.../auth/application/ApprovalProcessAdminService.java`
- `source/backend/.../auth/application/dto/{ApprovalDomainResponse,ApprovalProcessDeletedResponse,ApprovalProcessDetailResponse,ApprovalProcessStepInput,ApprovalProcessSummaryResponse,CreateApprovalProcessRequest,UpdateApprovalProcessRequest}.java`
- `source/backend/.../auth/presentation/AdminApprovalProcessController.java`
- `source/frontend/src/features/admin/{ApprovalProcessFormPage.tsx,ApprovalProcessListPage.tsx,api.ts,types.ts}`
- `test/.../auth/integration/ApprovalProcessAdminIntegrationTest.java`
- `docs/02_plan/{api_spec/auth.md,screen/admin.md,security/authorization/{approver,process_owner,system_admin}.md}`

## 4. 테스트 결과

Stage 1 통합 테스트(TC-ADM-001~008) 중 TC-ADM-006이 1차에서 실패(승인 프로세스 steps 교체 시 500)했다.
원인은 `ApprovalProcessAdminService.update()`가 기존 step을 삭제 후 같은 트랜잭션에서 재삽입하는데, Hibernate 기본 flush 순서(insert가 delete보다 먼저 실행)로 유니크 제약(`uq_approval_process_step`) 위반이 발생한 것이었다.
delete 직후 flush를 강제하도록 수정한 뒤 2차 재테스트에서 1단계→2단계 확장, 원복, requesterRoleIds 단독 교체까지 전부 200 정상 확인했다.
TC-ADM-001~005, 007~008(도메인 목록, 요청유형 목록, 규칙 생성·조회·삭제, 403 확인)은 1차부터 전부 PASS했다.
커밋 `0b1caa3`으로 반영했다.
