---
date: 20260715-061016
domain: service-request
change_type: [new, modified, removed]
keywords: [카탈로그 담당자 역할 배정, 담당 큐 Select, 배정버튼 노출조건, 라우팅 담당자 필수화, 동적 승인 열람권한]
---

# 유지보수 이력 — service-request

> 유지보수 일시: 20260715-061016 · 도메인: service-request

## 1. 요구사항

서비스 카탈로그 관리에서 요청 유형별 담당자 지정 기능이 필요하다(Optional, 역할 기반 지정, 미선택 시 담당자 미배정).
서비스 카탈로그 관리의 담당 큐 선택을 Select 태그로 변경해야 한다(Optional, 미선택 시 미분류).
요청 큐에서 나에게 배정된 아이템은 "나에게 배정" 버튼을 숨겨야 한다.
요청 큐에서 상태가 라우팅됨(ROUTED) 이후 단계인 아이템도 "나에게 배정" 버튼을 숨겨야 한다.
서비스 요청 프로세스에서 담당자가 할당되지 않으면 라우팅 버튼을 비활성화해야 한다.

## 2. 해결 방법

서비스 카탈로그 항목에 담당자 후보를 좁히는 역할 필드(`assignee_role_id`, nullable)를 신규로 추가했다.
자동배정이 아니라, 서비스 데스크 담당자가 배정 시점에 해당 역할 보유자 후보 중 한 명을 직접 선택하는 방식으로 설계했다.
담당자 후보 목록 조회 API(API-SRM-017)를 신규로 추가해, 요청 큐/상세 화면에서 배정 시 후보 역할 보유자 목록을 팝업으로 조회해 수동 선택할 수 있게 했다.
카탈로그 관리 화면(`CatalogManagePage.tsx`)의 담당 큐·담당자 역할 입력을 숫자 직접입력에서 Select 컴포넌트로 전환했다.
기존에 카탈로그 항목 편집 진입 시 담당 큐 값이 항상 빈 값으로 초기화되던 프리필 결함도 함께 수정했다.
요청 큐 화면(`RequestQueuePage.tsx`)에서 "나에게 배정" 버튼을, 본인에게 이미 배정된 건과 ROUTED 이후 상태인 건에서 숨기도록 노출 조건을 추가했다.
요청 상세 화면(`RequestDetailPage.tsx`)에서 담당자가 미배정 상태이면 라우팅 버튼을 비활성화하고 tooltip으로 사유를 안내했다.
백엔드는 VALIDATED→ROUTED 전이 시 담당자가 미배정이면 409(`ASSIGNEE_REQUIRED_FOR_ROUTING`)를 반환하도록 검증을 추가했다.
요청 상세조회 권한(`assertCanView`)의 기존 정적 APPROVER 전체조회 규칙을 폐지하고, 공용 승인 엔진의 동적 매칭 규칙으로 대체했다(상세 내용은 common 도메인 이력 참고).
통합 테스트 중 요청 목록 응답에 담당자 ID(`assigneeId`)가 누락되어 있던 결함을 발견해 함께 수정했다.

## 3. 변경 파일

- `docs/02_plan/database/service-request.md`
- `docs/02_plan/api_spec/service-request.md`
- `docs/02_plan/screen/service-request.md`
- `docs/02_plan/security/authorization/process_owner.md`
- `docs/02_plan/security/authorization/service_desk_agent.md`
- `docs/03_develop/plan/service-request.md`
- `source/db/sql/33_srm_catalog_assignee_role.sql`
- `source/db/sql/CLAUDE.md`
- `source/backend/src/main/java/com/itsm/srm/application/{CLAUDE.md,ServiceCatalogService.java,ServiceRequestService.java}`
- `source/backend/src/main/java/com/itsm/srm/application/dto/{CLAUDE.md,AssigneeCandidateResponse.java(신규),CatalogItemDetailResponse.java,CreateCatalogItemRequest.java,RequestSummaryResponse.java,UpdateCatalogItemRequest.java}`
- `source/backend/src/main/java/com/itsm/srm/domain/{CLAUDE.md,ServiceCatalogItem.java}`
- `source/backend/src/main/java/com/itsm/srm/presentation/{CLAUDE.md,ServiceRequestController.java}`
- `test/.../srm/application/{ServiceCatalogServiceTest.java,ServiceRequestServiceTest.java}`
- `test/.../srm/integration/SrmApprovalIntegrationTest.java`
- `source/frontend/src/features/service-request/{CLAUDE.md,CatalogManagePage.tsx,RequestDetailPage.tsx,RequestQueuePage.tsx,api.ts,types.ts}`
- `source/frontend/src/i18n/locales/{en,ko}/service-request.json`

## 4. 테스트 결과

통합 테스트 결과는 `docs/04_test/20260715-142838/service-request/`에 기록되어 있다.
테스트 중 요청 목록 응답의 `assigneeId` 누락 결함을 발견해 수정 후 재확인했다.
재확인 결과 최종 발견 사항 없이 전부 PASS했다.
커밋 `fb092ef`로 반영했다.
