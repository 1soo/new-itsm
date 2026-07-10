# CLAUDE.md

엔터프라이즈 서비스 관리(ESM) 기능. 부서별(HR/법무/시설/재무, IT는 기존 SRM 유지) 서비스 카탈로그·요청 처리, HR 케이스 관리(HR 전용 민감정보), 온보딩/오프보딩 체크리스트(부서 간 하위 작업 오케스트레이션), ESM 지표 대시보드 화면과 API·타입, 부서/상태 표시 매핑을 제공한다. 역할: END_USER(포털·제출·내 요청), DEPT_COORDINATOR(처리 큐·내 하위 작업), PROCESS_OWNER(카탈로그 관리·지표), HR_CASE_MANAGER(HR 케이스, 타 역할 403). API 계약은 esm.md 기준.

## 파일
- `api.ts` — ESM API 호출(`esmApi`: 카탈로그 목록/상세/생성/수정, 요청 제출/목록/상세/상태전이/코멘트, HR 케이스 접수/목록/상세/상태전이, 체크리스트 상세, 내 하위 작업 목록/완료 처리, 지표).
- `types.ts` — ESM 도메인 타입(`Department`/`ChecklistTemplateType`/`EsmRequestStatus`/`HrCaseStatus`/`ChecklistStatus`/`ChecklistTaskStatus` 등, `CatalogItemDetail`/`EsmRequestDetail`/`HrCaseDetail`/`ChecklistDetail`/`MyChecklistTask`/`EsmMetrics`).
- `status.ts` — 부서 라벨(`departmentLabel`, `DEPARTMENTS`=카탈로그용 4개/`TASK_DEPARTMENTS`=+IT 5개), 각 상태 라벨/tone 매핑, HR 케이스 4단계 순차 다음 상태(`hrCaseNextStatus`).
- `format.ts` — 날짜·일시 표시 포맷터.
- `DeptPortalPage.tsx` — 부서 서비스 포털(SCR-ESM-001). 부서 탭(Tabs 공통 컴포넌트 없어 버튼형 토글로 구현)+검색+카탈로그 카드.
- `DeptRequestSubmitPage.tsx` — 부서 요청 제출(SCR-ESM-002). 동적 양식(DynamicForm 재사용) + 온보딩/오프보딩 유형이면 대상자명 필수 입력, 체크리스트 자동 생성 안내 토스트.
- `MyEsmRequestsPage.tsx` — 내 부서 요청 목록(SCR-ESM-003). scope=mine.
- `EsmRequestQueuePage.tsx` — 부서 요청 처리 큐(SCR-ESM-004). DEPT_COORDINATOR 전용, 소속 부서는 BE가 로그인 사용자 기준으로 강제 스코프하므로 별도 부서 선택 UI 없음.
- `EsmRequestDetailPage.tsx` — 부서 요청 상세(SCR-ESM-005). 상태 전이(DEPT_COORDINATOR만 버튼 노출, allowedTransitions 미제공이라 FE 폴백 계산)·코멘트·연계 체크리스트 진행률 카드.
- `EsmCatalogManagePage.tsx` — 부서별 카탈로그 관리(SCR-ESM-006). PROCESS_OWNER 전용. `FieldBuilder`(양식 필드) 재사용 + 신규 로컬 `ChecklistTemplateBuilder`(체크리스트 템플릿 하위 작업 반복 입력, 공통 컴포넌트로 승격하지 않고 이 파일에 로컬 구현).
- `HrCaseListPage.tsx` — HR 케이스 목록(SCR-ESM-007). HR_CASE_MANAGER 전용, "케이스 접수"는 Modal 폼.
- `HrCaseDetailPage.tsx` — HR 케이스 상세(SCR-ESM-008). 접수→기록→조사→해결 순차 전이(다음 단계 버튼 1개만 노출), 연결 항목 패널 없음(민감정보).
- `ChecklistDetailPage.tsx` — 온보딩/오프보딩 체크리스트 상세(SCR-ESM-009). 조회 전용(API-ESM-014 GET만, 완료 처리는 SCR-ESM-010에서 수행). 오프보딩 회수 자산은 자산 상세로 링크.
- `MyChecklistTasksPage.tsx` — 내 하위 작업 목록(SCR-ESM-010). scope=mine, PENDING 건만 "완료 처리" 버튼.
- `EsmMetricsPage.tsx` — ESM 지표 대시보드(SCR-ESM-011). PROCESS_OWNER 전용, 기간·부서 필터 + KPI 카드.
