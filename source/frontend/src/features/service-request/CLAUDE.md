# CLAUDE.md

서비스 요청(SRM) 관리 기능. 포털·요청 제출·목록·큐·상세·카탈로그 관리·지표 화면·API·타입, 상태/SLA 표시 매핑 제공. END_USER/SERVICE_DESK_AGENT/PROCESS_OWNER 역할 기반. API 계약: service-request.md. 승인 대기·결정은 승인 프로세스 커스텀 기능(유지보수 요청)으로 `features/common/ApprovalInboxPage.tsx`(SCR-COM-014, 전 도메인 공용)로 이관(기존 SCR-SRM-006 제거).

## 파일
- `api.ts` — SRM API 호출(`srmApi`: 카탈로그 목록/상세/생성/수정, 지식 추천, 요청 생성/목록/상세, 큐 목록, 담당자 배정, 담당자 후보 목록 조회(`getAssigneeCandidates`, API-SRM-017, 2026-07-15 유지보수 요청), 상태 전이, 코멘트, CSAT, 지표). 승인 관련 API(구 API-SRM-011/012)는 공용 승인 API(`features/common/api.ts`)로 대체되어 제거.
- `types.ts` — SRM 도메인 타입(`SrStatus`/`SlaStatus`/`TargetStatus`, `CatalogItem*`/`RequestSummary`/`RequestDetail`/`RequestMetrics`/`AssigneeCandidate` 등). `CatalogItemDetail`/`CatalogItemInput`에 `queueId`/`assigneeRoleId`(2026-07-15 유지보수 요청, 담당자 역할) 추가. `RequestSummary.assigneeId`는 요청 큐 배정 버튼 노출 조건 판정용(2026-07-15). `RequestDetail.approval`(`RequestApproval`)은 `approvalRequestId`/`status`만 보유(진행 상태 조회는 공용 API-COM-004). 동적 폼 스키마(`FormFieldSchema`)는 공통 컴포넌트와 계약 공유.
- `status.ts` — 요청 상태·SLA 라벨/tone 매핑.
- `format.ts` — 날짜·일시 표시 포맷터.
- `PortalPage.tsx` — 서비스 포털/카탈로그 탐색(SCR-SRM-001).
- `RequestSubmitPage.tsx` — 요청 제출(동적 폼)(SCR-SRM-002).
- `RequestListPage.tsx` — 요청 목록(SCR-SRM-003).
- `RequestQueuePage.tsx` — 요청 큐(Agent)(SCR-SRM-004). 배정 버튼("배정")은 (1) 이미 본인에게 배정 또는 (2) 상태가 ROUTED 이후(ROUTED/IN_FULFILLMENT/FULFILLED/CLOSED)면 숨김(2026-07-15 유지보수 요청). 클릭 시 담당자 배정 팝업(`Modal` 재사용)이 열려 후보 목록(API-SRM-017)을 조회 — 후보가 있으면 이름 클릭으로 배정, 후보가 없거나 후보 중 본인이 있으면 "나에게 배정" 버튼도 노출.
- `RequestDetailPage.tsx` — 요청 상세·상태 전이·코멘트·CSAT(SCR-SRM-005). 승인 패널은 공용 `ApprovalPanel`(`components/common`)에 API-COM-004 조회 결과(`steps`/`currentStepNo`) 주입해 진행 상태만 표시(결정 액션 없음, 처리는 SCR-COM-014에서). 미매칭(`emptyMessage`) 문구는 요청 상태 기준으로 분기(2026-07-15 유지보수 요청) — 승인 게이트가 ROUTED→IN_FULFILLMENT 전이 시점에만 평가되므로, 그 전(SUBMITTED/VALIDATED/ROUTED)에는 "미확정" 문구를, 게이트 평가 후(그 외 상태)에 인스턴스가 없으면 기존 "승인 절차 없음" 문구를 표시. ROUTED 전이 버튼은 담당자(`assignee`) 미배정 시 비활성화+tooltip(`vulnerability` 도메인 REMEDIATION 가드와 동일 패턴, 2026-07-15), 서버 409(`ASSIGNEE_REQUIRED_FOR_ROUTING`)도 동일 문구로 방어적 토스트 처리.
- `CatalogManagePage.tsx` — 서비스 카탈로그 관리(양식 빌더)(SCR-SRM-007). 승인 필요 토글 제거(승인 프로세스 커스텀 기능으로 SCR-ADMIN-008에서 별도 설정). 담당 큐·담당자 역할(`features/auth/api.ts`의 `getRoles`, API-AUTH-030)을 Input이 아닌 Select로 제공(2026-07-15 유지보수 요청, sentinel 값 `__NONE__`으로 "미분류"/"선택 안 함" 표현), 편집 진입 시 상세 조회 응답(`queueId`/`assigneeRoleId`)으로 프리필(기존 큐 값이 항상 빈 값으로 리셋되던 결함 수정).
- `MetricsPage.tsx` — 요청 지표(CSAT·SLA 등)(SCR-SRM-008).
