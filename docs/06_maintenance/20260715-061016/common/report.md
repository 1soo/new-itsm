---
date: 20260715-061016
domain: common
change_type: [new, modified, removed]
keywords: [canApproverView, 동적상세조회권한, 정적권한폐지]
---

# 유지보수 이력 — common

> 유지보수 일시: 20260715-061016 · 도메인: common

## 1. 요구사항

승인 프로세스의 승인 대상자(각 차수 승인자, ApprovalProcessStepRole)로 할당된 역할을 가진 사용자는, 그 승인 프로세스가 적용되는 요청 아이템의 상세 정보를 조회할 수 있는 권한을 가져야 한다.
이 권한은 도메인/요청 유형/승인 요청자 역할 3축으로 매칭되는 승인 프로세스 규칙을 기준으로 판정해야 하며, 승인 인스턴스 생성 여부와 무관하게 규칙 설정 자체로 판정해야 한다.
이 규칙은 승인 프로세스가 정의될 수 있는 8개 도메인(SRM/CHANGE/INCIDENT/PROBLEM/ASSET/VULNERABILITY/COMPLIANCE/ESM)에 공통 적용되어야 한다.
기존 SRM/CHANGE 도메인이 갖고 있던 "APPROVER 역할이면 도메인 내 전체 티켓 상세조회 가능"이라는 정적 권한은 이번 동적 규칙으로 완전히 대체(폐지)해야 한다.
(부수 발견) INCIDENT 도메인은 상세조회 시 백엔드 역할 체크 자체가 없어 인증된 사용자라면 누구나 API 직접 호출로 전체 조회가 가능한 기존 결함이 있어, 이번 기회에 다른 도메인처럼 명시적 역할 체크(SERVICE_DESK_AGENT/INCIDENT_MANAGER)를 추가하기로 함께 결정되었다.

## 2. 해결 방법

공용 승인 엔진(`ApprovalGateService`)에 `canApproverView` 메서드를 신설해, 티켓의 도메인/요청유형/요청자역할과 매칭되는 `approval_process`의 각 차수 `step_role` 규칙 설정을 근거로 상세조회 권한을 동적으로 판정하도록 구현했다.
SRM(`ServiceRequestService`)과 CHANGE(`ChangeService`)의 기존 정적 "APPROVER 전체조회" 권한을 폐지하고 `canApproverView` 동적 매칭으로 대체했다.
INCIDENT(`IncidentService`)는 기존에 없던 상세조회 역할 체크를 신설(SERVICE_DESK_AGENT/INCIDENT_MANAGER 명시 체크)하면서 `canApproverView` 동적 매칭을 OR 조건으로 함께 추가했다.
PROBLEM/VULNERABILITY/COMPLIANCE/ESM(`ProblemService`/`VulnerabilityService`/`ComplianceService`/`EsmRequestService`)은 기존 담당 매니저 전용 조회 조건을 유지한 채 `canApproverView` 동적 매칭을 신규로 추가했다.
ASSET 도메인은 검토 결과 원래부터 전체 인증 사용자에게 상세조회가 공개되도록 의도된 설계였음을 확인해(관련 `approver.md` 서술이 실제와 달랐던 문서 오류였음), 코드 변경 없이 문서만 정정하고 스킵했다.
코드 리뷰 중 ESM에서 동적 승인자 조회 권한이 상세조회뿐 아니라 댓글 작성(API-ESM-009)까지 새어나가는 범위초과 결함을 발견해, 상세조회 전용 가드로 분리했다.
프론트엔드는 SRM/CHANGE/PROBLEM/ASSET/VULNERABILITY/COMPLIANCE/ESM 7개 도메인(INCIDENT 제외)의 라우트 가드에 ROLE_APPROVER를 추가해 화면 접근을 허용했다.
통합 테스트 중 COMPLIANCE 상세 화면에서 감사 로그 조회 403이 상세조회 자체의 403으로 잘못 전파되던 결함을 발견해 수정했다.
`docs/00_context/glossary.md`에 관련 용어를 추가하고, `docs/02_plan/security/authorization/approver.md`를 이번 동적 조회 권한 기준으로 갱신했다.

## 3. 변경 파일

- `docs/02_plan/api_spec/common.md`
- `docs/02_plan/security/authorization/approver.md`
- `docs/00_context/glossary.md`
- `docs/03_develop/plan/common.md`
- `docs/03_develop/plan/{asset,change,compliance,esm,incident,problem,vulnerability}.md`(포인터 절 추가)
- `source/backend/src/main/java/com/itsm/common/approval/application/{ApprovalGateService.java,CLAUDE.md}`
- `source/backend/src/main/java/com/itsm/common/exception/ErrorCode.java`
- `source/backend/src/main/java/com/itsm/change/application/{ChangeService.java,CLAUDE.md}`
- `source/backend/src/main/java/com/itsm/incident/application/{IncidentService.java,CLAUDE.md}`
- `source/backend/src/main/java/com/itsm/problem/application/{ProblemService.java,CLAUDE.md}`
- `source/backend/src/main/java/com/itsm/vulnerability/application/{VulnerabilityService.java,CLAUDE.md}`
- `source/backend/src/main/java/com/itsm/compliance/application/{ComplianceService.java,CLAUDE.md}`
- `source/backend/src/main/java/com/itsm/esm/application/{EsmRequestService.java,CLAUDE.md}`
- `test/.../change/application/ChangeServiceTest.java`
- `test/.../esm/application/EsmRequestServiceTest.java`
- `test/.../{asset,auth,change,compliance,esm,incident,infra,knowledge,problem,search,vulnerability}/integration/*`(Testcontainers 스키마 동기화용 DDL 마운트만 추가)
- `source/frontend/src/routes/{index.tsx,CLAUDE.md}`
- `source/frontend/src/features/compliance/{ComplianceDetailPage.tsx,CLAUDE.md}`

## 4. 테스트 결과

통합 테스트 결과는 `docs/04_test/20260715-142838/common/`에 기록되어 있다.
코드 리뷰에서 ESM 댓글 작성 권한 범위초과 결함을 발견해 상세조회 전용 가드로 분리 수정했다.
통합 테스트 중 COMPLIANCE 상세화면 감사로그 403 전파 결함을 발견해 수정했다.
재확인 후 최종 발견 사항 없이 전부 PASS했다.
커밋 `fb092ef`로 반영했다.
